# Network Watcher - Complete Technical Deep Dive

## Table of Contents
1. Phase 1: Network Discovery
2. Phase 2: Vulnerability Assessment
3. Phase 3: Bandwidth Monitoring
4. Phase 4: Packet Analysis
5. Phase 5: Device Control
6. Phase 6: Reporting
7. Phase 7: Authentication & Enterprise

---

# PHASE 1: NETWORK DISCOVERY

## Overview
Network discovery is the foundation. We need to find all devices on the network and collect their details.

## Architecture Decisions

### Why Nmap?
**Decision:** Use Nmap as the scanning engine instead of building from scratch.

**Reasoning:**
- Nmap is industry-standard (used by security professionals worldwide)
- 25+ years of development and testing
- Built-in OS fingerprinting database
- MAC vendor lookup (OUI database)
- Handles edge cases we'd miss (firewalls, timeouts, etc.)

**Alternative Considered:**
- Pure Java network scanning (InetAddress.isReachable())
- **Rejected because:** Too basic, no OS detection, no MAC vendor lookup

### Why ProcessBuilder?
**Decision:** Execute Nmap as external process via ProcessBuilder.

**Code:**
```java
ProcessBuilder pb = new ProcessBuilder("nmap", "-sn", "-oX", "-", range);
Process process = pb.start();
```

**Reasoning:**
- Nmap is a native binary (C/C++), not Java
- ProcessBuilder is Java's way to run external programs
- We capture stdout to get XML results

**How it works:**
1. ProcessBuilder creates new OS process
2. Nmap runs independently
3. We read its output stream
4. Parse XML results

### Why XML Output Format?
**Decision:** Use `-oX -` flag for XML output.

**Reasoning:**
- Structured data (easy to parse)
- Contains all fields we need
- More reliable than parsing text output
- `-` means output to stdout (not file)

**Alternative Considered:**
- Text output parsing
- **Rejected because:** Fragile, format changes break code

## Entity Design: Device

### Why JPA Entity?
**Decision:** Use JPA (Java Persistence API) for database mapping.

**Code:**
```java
@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ...
}
```

**Reasoning:**
- Automatic table creation (no SQL needed)
- Object-Relational Mapping (work with objects, not SQL)
- Database-agnostic (works with H2, MySQL, PostgreSQL)
- Built-in CRUD operations

**What happens:**
1. Spring Boot sees @Entity
2. Hibernate (JPA implementation) creates table
3. Table name: "devices"
4. Columns match field names

### Why Lombok?
**Decision:** Use Lombok annotations for boilerplate code.

**Code:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
```

**Reasoning:**
- @Data generates: getters, setters, toString, equals, hashCode
- Saves ~100 lines of repetitive code
- Cleaner, more readable code
- Compile-time code generation (no runtime overhead)

**Without Lombok:**
```java
public String getIpAddress() { return ipAddress; }
public void setIpAddress(String ip) { this.ipAddress = ip; }
// ... 50 more methods
```

**With Lombok:**
```java
@Data  // Done!
```

### Field Design Decisions

#### IP Address Field
```java
@Column(nullable = false, unique = true, length = 45)
private String ipAddress;
```

**Why nullable = false?**
- Every device MUST have an IP
- Database enforces this (NOT NULL constraint)
- Prevents invalid data

**Why unique = true?**
- One IP = One device
- Database creates UNIQUE index
- Prevents duplicate entries
- Fast lookups by IP

**Why length = 45?**
- IPv4: "255.255.255.255" = 15 chars
- IPv6: "2001:0db8:85a3:0000:0000:8a2e:0370:7334" = 39 chars
- 45 gives buffer for both

#### MAC Address Field
```java
@Column(length = 17)
private String macAddress;
```

**Why 17 characters?**
- Format: "AA:BB:CC:DD:EE:FF"
- 12 hex digits + 5 colons = 17 chars

**Why not unique?**
- MAC can be spoofed
- Virtual machines share MACs
- Not reliable as primary key

#### Status Enum
```java
@Enumerated(EnumType.STRING)
@Column(length = 20)
private DeviceStatus status = DeviceStatus.ONLINE;

public enum DeviceStatus {
    ONLINE, OFFLINE, UNKNOWN
}
```

**Why enum not String?**
- Type safety: compiler prevents typos
- IDE autocomplete
- Refactoring-safe

**Why EnumType.STRING?**
- Database stores: "ONLINE", "OFFLINE", "UNKNOWN"
- Alternative: EnumType.ORDINAL stores 0, 1, 2
- **Problem with ORDINAL:** If you reorder enum, data breaks!

**Example of ORDINAL problem:**
```java
// Original
enum Status { ONLINE, OFFLINE }  // ONLINE=0, OFFLINE=1

// Later you add UNKNOWN at start
enum Status { UNKNOWN, ONLINE, OFFLINE }  // UNKNOWN=0, ONLINE=1, OFFLINE=2

// Database has 0 (was ONLINE, now UNKNOWN) - DATA CORRUPTION!
```

#### Timestamp Fields
```java
@Column(name = "first_seen", updatable = false)
private LocalDateTime firstSeen;

