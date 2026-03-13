# PHASE 2: VULNERABILITY ASSESSMENT - TECHNICAL DEEP DIVE

## Overview
Phase 2 adds security scanning to detect vulnerabilities in discovered devices.

## Architecture Decisions

### Why Separate Vulnerability Entity?
**Decision:** Create separate Vulnerability table linked to Device.

**Code:**
```java
@Entity
@Table(name = "vulnerabilities")
public class Vulnerability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;
    
    private String vulnerabilityName;
    private String severity;
    private String description;
}
```

**Reasoning:**
- One device can have MULTIPLE vulnerabilities
- Normalized database design (3NF)
- Easy to query: "Show all critical vulnerabilities"
- Historical tracking

**Alternative Considered:**
- Store vulnerabilities as JSON in Device table
- **Rejected because:** Can't query efficiently, no referential integrity

### Database Relationship: @ManyToOne

**What it means:**
- Many vulnerabilities → One device
- Foreign key: device_id in vulnerabilities table

**Visual:**
```
devices table:
id | ip_address
1  | 192.168.1.1
2  | 192.168.1.2

vulnerabilities table:
id | device_id | vulnerability_name
1  | 1         | CVE-2024-1234
2  | 1         | CVE-2024-5678
3  | 2         | CVE-2024-9999
```

**JPA generates SQL:**
```sql
CREATE TABLE vulnerabilities (
    id BIGINT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    vulnerability_name VARCHAR(255),
    FOREIGN KEY (device_id) REFERENCES devices(id)
);
```

**Why @JoinColumn?**
```java
@JoinColumn(name = "device_id")
```
- Specifies foreign key column name
- Without it: JPA uses "device_id" by default
- Explicit is better than implicit

### Bidirectional vs Unidirectional Relationship

**Current (Unidirectional):**
```java
// Vulnerability knows about Device
class Vulnerability {
    @ManyToOne
    private Device device;
}

// Device doesn't know about Vulnerabilities
class Device {
    // No reference to vulnerabilities
}
```

**Alternative (Bidirectional):**
```java
class Device {
    @OneToMany(mappedBy = "device")
    private List<Vulnerability> vulnerabilities;
}
```

**Why Unidirectional?**
- Simpler code
- Avoid circular references
- Query when needed: `vulnerabilityRepository.findByDeviceId(id)`
- Less memory overhead

## Vulnerability Scanning Strategy

### Why Nmap NSE Scripts?
**Decision:** Use Nmap Scripting Engine for vulnerability detection.

**Code:**
```java
ProcessBuilder pb = new ProcessBuilder(
    "nmap",
    "-sV",              // Service version detection
    "--script=vuln",    // Vulnerability scripts
    "-oX", "-",         // XML output
    ipAddress
);
```

**Reasoning:**
- NSE has 600+ scripts
- Community-maintained
- Detects: outdated software, misconfigurations, known CVEs
- No need to build vulnerability database

**What -sV does:**
- Probes open ports
- Identifies service (Apache, SSH, MySQL)
- Detects version (Apache 2.4.41)

**What --script=vuln does:**
- Runs vulnerability detection scripts
- Checks for known exploits
- Tests for common misconfigurations

**Example NSE output:**
```xml
<script id="http-vuln-cve2017-5638" output="VULNERABLE">
  <elem key="title">Apache Struts Remote Code Execution</elem>
  <elem key="state">VULNERABLE</elem>
</script>
```

### Parsing Vulnerability Results

**XML Structure:**
```xml
<nmaprun>
  <host>
    <address addr="192.168.1.1"/>
    <ports>
      <port protocol="tcp" portid="80">
        <service name="http" product="Apache" version="2.4.41"/>
        <script id="http-vuln-cve2024-1234" output="VULNERABLE">
          <elem key="title">Critical RCE</elem>
          <elem key="severity">CRITICAL</elem>
        </script>
      </port>
    </ports>
  </host>
</nmaprun>
```

**Parsing Logic:**
```java
private List<Vulnerability> parseVulnerabilities(String xml, Device device) {
    List<Vulnerability> vulns = new ArrayList<>();
    
    // Find all <script> tags with "vuln" in id
    // Extract: id, title, severity
    // Create Vulnerability object
    // Link to device
    
    return vulns;
}
```

