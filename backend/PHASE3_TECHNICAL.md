# PHASE 3: BANDWIDTH MONITORING - TECHNICAL DEEP DIVE

## Overview
Track network data usage per device to identify bandwidth hogs, detect anomalies, and plan capacity.

## Architecture Decisions

### Why Separate BandwidthUsage Entity?
**Decision:** Time-series data in separate table.

**Code:**
```java
@Entity
@Table(name = "bandwidth_usage")
public class BandwidthUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;
    
    private Long bytesSent;
    private Long bytesReceived;
    private Long totalBytes;
    private Long packetsSent;
    private Long packetsReceived;
    private LocalDateTime recordedAt;
}
```

**Reasoning:**
- Time-series data (multiple samples per device)
- Historical tracking
- Trend analysis
- Storage optimization (old data can be archived)

**Alternative Considered:**
- Store in Device entity as JSON
- **Rejected:** Can't query efficiently, no aggregation

### Database Relationship Pattern

**One Device → Many Bandwidth Samples:**
```
devices:
id=1, ip=192.168.1.1

bandwidth_usage:
id=1, device_id=1, bytes=1000, recorded_at=10:00
id=2, device_id=1, bytes=2000, recorded_at=10:05
id=3, device_id=1, bytes=3000, recorded_at=10:10
```

**Why this works:**
- Track usage over time
- Calculate trends (increasing/decreasing)
- Identify spikes
- Generate graphs

### Why Long for Bytes?
```java
private Long bytesSent;
private Long bytesReceived;
```

**Reasoning:**
- Integer max: 2,147,483,647 bytes = 2GB
- Long max: 9,223,372,036,854,775,807 bytes = 8 exabytes
- Network traffic can exceed 2GB easily
- Long prevents overflow

**Example overflow:**
```java
int bytes = 2_000_000_000;  // 2GB
bytes += 200_000_000;       // Add 200MB
// Result: -2,094,967,296 (NEGATIVE! Overflow!)

long bytes = 2_000_000_000L;
bytes += 200_000_000L;
// Result: 2,200,000,000 (Correct)
```

## Data Collection Strategy

### Why netstat?
**Decision:** Use Windows `netstat -e` command.

**Code:**
```java
ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "netstat -e");
Process process = pb.start();
```

**Reasoning:**
- Built into Windows (no installation)
- Shows interface statistics
- Bytes sent/received
- Packets sent/received

**netstat -e output:**
```
Interface Statistics

                           Received            Sent
Bytes                    1234567890      9876543210
Unicast packets             1000000          500000
```

**Limitations:**
- System-wide (not per-device on Windows)
- Requires correlation with device IP
- Alternative: Pcap4J for per-device (more complex)

### Parsing netstat Output

**Strategy:**
```java
private BandwidthData parseNetstat(String output) {
    // Find "Bytes" line
    // Extract received and sent values
    // Parse as Long
    
    String[] lines = output.split("\n");
    for (String line : lines) {
        if (line.contains("Bytes")) {
            String[] parts = line.trim().split("\\s+");
            long received = Long.parseLong(parts[1]);
            long sent = Long.parseLong(parts[2]);
            return new BandwidthData(received, sent);
        }
    }
}
```

**Why regex split?**
- `\\s+` = one or more whitespace
- Handles variable spacing
- Robust parsing

### Recording Bandwidth Snapshots

**Method: recordBandwidth(String ipAddress)**
```java
public BandwidthUsage recordBandwidth(String ipAddress) {
    // 1. Find device
    Device device = deviceRepository.findByIpAddress(ipAddress)
        .orElseThrow(() -> new DeviceNotFoundException(ipAddress));
    
    // 2. Get current bandwidth stats
    BandwidthData data = getCurrentBandwidth();
    
    // 3. Create snapshot
    BandwidthUsage usage = new BandwidthUsage();
    usage.setDevice(device);
    usage.setBytesSent(data.getSent());
    usage.setBytesReceived(data.getReceived());
    usage.setTotalBytes(data.getSent() + data.getReceived());
    usage.setRecordedAt(LocalDateTime.now());
    
    // 4. Save to database
    return bandwidthRepository.save(usage);
}
```

**Why snapshot approach?**
- Point-in-time measurement
- Compare snapshots to calculate usage
- Example: 10:00 = 1GB, 10:05 = 1.2GB → Used 200MB in 5 minutes