@Column(name = "last_seen")
private LocalDateTime lastSeen;
```

**Why LocalDateTime not Date?**
- Date is legacy (Java 1.0)
- LocalDateTime is modern (Java 8+)
- No timezone issues
- Better API

**Why updatable = false on firstSeen?**
- Historical record: when device FIRST appeared
- Should NEVER change
- JPA ignores updates to this field

**Why snake_case in database?**
```java
@Column(name = "first_seen")  // Database: first_seen
private LocalDateTime firstSeen;  // Java: firstSeen
```
- SQL convention: snake_case
- Java convention: camelCase
- @Column(name) bridges the gap

### Lifecycle Callback: @PrePersist
```java
@PrePersist
protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    this.firstSeen = now;
    this.lastSeen = now;
}
```

**What is @PrePersist?**
- JPA lifecycle hook
- Runs BEFORE INSERT (not UPDATE)
- Automatic timestamp initialization

**When it runs:**
```java
Device device = new Device();
device.setIpAddress("192.168.1.1");
deviceRepository.save(device);  // @PrePersist runs HERE
// firstSeen and lastSeen are now set automatically
```

**Why this pattern?**
- Prevents forgetting to set timestamps
- Consistent across entire app
- Single source of truth

## Repository Layer

### Why Spring Data JPA?
**Decision:** Use Spring Data JPA repositories.

**Code:**
```java
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByIpAddress(String ipAddress);
    List<Device> findByStatus(Device.DeviceStatus status);
}
```

**Reasoning:**
- Zero implementation code needed
- Spring generates implementation at runtime
- Query methods from method names
- Built-in pagination, sorting

**How it works:**
1. Spring sees method name: findByIpAddress
2. Parses: "find" + "By" + "IpAddress"
3. Generates SQL: SELECT * FROM devices WHERE ip_address = ?
4. Returns result

**Magic method naming:**
- findBy = SELECT
- IpAddress = field name
- Spring converts camelCase to snake_case

## Service Layer

### Why Service Layer?
**Decision:** Separate business logic from controllers.

**Architecture:**
```
Controller → Service → Repository → Database
```

**Reasoning:**
- Single Responsibility Principle
- Reusable business logic
- Easier testing
- Transaction management

### NetworkScanService Deep Dive

**Method: scanNetwork(String range)**
```java
public ScanResult scanNetwork(String range) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("nmap", "-sn", "-oX", "-", range);
    Process process = pb.start();
    
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream())
    );
    
    StringBuilder xmlOutput = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
        xmlOutput.append(line);
    }
    
    return parseNmapXml(xmlOutput.toString());
}
```

**Step-by-step execution:**

1. **Create ProcessBuilder**
   - Command: `nmap -sn -oX - 192.168.1.0/24`
   - `-sn`: Ping scan (no port scan)
   - `-oX -`: XML output to stdout
   - `192.168.1.0/24`: CIDR range

2. **Start Process**
   - OS creates new process
   - Nmap runs independently
   - Java waits for output

3. **Read Output Stream**
   - process.getInputStream() = Nmap's stdout
   - BufferedReader for efficient reading
   - StringBuilder accumulates XML

4. **Parse XML**
   - Extract host information
   - Create Device objects
   - Save to database

**XML Parsing:**
```java
private ScanResult parseNmapXml(String xml) {
    // XML structure:
    // <host>
    //   <address addr="192.168.1.1" addrtype="ipv4"/>
    //   <address addr="AA:BB:CC:DD:EE:FF" addrtype="mac" vendor="Apple"/>
    //   <hostnames>
    //     <hostname name="router.local"/>
    //   </hostnames>
    // </host>
    
    // Extract each field
    // Create Device object
    // Save to database
}
```

## Controller Layer

### Why REST Controllers?
**Decision:** Use @RestController for API endpoints.

**Code:**
```java
@RestController
@RequestMapping("/api/scan")
public class ScanController {
    @PostMapping("/network")
    public ResponseEntity<ScanResult> scanNetwork(@RequestParam String range) {
        // ...
    }
}
```

**Reasoning:**
- RESTful API design
- JSON responses (automatic serialization)
- HTTP status codes
- Stateless (no sessions)

**@RestController vs @Controller:**
- @Controller: Returns views (HTML)
- @RestController: Returns data (JSON)
- @RestController = @Controller + @ResponseBody

### Endpoint Design

**POST /api/scan/network?range=192.168.1.0/24**

**Why POST not GET?**
- Scanning is an action (not just reading data)
- POST semantically correct for operations
- GET should be idempotent (safe to repeat)

**Why query parameter?**
```java
@PostMapping("/network")
public ResponseEntity<ScanResult> scanNetwork(@RequestParam String range)
```
- Simple parameter
- URL: /api/scan/network?range=192.168.1.0/24
- Alternative: @PathVariable for /api/scan/network/192.168.1.0/24
- Query param better for optional parameters

**Why ResponseEntity?**
```java
return ResponseEntity.ok(result);  // 200 OK
return ResponseEntity.status(500).body("Error");  // 500 Error
```
- Control HTTP status code
- Add headers if needed
- Flexible response handling

## Data Flow Example

**Complete flow of scanning network:**

1. **HTTP Request**
   ```
   POST /api/scan/network?range=192.168.1.0/24
   ```

2. **Controller receives request**
   ```java
   @PostMapping("/network")
   public ResponseEntity<ScanResult> scanNetwork(@RequestParam String range)
   ```

3. **Controller calls Service**
   ```java
   ScanResult result = networkScanService.scanNetwork(range);
   ```

4. **Service executes Nmap**
   ```java
   ProcessBuilder pb = new ProcessBuilder("nmap", "-sn", "-oX", "-", range);
   ```

5. **Nmap scans network**
   - Sends ARP requests
   - Collects responses
   - Outputs XML

6. **Service parses XML**
   ```java
   List<Device> devices = parseNmapXml(xml);
   ```

7. **Service saves to database**
   ```java
   for (Device device : devices) {
       deviceRepository.save(device);
   }
   ```

8. **Service returns result**
   ```java
   return new ScanResult(devices.size(), devices);
   ```

9. **Controller returns JSON**
   ```json
   {
     "devicesFound": 5,
     "devices": [...]
   }
   ```

## Database Schema Generated

**From Device entity, Hibernate creates:**

```sql
CREATE TABLE devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(45) NOT NULL UNIQUE,
    mac_address VARCHAR(17),
    mac_vendor VARCHAR(100),
    hostname VARCHAR(255),
    operating_system VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    first_seen TIMESTAMP NOT NULL,
    last_seen TIMESTAMP,
    is_vulnerable BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_ip ON devices(ip_address);