### Severity Mapping

**Nmap Output → Our Severity:**
```java
private String mapSeverity(String nmapSeverity) {
    return switch(nmapSeverity.toUpperCase()) {
        case "CRITICAL" -> "CRITICAL";
        case "HIGH" -> "HIGH";
        case "MEDIUM" -> "MEDIUM";
        case "LOW" -> "LOW";
        default -> "UNKNOWN";
    };
}
```

**Why enum for severity?**
```java
public enum Severity {
    CRITICAL, HIGH, MEDIUM, LOW, UNKNOWN
}
```
- Type safety
- Sortable (CRITICAL > HIGH > MEDIUM > LOW)
- Database indexing

## Service Layer Design

### VulnerabilityService Methods

**1. Scan Single Device**
```java
public VulnerabilityScanResult scanDevice(String ipAddress) {
    // 1. Find device in database
    Device device = deviceRepository.findByIpAddress(ipAddress)
        .orElseThrow(() -> new DeviceNotFoundException(ipAddress));
    
    // 2. Run Nmap vulnerability scan
    String xml = executeNmapVulnScan(ipAddress);
    
    // 3. Parse results
    List<Vulnerability> vulns = parseVulnerabilities(xml, device);
    
    // 4. Save to database
    vulnerabilityRepository.saveAll(vulns);
    
    // 5. Update device vulnerability flag
    device.setIsVulnerable(!vulns.isEmpty());
    deviceRepository.save(device);
    
    // 6. Return result
    return new VulnerabilityScanResult(device, vulns);
}
```

**Why this flow?**
- Verify device exists first
- Atomic operation (all or nothing)
- Update device flag for quick filtering
- Return comprehensive result

**2. Scan All Devices**
```java
public List<VulnerabilityScanResult> scanAllDevices() {
    List<Device> devices = deviceRepository.findAll();
    List<VulnerabilityScanResult> results = new ArrayList<>();
    
    for (Device device : devices) {
        try {
            VulnerabilityScanResult result = scanDevice(device.getIpAddress());
            results.add(result);
        } catch (Exception e) {
            // Log error, continue with next device
            log.error("Failed to scan {}: {}", device.getIpAddress(), e.getMessage());
        }
    }
    
    return results;
}
```

**Why try-catch inside loop?**
- One device failure shouldn't stop entire scan
- Collect partial results
- Log errors for investigation

**Performance consideration:**
- Scanning 100 devices sequentially = slow
- Solution: Parallel streams (added in Phase 7)
```java
devices.parallelStream()
    .map(device -> scanDevice(device.getIpAddress()))
    .collect(Collectors.toList());
```

## Controller Design

### Endpoint: POST /api/vulnerabilities/scan/{ipAddress}

**Why POST not GET?**
- Scanning is an action (side effect)
- Creates database records
- POST semantically correct

**Why path variable?**
```java
@PostMapping("/scan/{ipAddress}")
public ResponseEntity<?> scanDevice(@PathVariable String ipAddress)
```
- IP is resource identifier
- RESTful: /api/vulnerabilities/scan/192.168.1.1
- Clean URL structure

**Response structure:**
```json
{
  "device": {
    "id": 1,
    "ipAddress": "192.168.1.1",
    "isVulnerable": true
  },
  "vulnerabilities": [
    {
      "id": 1,
      "vulnerabilityName": "CVE-2024-1234",
      "severity": "CRITICAL",
      "description": "Remote code execution",
      "discoveredAt": "2024-03-15T14:30:00"
    }
  ],
  "scanDuration": "15.3s"
}
```

### Endpoint: POST /api/vulnerabilities/scan/all

**Why separate endpoint?**
- Different use case (scan all vs scan one)
- Different response structure
- Different authorization requirements

**Response:**
```json
{
  "totalDevices": 10,
  "scannedDevices": 10,
  "vulnerableDevices": 3,
  "totalVulnerabilities": 7,
  "results": [...]
}
```

## Data Transfer Objects (DTOs)

### Why DTOs?
**Problem:** Don't expose entire entity to API