## Service Layer Design

### BandwidthService Methods

**1. Get Device History**
```java
public List<BandwidthUsage> getDeviceHistory(Long deviceId) {
    return bandwidthRepository.findByDeviceIdOrderByRecordedAtDesc(deviceId);
}
```

**Why OrderByRecordedAtDesc?**
- Most recent first
- Chronological order
- Spring Data JPA generates: `ORDER BY recorded_at DESC`

**2. Calculate Summary**
```java
public BandwidthSummary getDeviceSummary(Long deviceId) {
    List<BandwidthUsage> history = getDeviceHistory(deviceId);
    
    long totalSent = history.stream()
        .mapToLong(BandwidthUsage::getBytesSent)
        .sum();
    
    long totalReceived = history.stream()
        .mapToLong(BandwidthUsage::getBytesReceived)
        .sum();
    
    long totalBytes = totalSent + totalReceived;
    double totalMB = totalBytes / (1024.0 * 1024.0);
    
    return new BandwidthSummary(
        totalSent, totalReceived, totalBytes, totalMB, history.size()
    );
}
```

**Why Stream API?**
- Functional programming style
- Readable aggregation
- Efficient (single pass)

**Stream breakdown:**
```java
history.stream()              // Convert list to stream
    .mapToLong(...)          // Extract long values
    .sum();                  // Sum all values
```

**Alternative (imperative):**
```java
long total = 0;
for (BandwidthUsage usage : history) {
    total += usage.getBytesSent();
}
```

### Unit Conversion

**Bytes to Megabytes:**
```java
double totalMB = totalBytes / (1024.0 * 1024.0);
```

**Why 1024.0 not 1024?**
- 1024 = integer division (truncates)
- 1024.0 = double division (precise)

**Example:**
```java
long bytes = 1500000;
int mb1 = bytes / (1024 * 1024);      // = 1 (truncated)
double mb2 = bytes / (1024.0 * 1024.0); // = 1.43 (precise)
```

**Unit hierarchy:**
- 1 KB = 1024 bytes
- 1 MB = 1024 KB = 1,048,576 bytes
- 1 GB = 1024 MB = 1,073,741,824 bytes

## Repository Design

### Custom Query Methods

**Find by device, ordered by time:**
```java
public interface BandwidthRepository extends JpaRepository<BandwidthUsage, Long> {
    List<BandwidthUsage> findByDeviceIdOrderByRecordedAtDesc(Long deviceId);
    List<BandwidthUsage> findByDeviceIdAndRecordedAtBetween(
        Long deviceId, LocalDateTime start, LocalDateTime end
    );
}
```

**Generated SQL:**
```sql
-- findByDeviceIdOrderByRecordedAtDesc(1L)
SELECT * FROM bandwidth_usage 
WHERE device_id = 1 
ORDER BY recorded_at DESC;

-- findByDeviceIdAndRecordedAtBetween(1L, start, end)
SELECT * FROM bandwidth_usage 
WHERE device_id = 1 
AND recorded_at BETWEEN ? AND ?;
```

**Use case for date range:**
```java
// Get last 24 hours
LocalDateTime end = LocalDateTime.now();
LocalDateTime start = end.minusHours(24);
List<BandwidthUsage> last24h = repository.findByDeviceIdAndRecordedAtBetween(
    deviceId, start, end
);
```

### Aggregation Queries

**Using @Query for complex aggregations:**
```java
@Query("SELECT SUM(b.totalBytes) FROM BandwidthUsage b WHERE b.device.id = :deviceId")
Long getTotalBytesForDevice(@Param("deviceId") Long deviceId);

@Query("SELECT AVG(b.totalBytes) FROM BandwidthUsage b WHERE b.device.id = :deviceId")
Double getAverageBytesForDevice(@Param("deviceId") Long deviceId);
```

**Why @Query?**
- Method name would be too long
- Complex aggregation
- Database-level calculation (faster)

## Controller Design

### Endpoint: POST /api/bandwidth/record/{ip}

**Why POST?**
- Creates new record
- Side effect (database write)
- Not idempotent

**Response:**
```json
{
  "id": 1,
  "device": {
    "id": 1,
    "ipAddress": "192.168.1.1"
  },
  "bytesSent": 1048576,
  "bytesReceived": 2097152,
  "totalBytes": 3145728,
  "packetsSent": 1000,
  "packetsReceived": 1500,
  "recordedAt": "2024-03-15T14:30:00"
}
```