CREATE INDEX idx_status ON devices(status);
```

**Why these indexes?**
- ip_address: Frequent lookups by IP
- status: Filter by ONLINE/OFFLINE
- Indexes speed up queries

## Error Handling

### Why try-catch?
```java
try {
    ScanResult result = networkScanService.scanNetwork(range);
    return ResponseEntity.ok(result);
} catch (IOException e) {
    return ResponseEntity.status(500).body("Scan failed: " + e.getMessage());
}
```

**Reasoning:**
- Nmap might not be installed
- Network might be unreachable
- Invalid CIDR range
- Graceful error responses

### Exception Hierarchy
```
Exception
  └─ IOException (checked)
       └─ Nmap execution fails
```

**Checked vs Unchecked:**
- IOException is checked (must handle)
- RuntimeException is unchecked (optional)
- Network operations = checked exceptions

## Performance Considerations

### Why async scanning?
**Problem:** Scanning 254 IPs takes time (30+ seconds)

**Solution:** Return immediately, scan in background
```java
@Async
public CompletableFuture<ScanResult> scanNetworkAsync(String range) {
    // Scan runs in separate thread
    // Controller returns immediately
}
```

**Not implemented in Phase 1 because:**
- Keeping it simple for MVP
- Added in Phase 7 with scan orchestration

### Why pagination?
**Problem:** 1000+ devices = huge JSON response

**Solution:** Spring Data JPA pagination
```java
Page<Device> findAll(Pageable pageable);
```

**Usage:**
```java
Pageable pageable = PageRequest.of(0, 20);  // Page 0, 20 items
Page<Device> page = deviceRepository.findAll(pageable);
```

## Testing Strategy

### Unit Tests
```java
@Test
void testDeviceCreation() {
    Device device = new Device();
    device.setIpAddress("192.168.1.1");
    assertNotNull(device.getIpAddress());
}
```

### Integration Tests
```java
@SpringBootTest
@Test
void testScanNetwork() {
    ScanResult result = scanController.scanNetwork("192.168.1.0/24");
    assertTrue(result.getDevicesFound() > 0);
}
```

## Security Considerations

### Input Validation
**Problem:** User provides malicious input

**Example attack:**
```
range = "192.168.1.0/24; rm -rf /"
```

**Solution:** Validate CIDR format
```java
if (!range.matches("\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+")) {
    throw new IllegalArgumentException("Invalid CIDR");
}
```

### Command Injection Prevention
**Problem:** ProcessBuilder with user input

**Safe approach:**
```java
// Safe: separate arguments
new ProcessBuilder("nmap", "-sn", range);

// Unsafe: shell command
new ProcessBuilder("sh", "-c", "nmap -sn " + range);  // DON'T DO THIS
```

## Summary: Phase 1 Key Decisions

1. **Nmap for scanning** - Industry standard, proven
2. **JPA for persistence** - Object-relational mapping
3. **Lombok for boilerplate** - Clean code
4. **Spring Data JPA** - Zero-code repositories
5. **REST API** - Stateless, JSON responses
6. **Service layer** - Business logic separation
7. **Enum for status** - Type safety
8. **LocalDateTime** - Modern date/time
9. **@PrePersist** - Automatic timestamps
10. **ProcessBuilder** - External process execution

---

*Continue to Phase 2...*
