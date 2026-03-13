# PHASE 4: PACKET ANALYSIS - TECHNICAL DEEP DIVE

## Overview
Capture and analyze HTTP packets to extract important metadata (URLs, methods, headers) for security monitoring.

## Architecture Decisions

### Why Pcap4J?
**Decision:** Use Pcap4J library for packet capture.

**Reasoning:**
- Java wrapper for libpcap/WinPcap
- Cross-platform (Windows/Linux/Mac)
- Low-level packet access
- Industry-standard packet capture

**Alternative Considered:**
- Raw sockets in Java
- **Rejected:** Too low-level, complex, platform-specific

**Dependency:**
```xml
<dependency>
    <groupId>org.pcap4j</groupId>
    <artifactId>pcap4j-core</artifactId>
    <version>1.8.2</version>
</dependency>
```

### Why In-Memory Storage?
**Decision:** Store packets in CopyOnWriteArrayList, not database.

**Code:**
```java
private final List<CapturedPacket> capturedPackets = new CopyOnWriteArrayList<>();
private final List<HttpPacketDetails> httpPackets = new CopyOnWriteArrayList<>();
```

**Reasoning:**
- Packets are transient (temporary analysis)
- High volume (thousands per second)
- Database would be overwhelmed
- Memory is faster
- Limit to last 100 packets

**Why CopyOnWriteArrayList?**
- Thread-safe (multiple threads reading/writing)
- No ConcurrentModificationException
- Optimized for read-heavy workloads
- Packet capture thread writes, API reads

**Alternative:**
```java
// Not thread-safe
private final List<CapturedPacket> packets = new ArrayList<>();

// Thread-safe but slower
private final List<CapturedPacket> packets = 
    Collections.synchronizedList(new ArrayList<>());
```

### Entity Design: CapturedPacket

**Why not @Entity?**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapturedPacket {  // No @Entity
    private String sourceIp;
    private String destinationIp;
    private String protocol;
    private int length;
    private String info;
    private LocalDateTime timestamp;
}
```

**Reasoning:**
- POJO (Plain Old Java Object)
- No database persistence
- Lightweight
- Fast creation

**If it were an entity:**
```java
@Entity  // Would create table
public class CapturedPacket {
    @Id
    @GeneratedValue
    private Long id;  // Unnecessary overhead
    // ...
}
```

## Packet Capture Architecture

### Pcap4J Workflow

**1. Find Network Interface**
```java
PcapNetworkInterface nif = Pcaps.getDevByAddress(
    InetAddress.getByName(interfaceIp)
);
```

**What this does:**
- Lists all network interfaces
- Finds one matching IP address
- Returns interface handle

**Example interfaces:**
```
eth0: 192.168.1.100
wlan0: 192.168.1.101
lo: 127.0.0.1
```

**2. Open Interface for Capture**
```java
int snapLen = 65536;  // Max bytes per packet
PcapNetworkInterface.PromiscuousMode mode = 
    PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;
int timeout = 10;  // Read timeout (ms)