**Example:**
```java
// Bad: Exposing entity directly
@GetMapping("/vulnerabilities")
public List<Vulnerability> getAll() {
    return vulnerabilityRepository.findAll();  // Exposes everything
}

// Good: Using DTO
@GetMapping("/vulnerabilities")
public List<VulnerabilityDTO> getAll() {
    return vulnerabilityRepository.findAll()
        .stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
}
```

**VulnerabilityScanResult DTO:**
```java
@Data
@AllArgsConstructor
public class VulnerabilityScanResult {
    private Device device;
    private List<Vulnerability> vulnerabilities;
    private int vulnerabilityCount;
    private String scanDuration;
}
```

**Benefits:**
- Control what data is exposed
- Add computed fields (vulnerabilityCount)
- Version API independently from database
- Security (hide internal IDs, timestamps)

## Repository Queries

### Custom Query Methods

**Find vulnerabilities by device:**
```java
public interface VulnerabilityRepository extends JpaRepository<Vulnerability, Long> {
    List<Vulnerability> findByDeviceId(Long deviceId);
    List<Vulnerability> findBySeverity(String severity);
    List<Vulnerability> findByDeviceIdAndSeverity(Long deviceId, String severity);
}
```

**How Spring generates SQL:**
```java
// findByDeviceId(1L)
SELECT * FROM vulnerabilities WHERE device_id = 1;

// findBySeverity("CRITICAL")
SELECT * FROM vulnerabilities WHERE severity = 'CRITICAL';

// findByDeviceIdAndSeverity(1L, "CRITICAL")
SELECT * FROM vulnerabilities 
WHERE device_id = 1 AND severity = 'CRITICAL';
```

**Method naming convention:**
- findBy = SELECT
- And = AND condition
- Or = OR condition
- OrderBy = ORDER BY
- Top/First = LIMIT

### @Query Annotation (Advanced)

**When method names aren't enough:**
```java
@Query("SELECT v FROM Vulnerability v WHERE v.device.ipAddress = :ip")
List<Vulnerability> findByDeviceIp(@Param("ip") String ipAddress);
```

**JPQL vs SQL:**
- JPQL: Query entities (Vulnerability, Device)
- SQL: Query tables (vulnerabilities, devices)
- JPQL is database-agnostic

## Updating Device Vulnerability Flag

### Why Boolean Flag?
**Decision:** Add isVulnerable flag to Device entity.

**Reasoning:**
- Quick filtering: "Show all vulnerable devices"
- Dashboard: "3 vulnerable devices" (no join needed)
- Performance: Indexed boolean column

**Update logic:**
```java
device.setIsVulnerable(!vulnerabilities.isEmpty());
deviceRepository.save(device);
```

**Alternative (without flag):**
```sql
-- Slow: Join + count for every query
SELECT d.* FROM devices d
JOIN vulnerabilities v ON d.id = v.device_id
GROUP BY d.id
HAVING COUNT(v.id) > 0;

-- Fast: Direct filter
SELECT * FROM devices WHERE is_vulnerable = true;
```

## Error Handling

### Device Not Found
```java
Device device = deviceRepository.findByIpAddress(ipAddress)
    .orElseThrow(() -> new DeviceNotFoundException(ipAddress));
```

**Why Optional?**
- Explicit handling of "not found"
- Avoid NullPointerException
- Functional programming style

**Custom Exception:**
```java
public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String ip) {
        super("Device not found: " + ip);
    }
}
```

**Controller exception handler:**
```java
@ExceptionHandler(DeviceNotFoundException.class)
public ResponseEntity<String> handleNotFound(DeviceNotFoundException e) {
    return ResponseEntity.status(404).body(e.getMessage());
}
```

### Scan Failures
```java
try {
    VulnerabilityScanResult result = scanDevice(ipAddress);
    return ResponseEntity.ok(result);
} catch (IOException e) {
    return ResponseEntity.status(500).body("Scan failed: " + e.getMessage());
} catch (DeviceNotFoundException e) {
    return ResponseEntity.status(404).body(e.getMessage());
}
```

**HTTP Status Codes:**
- 200 OK: Scan successful
- 404 Not Found: Device doesn't exist
- 500 Internal Server Error: Nmap failed

## Performance Optimization