### Endpoint: GET /api/bandwidth/device/{deviceId}/summary

**Response structure:**
```json
{
  "totalBytesSent": 10485760,
  "totalBytesReceived": 20971520,
  "totalBytes": 31457280,
  "totalMB": 30.0,
  "recordCount": 10,
  "averageBytesPerSample": 3145728,
  "firstRecorded": "2024-03-15T10:00:00",
  "lastRecorded": "2024-03-15T14:30:00"
}
```

**Why summary endpoint?**
- Dashboard needs aggregated data
- Avoid client-side calculation
- Single API call
- Cached on server

## Data Retention Strategy

### Why Limit Historical Data?
**Problem:** Bandwidth data grows infinitely.

**Example:**
- 100 devices
- Record every 5 minutes
- 12 records/hour/device
- 288 records/day/device
- 28,800 records/day total
- 10,512,000 records/year

**Solution: Retention policy**
```java
@Scheduled(cron = "0 0 2 * * ?")  // 2 AM daily
public void cleanupOldData() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
    bandwidthRepository.deleteByRecordedAtBefore(cutoff);
}
```

**Why 30 days?**
- Balance between history and storage
- Sufficient for trend analysis
- Configurable via properties

**Alternative: Aggregation**
```java
// Keep hourly averages instead of 5-minute samples
@Scheduled(cron = "0 0 * * * ?")  // Every hour
public void aggregateHourlyData() {
    // Calculate hourly average
    // Delete individual samples
    // Keep aggregated data
}
```

## Performance Optimization

### Database Indexes
```sql
CREATE INDEX idx_device_recorded ON bandwidth_usage(device_id, recorded_at);
CREATE INDEX idx_recorded_at ON bandwidth_usage(recorded_at);
```

**Why composite index?**
- Queries filter by device AND sort by time
- Single index serves both
- Faster than two separate indexes

**Index usage:**
```sql
-- Uses idx_device_recorded
SELECT * FROM bandwidth_usage 
WHERE device_id = 1 
ORDER BY recorded_at DESC;

-- Uses idx_recorded_at
DELETE FROM bandwidth_usage 
WHERE recorded_at < '2024-01-01';
```

### Pagination for Large Results

**Problem:** Device with 10,000 records = huge response

**Solution:**
```java
@GetMapping("/device/{deviceId}")
public Page<BandwidthUsage> getHistory(
    @PathVariable Long deviceId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("recordedAt").descending());
    return bandwidthRepository.findByDeviceId(deviceId, pageable);
}
```

**Response:**
```json
{
  "content": [...],
  "totalElements": 10000,
  "totalPages": 500,
  "size": 20,
  "number": 0
}
```

### Caching Summary Data

**Problem:** Summary calculation on every request

**Solution:**
```java
@Cacheable(value = "bandwidth-summary", key = "#deviceId")
public BandwidthSummary getDeviceSummary(Long deviceId) {
    // Expensive calculation
}

@CacheEvict(value = "bandwidth-summary", key = "#deviceId")
public BandwidthUsage recordBandwidth(Long deviceId) {
    // Invalidate cache when new data recorded
}
```

**Why caching?**
- Summary rarely changes
- Expensive aggregation
- Reduce database load

## Bandwidth Calculation Patterns

### Delta Calculation
**Calculate usage between two snapshots:**
```java
public long calculateUsage(BandwidthUsage start, BandwidthUsage end) {
    return end.getTotalBytes() - start.getTotalBytes();
}
```

**Example:**
```
10:00 - 1,000,000 bytes
10:05 - 1,200,000 bytes
Usage = 200,000 bytes in 5 minutes
```

### Rate Calculation
**Calculate bytes per second:**
```java
public double calculateRate(BandwidthUsage start, BandwidthUsage end) {
    long bytes = end.getTotalBytes() - start.getTotalBytes();
    long seconds = ChronoUnit.SECONDS.between(
        start.getRecordedAt(), 
        end.getRecordedAt()
    );
    return (double) bytes / seconds;
}
```

**Example:**
```
200,000 bytes in 300 seconds = 666.67 bytes/second
```