PcapHandle handle = nif.openLive(snapLen, mode, timeout);
```

**Why snapLen = 65536?**
- Maximum IP packet size
- Captures entire packet
- Smaller value truncates data

**Why PROMISCUOUS mode?**
- Captures ALL packets on network
- Not just packets destined for this machine
- Required for network monitoring

**Normal mode:**
```
Your PC: 192.168.1.100
Packet: 192.168.1.50 → 192.168.1.60
Result: Ignored (not for you)
```

**Promiscuous mode:**
```
Your PC: 192.168.1.100
Packet: 192.168.1.50 → 192.168.1.60
Result: Captured (monitoring all traffic)
```

**3. Set Packet Filter**
```java
handle.setFilter("tcp", BpfProgram.BpfCompileMode.OPTIMIZE);
```

**BPF (Berkeley Packet Filter):**
- Kernel-level filtering
- Extremely fast
- Reduces packets to process

**Filter examples:**
```
"tcp"                    // All TCP packets
"tcp port 80"            // HTTP traffic
"tcp port 443"           // HTTPS traffic
"host 192.168.1.1"       // Specific host
"tcp and port 80"        // TCP on port 80
```

**Why filter at kernel level?**
- Millions of packets per second
- Filtering in Java = too slow
- Kernel filters before copying to userspace

**4. Start Capture Loop**
```java
handle.loop(-1, (PacketListener) packet -> {
    processPacket(packet);
});
```

**What loop(-1) means:**
- -1 = infinite loop
- Captures until stopped
- Callback for each packet

**Packet flow:**
```
Network → Kernel → BPF Filter → Pcap4J → Java Callback
```

### Threading Model

**Why separate thread?**
```java
new Thread(() -> {
    try {
        handle.loop(-1, packet -> processPacket(packet));
    } catch (Exception e) {
        e.printStackTrace();
    }
}).start();
```

**Reasoning:**
- Packet capture blocks
- Don't block HTTP request thread
- Return immediately to client
- Capture runs in background

**Thread lifecycle:**
```
1. Client: POST /api/packets/start
2. Controller: Start capture thread
3. Controller: Return 200 OK immediately
4. Background: Capture packets continuously
5. Client: GET /api/packets (retrieve captured)
6. Client: POST /api/packets/stop
7. Background: Stop capture thread
```

## Packet Processing

### Extracting IP Information

**Code:**
```java
if (packet.contains(IpV4Packet.class)) {
    IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
    String srcIp = ipV4Packet.getHeader().getSrcAddr().getHostAddress();
    String dstIp = ipV4Packet.getHeader().getDstAddr().getHostAddress();
}
```

**Packet structure:**
```
[Ethernet Header]
  [IP Header]
    [TCP Header]
      [HTTP Data]
```

**Why contains() check?**
- Not all packets have IP layer
- Could be ARP, ICMP, etc.
- Prevents NullPointerException

### Extracting TCP Information

**Code:**
```java
if (packet.contains(TcpPacket.class)) {
    TcpPacket tcpPacket = packet.get(TcpPacket.class);
    int srcPort = tcpPacket.getHeader().getSrcPort().valueAsInt();
    int dstPort = tcpPacket.getHeader().getDstPort().valueAsInt();
}
```

**TCP Header fields:**
- Source port (16 bits)
- Destination port (16 bits)
- Sequence number
- Acknowledgment number
- Flags (SYN, ACK, FIN, etc.)

**Port identification:**
```
Port 80  = HTTP
Port 443 = HTTPS
Port 22  = SSH
Port 3306 = MySQL
```

### HTTP Detection and Parsing

**Detection:**
```java
if (tcpPacket.getPayload() != null) {
    String payload = new String(tcpPacket.getPayload().getRawData());
    if (payload.startsWith("GET") || 
        payload.startsWith("POST") || 
        payload.startsWith("PUT") || 
        payload.startsWith("DELETE")) {
        // This is HTTP!
        analyzeHttpPacket(payload, srcIp, dstIp);
    }
}
```

**Why check payload start?**
- HTTP is text-based protocol
- Requests start with method
- Simple but effective detection

**HTTP Request structure:**
```
GET /api/users HTTP/1.1
Host: example.com
User-Agent: Mozilla/5.0
Accept: application/json