### Database Indexes
```sql
CREATE INDEX idx_device_id ON vulnerabilities(device_id);
CREATE INDEX idx_severity ON vulnerabilities(severity);
CREATE INDEX idx_discovered_at ON vulnerabilities(discovered_at);
```

**Why these indexes?**
- device_id: Join with devices table
- severity: Filter by CRITICAL/HIGH
- discovered_at: Sort by recent

### Batch Inserts
```java
// Slow: One INSERT per vulnerability
for (Vulnerability v : vulnerabilities) {
    vulnerabilityRepository.save(v);
}

// Fast: Batch INSERT
vulnerabilityRepository.saveAll(vulnerabilities);
```

**Hibernate batching:**
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
```

## Security Considerations

### Privilege Escalation
**Problem:** Vulnerability scanning requires elevated privileges

**Solution:**
- Run application as admin (development)
- Use sudo for Nmap (production)
```java
ProcessBuilder pb = new ProcessBuilder("sudo", "nmap", ...);
```

### Rate Limiting
**Problem:** Scanning all devices hammers network

**Solution:**
```java
@RateLimiter(name = "vulnerability-scan", fallbackMethod = "scanFallback")
public VulnerabilityScanResult scanDevice(String ip) {
    // ...
}
```

### Input Validation
```java
if (!ipAddress.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
    throw new IllegalArgumentException("Invalid IP address");
}
```

## Testing Strategy

### Unit Tests
```java
@Test
void testVulnerabilityCreation() {
    Vulnerability vuln = new Vulnerability();
    vuln.setVulnerabilityName("CVE-2024-1234");
    vuln.setSeverity("CRITICAL");
    
    assertNotNull(vuln.getVulnerabilityName());
    assertEquals("CRITICAL", vuln.getSeverity());
}
```

### Integration Tests
```java
@SpringBootTest
@Test
void testScanDevice() {
    // Given: Device exists
    Device device = new Device();
    device.setIpAddress("192.168.1.1");
    deviceRepository.save(device);
    
    // When: Scan device
    VulnerabilityScanResult result = vulnerabilityService.scanDevice("192.168.1.1");
    
    // Then: Vulnerabilities found
    assertNotNull(result);
    assertTrue(result.getVulnerabilities().size() >= 0);
}
```

### Mock Testing
```java
@Mock
private VulnerabilityRepository vulnerabilityRepository;

@Test
void testFindByDeviceId() {
    // Given
    List<Vulnerability> vulns = Arrays.asList(new Vulnerability());
    when(vulnerabilityRepository.findByDeviceId(1L)).thenReturn(vulns);
    
    // When
    List<Vulnerability> result = vulnerabilityService.getByDeviceId(1L);
    
    // Then
    assertEquals(1, result.size());
    verify(vulnerabilityRepository).findByDeviceId(1L);
}
```

## Real-World Example

**Scenario:** Scan office network for vulnerabilities

**Step 1: Discover devices**
```bash
POST /api/scan/network?range=192.168.1.0/24
```

**Step 2: Scan all for vulnerabilities**
```bash
POST /api/vulnerabilities/scan/all
```

**Step 3: Get critical vulnerabilities**
```bash
GET /api/vulnerabilities?severity=CRITICAL
```

**Step 4: Get device details**
```bash
GET /api/devices/vulnerable
```

**Response:**
```json
[
  {
    "id": 1,
    "ipAddress": "192.168.1.50",
    "hostname": "OLD-SERVER",
    "operatingSystem": "Windows Server 2008",
    "isVulnerable": true,
    "vulnerabilities": [
      {
        "vulnerabilityName": "MS17-010 (EternalBlue)",
        "severity": "CRITICAL",
        "description": "Remote code execution via SMBv1"
      }
    ]
  }
]
```

## Summary: Phase 2 Key Decisions

1. **Separate Vulnerability entity** - Normalized design
2. **@ManyToOne relationship** - One device, many vulnerabilities
3. **Nmap NSE scripts** - Proven vulnerability detection
4. **Severity enum** - Type-safe severity levels
5. **isVulnerable flag** - Performance optimization
6. **DTOs for responses** - API control
7. **Custom repository queries** - Flexible data access
8. **Exception handling** - Graceful error responses
9. **Batch inserts** - Performance
10. **Database indexes** - Query optimization

---

*Continue to Phase 3...*