### Trend Detection
**Identify increasing usage:**
```java
public boolean isUsageIncreasing(Long deviceId) {
    List<BandwidthUsage> recent = getRecentSamples(deviceId, 10);
    
    // Compare first half vs second half
    double firstHalf = recent.subList(0, 5).stream()
        .mapToLong(BandwidthUsage::getTotalBytes)
        .average()
        .orElse(0);
    
    double secondHalf = recent.subList(5, 10).stream()
        .mapToLong(BandwidthUsage::getTotalBytes)
        .average()
        .orElse(0);
    
    return secondHalf > firstHalf * 1.2;  // 20% increase
}
```

## Real-World Use Cases

### 1. Identify Bandwidth Hogs
```java
@GetMapping("/top-users")
public List<DeviceBandwidthSummary> getTopUsers(@RequestParam int limit) {
    return deviceRepository.findAll().stream()
        .map(device -> new DeviceBandwidthSummary(
            device,
            bandwidthService.getDeviceSummary(device.getId())
        ))
        .sorted((a, b) -> Long.compare(
            b.getSummary().getTotalBytes(),
            a.getSummary().getTotalBytes()
        ))
        .limit(limit)
        .collect(Collectors.toList());
}
```

**Response:**
```json
[
  {
    "device": {"ip": "192.168.1.50", "hostname": "MEDIA-SERVER"},
    "totalGB": 500.5,
    "percentOfTotal": 45.2
  },
  {
    "device": {"ip": "192.168.1.25", "hostname": "BACKUP-SERVER"},
    "totalGB": 300.2,
    "percentOfTotal": 27.1
  }
]
```

### 2. Detect Anomalies
```java
public boolean isAnomalous(Long deviceId) {
    BandwidthSummary summary = getDeviceSummary(deviceId);
    double average = summary.getTotalBytes() / summary.getRecordCount();
    
    BandwidthUsage latest = getLatestSample(deviceId);
    
    // Anomaly if latest is 3x average
    return latest.getTotalBytes() > (average * 3);
}
```

### 3. Capacity Planning
```java
public CapacityReport generateCapacityReport() {
    long totalBandwidth = 1_000_000_000L;  // 1 Gbps
    long usedBandwidth = calculateTotalUsage();
    double utilizationPercent = (usedBandwidth * 100.0) / totalBandwidth;
    
    return new CapacityReport(
        totalBandwidth,
        usedBandwidth,
        utilizationPercent,
        utilizationPercent > 80  // Alert if > 80%
    );
}
```

## Testing Strategy

### Unit Tests
```java
@Test
void testBandwidthCalculation() {
    BandwidthUsage start = new BandwidthUsage();
    start.setTotalBytes(1000L);
    
    BandwidthUsage end = new BandwidthUsage();
    end.setTotalBytes(2000L);
    
    long usage = bandwidthService.calculateUsage(start, end);
    assertEquals(1000L, usage);
}
```

### Integration Tests
```java
@SpringBootTest
@Test
void testRecordBandwidth() {
    Device device = createTestDevice();
    
    BandwidthUsage usage = bandwidthService.recordBandwidth(device.getIpAddress());
    
    assertNotNull(usage.getId());
    assertEquals(device.getId(), usage.getDevice().getId());
    assertNotNull(usage.getRecordedAt());
}
```

### Performance Tests
```java
@Test
void testLargeDatasetPerformance() {
    // Create 10,000 records
    for (int i = 0; i < 10000; i++) {
        bandwidthRepository.save(createSample());
    }
    
    // Measure query time
    long start = System.currentTimeMillis();
    BandwidthSummary summary = bandwidthService.getDeviceSummary(1L);
    long duration = System.currentTimeMillis() - start;
    
    assertTrue(duration < 1000);  // Should complete in < 1 second
}
```

## Summary: Phase 3 Key Decisions

1. **Separate time-series entity** - Historical tracking
2. **Long for bytes** - Prevent overflow
3. **netstat for collection** - Built-in, no dependencies
4. **Snapshot approach** - Point-in-time measurements
5. **Stream API for aggregation** - Functional, readable
6. **Retention policy** - Manage data growth
7. **Composite indexes** - Query optimization
8. **Pagination** - Handle large datasets
9. **Caching** - Reduce calculation overhead
10. **Delta calculation** - Measure actual usage

---

*Continue to Phase 4...*