```

**Parsing:**
```java
private void analyzeHttpPacket(String payload, String srcIp, String dstIp) {
    String[] lines = payload.split("\\r\\n");
    
    // First line: GET /api/users HTTP/1.1
    String requestLine = lines[0];
    String[] parts = requestLine.split(" ");
    String method = parts[0];  // GET
    String url = parts[1];     // /api/users
    
    // Parse headers
    String host = null;
    String userAgent = null;
    
    for (String line : lines) {
        if (line.startsWith("Host: ")) {
            host = line.substring(6).trim();
        } else if (line.startsWith("User-Agent: ")) {
            userAgent = line.substring(12).trim();
        }
    }
}
```

**Why split by \\r\\n?**
- HTTP uses CRLF (Carriage Return + Line Feed)
- Windows line ending
- HTTP standard (RFC 2616)

## HttpPacketDetails DTO

**Design:**
```java
@Data
@AllArgsConstructor
public class HttpPacketDetails {
    private String method;        // GET, POST, PUT, DELETE
    private String url;           // /api/users
    private String host;          // example.com
    private String userAgent;     // Mozilla/5.0...
    private String contentType;   // application/json
    private String sourceIp;      // 192.168.1.100
    private String destinationIp; // 93.184.216.34
    private LocalDateTime timestamp;
}
```

**Why separate from CapturedPacket?**
- CapturedPacket = all packets (TCP, UDP, etc.)
- HttpPacketDetails = HTTP-specific metadata
- Different use cases
- Cleaner separation

**Storage limit:**
```java
if (httpPackets.size() >= 50) {
    httpPackets.remove(0);  // Remove oldest
}
httpPackets.add(details);
```

**Why limit to 50?**
- Memory management
- Most recent packets most relevant
- Circular buffer pattern

## Service Layer

### PacketService Methods

**1. Start Capturing**
```java
public void startCapturing(String interfaceIp) 
    throws PcapNativeException, NotOpenException, UnknownHostException {
    
    if (isCapturing) {
        return;  // Already capturing
    }
    
    // Find interface
    PcapNetworkInterface nif = Pcaps.getDevByAddress(
        InetAddress.getByName(interfaceIp)
    );
    
    // Open for capture
    handle = nif.openLive(65536, PromiscuousMode.PROMISCUOUS, 10);
    handle.setFilter("tcp", BpfCompileMode.OPTIMIZE);
    
    isCapturing = true;
    
    // Start capture thread
    new Thread(() -> {
        try {
            handle.loop(-1, packet -> processPacket(packet));
        } catch (Exception e) {
            isCapturing = false;
        }
    }).start();
}
```

**Why check isCapturing?**
- Prevent multiple capture threads
- Resource leak prevention
- Only one capture per interface

**2. Stop Capturing**
```java
public void stopCapturing() {
    isCapturing = false;
    if (handle != null && handle.isOpen()) {
        try {
            handle.breakLoop();  // Stop capture loop
            handle.close();      // Release resources
        } catch (NotOpenException e) {
            e.printStackTrace();
        }
    }
}
```

**Why breakLoop()?**
- Graceful shutdown
- Exits loop(-1) call
- Allows thread to finish

**3. Get Captured Packets**
```java
public List<CapturedPacket> getCapturedPackets() {
    return new ArrayList<>(capturedPackets);
}
```

**Why new ArrayList?**
- Return copy, not original
- Prevents external modification
- Thread-safety

**4. Get HTTP Packets**
```java
public List<HttpPacketDetails> getHttpPackets() {
    return new ArrayList<>(httpPackets);
}
```

## Controller Design

### Endpoint: POST /api/packets/start

**Request:**
```bash
POST /api/packets/start?interfaceIp=192.168.1.100
```

**Response:**
```json
{
  "message": "Packet capturing started on 192.168.1.100",
  "status": "CAPTURING"
}
```

**Error handling:**
```java
try {
    packetService.startCapturing(interfaceIp);
    return ResponseEntity.ok("Capturing started");
} catch (PcapNativeException e) {
    return ResponseEntity.status(500)
        .body("Pcap error: " + e.getMessage());
} catch (UnknownHostException e) {
    return ResponseEntity.status(400)
        .body("Invalid IP: " + e.getMessage());
}
```

### Endpoint: GET /api/packets/http

**Response:**
```json
[
  {
    "method": "GET",
    "url": "/api/users",
    "host": "example.com",
    "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
    "contentType": null,
    "sourceIp": "192.168.1.100",
    "destinationIp": "93.184.216.34",
    "timestamp": "2024-03-15T14:30:00"
  }
]
```

**Use cases:**
- Security monitoring
- API usage tracking
- Debugging network issues
- Compliance auditing

## Security and Privacy

### Why Only HTTP (Not HTTPS)?
**HTTPS is encrypted:**
```
HTTP:  GET /api/users → Visible
HTTPS: ��#$%^&*()_+ → Encrypted
```

**What we can see in HTTPS:**
- Source/destination IP
- Port numbers
- Packet size
- Timing
- **Cannot see:** URL, headers, body

**Ethical considerations:**
- Only capture on your own network
- Inform users of monitoring
- Comply with privacy laws
- Don't capture passwords

### Data Minimization

**What we DON'T capture:**
- Packet payload (full body)
- Passwords
- Credit card numbers
- Personal data

**What we DO capture:**
- Metadata only
- HTTP method
- URL path
- Headers (non-sensitive)

**Redaction example:**
```java
private String redactSensitiveData(String url) {
    // /api/users/123/password → /api/users/[REDACTED]/password
    return url.replaceAll("/\\d+/", "/[REDACTED]/");
}
```

## Performance Considerations

### Memory Management

**Problem:** Unlimited packet storage = OutOfMemoryError

**Solution: Circular buffer**
```java
if (capturedPackets.size() >= 100) {
    capturedPackets.remove(0);  // Remove oldest
}
capturedPackets.add(packet);
```

**Memory calculation:**
```
100 packets × 1KB per packet = 100KB
50 HTTP packets × 2KB = 100KB
Total: ~200KB (negligible)
```

### CPU Usage

**Problem:** Processing every packet is CPU-intensive

**Solution: BPF filtering**
```java
handle.setFilter("tcp port 80 or tcp port 8080", ...);
```

**Impact:**
- Without filter: 100,000 packets/sec
- With filter: 1,000 packets/sec
- 99% reduction in processing

### Thread Safety

**CopyOnWriteArrayList characteristics:**
- Write: Creates copy of array
- Read: No locking
- Trade-off: Slow writes, fast reads

**Perfect for our use case:**
- Capture thread: Occasional writes
- API requests: Frequent reads

## Testing Strategy

### Unit Tests
```java
@Test
void testHttpDetection() {
    String payload = "GET /api/users HTTP/1.1\r\nHost: example.com\r\n";
    boolean isHttp = payload.startsWith("GET");
    assertTrue(isHttp);
}
```

### Integration Tests
```java
@SpringBootTest
@Test
void testPacketCapture() throws Exception {
    packetService.startCapturing("192.168.1.100");
    Thread.sleep(5000);  // Capture for 5 seconds
    
    List<CapturedPacket> packets = packetService.getCapturedPackets();
    assertFalse(packets.isEmpty());
    
    packetService.stopCapturing();
}
```

### Mock Tests
```java
@Test
void testHttpParsing() {
    String mockPayload = "GET /test HTTP/1.1\r\nHost: test.com\r\n";
    HttpPacketDetails details = parseHttp(mockPayload);
    
    assertEquals("GET", details.getMethod());
    assertEquals("/test", details.getUrl());
    assertEquals("test.com", details.getHost());
}
```

## Real-World Use Cases

### 1. API Usage Monitoring
```
Track which APIs are being called:
- GET /api/users (50 times)
- POST /api/orders (20 times)
- GET /api/products (100 times)
```

### 2. Security Monitoring
```
Detect suspicious patterns:
- SQL injection attempts: /api/users?id=1' OR '1'='1
- Path traversal: /api/files?path=../../etc/passwd
- Unusual user agents: sqlmap/1.0
```

### 3. Performance Analysis
```
Identify slow endpoints:
- /api/reports (5 seconds response time)
- /api/search (2 seconds response time)
```

### 4. Compliance Auditing
```
Verify HTTPS usage:
- HTTP traffic: 5% (should be 0%)
- HTTPS traffic: 95%
```

## Summary: Phase 4 Key Decisions

1. **Pcap4J for capture** - Industry standard, cross-platform
2. **In-memory storage** - Fast, transient data
3. **CopyOnWriteArrayList** - Thread-safe, read-optimized
4. **BPF filtering** - Kernel-level performance
5. **Promiscuous mode** - Capture all network traffic
6. **Separate thread** - Non-blocking capture
7. **HTTP-only analysis** - HTTPS is encrypted
8. **Circular buffer** - Memory management
9. **Metadata only** - Privacy and security
10. **Limit to 50/100 packets** - Resource management

---

*Continue to Phase 5...*
