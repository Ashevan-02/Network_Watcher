# PHASES 5-7: DEVICE CONTROL, REPORTING & AUTHENTICATION - TECHNICAL DEEP DIVE

## PHASE 5: DEVICE CONTROL

### Overview
Block/unblock devices from network access using Windows Firewall rules.

### Architecture Decision: Windows Firewall Integration

**Why Windows Firewall?**
```java
String command = String.format(
    "netsh advfirewall firewall add rule name=\"Block_%s\" " +
    "dir=in action=block remoteip=%s",
    ipAddress.replace(".", "_"), ipAddress
);
```

**Reasoning:**
- Built into Windows (no installation)
- Immediate effect
- Persistent across reboots
- Centralized management

**Alternative Considered:**
- Router/switch API (MikroTik, UniFi)
- **Rejected:** Requires specific hardware, complex setup

### netsh Command Breakdown

**Block device:**
```cmd
netsh advfirewall firewall add rule 
  name="Block_192_168_1_50" 
  dir=in 
  action=block 
  remoteip=192.168.1.50
```

**Parameters:**
- `name`: Rule identifier (must be unique)
- `dir=in`: Inbound traffic
- `action=block`: Drop packets
- `remoteip`: Source IP to block

**Why dir=in?**
- Blocks traffic FROM that IP
- Prevents device from accessing this machine
- Could add dir=out to block both directions

**Unblock device:**
```cmd
netsh advfirewall firewall delete rule name="Block_192_168_1_50"
```

### Service Implementation

**DeviceControlService:**
```java
public String disconnectDevice(String ipAddress) {
    try {
        // 1. Verify device exists
        Device device = deviceService.getDeviceByIp(ipAddress)
            .orElseThrow(() -> new DeviceNotFoundException(ipAddress));
        
        // 2. Create firewall rule
        String command = String.format(
            "netsh advfirewall firewall add rule name=\"Block_%s\" " +
            "dir=in action=block remoteip=%s",
            ipAddress.replace(".", "_"), ipAddress
        );
        
        // 3. Execute command
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        // 4. Update device status
        if (exitCode == 0) {
            device.setStatus(Device.DeviceStatus.OFFLINE);
            deviceService.updateDevice(device.getId(), device);
            
            // 5. Audit log
            auditLogService.log(
                "system", 
                "DISCONNECT_DEVICE", 
                ipAddress, 
                "Device blocked via firewall", 
                null
            );
            
            return "Device blocked successfully";
        }
    } catch (Exception e) {
        return "Error: " + e.getMessage();
    }
}
```

**Why replace dots with underscores?**
```java
ipAddress.replace(".", "_")
// 192.168.1.50 → 192_168_1_50
```
- Windows rule names can't contain dots
- Underscores are valid
- Easy to identify rules

### Security Considerations

**Administrator Privileges Required:**
```
Error: Access is denied
```

**Why?**
- Firewall modification requires admin rights
- Security feature (prevents malware)
- Must run application as administrator

**Production solution:**
```java
// Run with elevated privileges
ProcessBuilder pb = new ProcessBuilder("runas", "/user:Administrator", command);
```

**Or use Windows Service:**
- Run as SYSTEM account
- Always has admin rights
- Recommended for production

### Audit Trail Integration

**Why audit logging?**
- Compliance requirement
- Track who blocked what
- Investigate incidents
- Legal evidence

**Audit log entry:**
```json
{
  "username": "admin",
  "action": "DISCONNECT_DEVICE",
  "resource": "192.168.1.50",
  "details": "Device blocked via firewall",
  "ipAddress": "192.168.1.100",
  "timestamp": "2024-03-15T14:30:00"
}
```

### Testing Device Control

**Manual test:**
```bash
# 1. Ping device (should work)
ping 192.168.1.50

# 2. Block device
curl -X POST http://localhost:8080/api/devices/disconnect/192.168.1.50

# 3. Ping again (should fail)
ping 192.168.1.50
# Result: Request timed out

# 4. Unblock device
curl -X POST http://localhost:8080/api/devices/reconnect/192.168.1.50

# 5. Ping again (should work)
ping 192.168.1.50
```

---

## PHASE 6: REPORTING

### Overview
Generate comprehensive network reports in JSON and text formats.

### Architecture Decision: DTO-Based Reporting

**Why NetworkReport DTO?**
```java
@Data
@AllArgsConstructor
public class NetworkReport {
    private LocalDateTime generatedAt;
    private int totalDevices;
    private int onlineDevices;
    private int offlineDevices;
    private int vulnerableDevices;
    private long totalBandwidthMB;
    private List<DeviceSummary> devices;
    private Map<String, Integer> devicesByOS;
    private Map<String, Integer> devicesByVendor;
}
```

**Reasoning:**
- Aggregated data (no raw entities)
- Computed fields
- Multiple data sources combined
- API-friendly structure

### Report Generation Strategy

**ReportService.generateReport():**
```java
public NetworkReport generateReport() {
    // 1. Get all devices
    List<Device> allDevices = deviceRepository.findAll();
    
    // 2. Calculate statistics
    int totalDevices = allDevices.size();
    int onlineDevices = (int) allDevices.stream()
        .filter(d -> d.getStatus() == Device.DeviceStatus.ONLINE)
        .count();
    int offlineDevices = (int) allDevices.stream()
        .filter(d -> d.getStatus() == Device.DeviceStatus.OFFLINE)
        .count();
    int vulnerableDevices = (int) allDevices.stream()
        .filter(Device::getIsVulnerable)
        .count();
    
    // 3. Calculate bandwidth
    List<BandwidthUsage> allBandwidth = bandwidthRepository.findAll();
    long totalBandwidthBytes = allBandwidth.stream()
        .mapToLong(BandwidthUsage::getTotalBytes)
        .sum();
    long totalBandwidthMB = totalBandwidthBytes / (1024 * 1024);
    
    // 4. Group by OS
    Map<String, Integer> devicesByOS = allDevices.stream()
        .filter(d -> d.getOperatingSystem() != null)
        .collect(Collectors.groupingBy(
            Device::getOperatingSystem,
            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
        ));
    
    // 5. Group by vendor
    Map<String, Integer> devicesByVendor = allDevices.stream()
        .filter(d -> d.getMacVendor() != null)
        .collect(Collectors.groupingBy(
            Device::getMacVendor,
            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
        ));
    
    // 6. Create device summaries
    List<DeviceSummary> deviceSummaries = allDevices.stream()
        .map(d -> new DeviceSummary(
            d.getIpAddress(),
            d.getHostname(),
            d.getMacAddress(),
            d.getMacVendor(),
            d.getOperatingSystem(),
            d.getStatus().toString(),
            d.getIsVulnerable(),
            d.getFirstSeen(),
            d.getLastSeen()
        ))
        .collect(Collectors.toList());
    
    // 7. Build report
    return new NetworkReport(
        LocalDateTime.now(),
        totalDevices,
        onlineDevices,
        offlineDevices,
        vulnerableDevices,
        totalBandwidthMB,
        deviceSummaries,
        devicesByOS,
        devicesByVendor
    );
}
```

### Stream API Deep Dive

**Grouping by OS:**
```java
Map<String, Integer> devicesByOS = allDevices.stream()
    .filter(d -> d.getOperatingSystem() != null)  // Remove nulls
    .collect(Collectors.groupingBy(
        Device::getOperatingSystem,               // Group key
        Collectors.collectingAndThen(
            Collectors.counting(),                 // Count per group
            Long::intValue                         // Convert Long to Integer
        )
    ));
```

**Result:**
```java
{
  "Windows 11": 5,
  "Ubuntu 22.04": 3,
  "iOS 17": 4
}
```

**Why collectingAndThen?**
- counting() returns Long
- We want Integer
- collectingAndThen applies transformation

### Text Report Generation

**StringBuilder pattern:**
```java
public String generateTextReport() {
    NetworkReport report = generateReport();
    StringBuilder sb = new StringBuilder();
    
    sb.append("═══════════════════════════════════════\n");
    sb.append("   NETWORK WATCHER - SECURITY REPORT\n");
    sb.append("═══════════════════════════════════════\n");
    sb.append("Generated: ").append(report.getGeneratedAt()).append("\n\n");
    
    sb.append("SUMMARY:\n");
    sb.append("  Total Devices: ").append(report.getTotalDevices()).append("\n");
    sb.append("  Online: ").append(report.getOnlineDevices()).append("\n");
    sb.append("  Vulnerable: ").append(report.getVulnerableDevices()).append("\n\n");
    
    // ... more sections
    
    return sb.toString();
}
```

**Why StringBuilder?**
- Efficient string concatenation
- Mutable (no new objects)
- Better than String + operator

**Performance comparison:**
```java
// Slow: Creates 1000 String objects
String result = "";
for (int i = 0; i < 1000; i++) {
    result += "line\n";  // New String each time
}

// Fast: Single StringBuilder
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append("line\n");  // Modifies existing
}
```

### Report Download

**Content-Disposition header:**
```java
@GetMapping("/text")
public ResponseEntity<String> getTextReport() {
    String report = reportService.generateTextReport();
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_PLAIN);
    headers.setContentDispositionFormData("attachment", "network-report.txt");
    
    return ResponseEntity.ok()
        .headers(headers)
        .body(report);
}
```

**What this does:**
- Browser downloads file instead of displaying
- Filename: network-report.txt
- Content-Type: text/plain

---

## PHASE 7: AUTHENTICATION & ENTERPRISE FEATURES

### Overview
Add JWT authentication, RBAC, network management, scan orchestration, alerts, and audit trail.

## 7.1: JWT Authentication

### Why JWT?
**Decision:** Token-based authentication, not sessions.

**JWT Structure:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.signature
[Header].[Payload].[Signature]
```

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "admin",
  "iat": 1710504000,
  "exp": 1710590400
}
```

**Signature:**
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

**Why JWT over sessions?**
- Stateless (no server-side storage)
- Scalable (works across multiple servers)
- Mobile-friendly
- Microservices-ready

**Session-based (old way):**
```
Client → Login → Server creates session → Session ID in cookie
Client → Request + Cookie → Server looks up session in database
```

**JWT (new way):**
```
Client → Login → Server creates JWT → Client stores token
Client → Request + JWT → Server verifies signature (no database)
```

### JwtUtil Implementation

**Generate token:**
```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 86400000))  // 24 hours
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
}
```

**Validate token:**
```java
public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
}
```

**Extract username:**
```java
public String extractUsername(String token) {
    return extractClaims(token).getSubject();
}

private Claims extractClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
}
```

### Security Configuration

**Spring Security filter chain:**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // Disable CSRF for stateless API
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()  // Login endpoint public
            .anyRequest().authenticated()                  // Everything else requires auth
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // No sessions
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

**Why disable CSRF?**
- CSRF protects session-based auth
- JWT is stateless (no cookies)
- CSRF not needed for token-based auth

**Filter order:**
```
Request → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → Controller
```

### JWT Authentication Filter

**JwtAuthenticationFilter:**
```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain chain) {
    // 1. Extract token from header
    final String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String jwt = authHeader.substring(7);
        
        // 2. Extract username from token
        String username = jwtUtil.extractUsername(jwt);
        
        // 3. Load user details
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 4. Validate token
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 5. Set authentication
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
    }
    
    // 6. Continue filter chain
    chain.doFilter(request, response);
}
```

**Flow:**
```
1. Client sends: Authorization: Bearer eyJhbGc...
2. Filter extracts token
3. Filter validates signature
4. Filter loads user from database
5. Filter sets authentication in SecurityContext
6. Controller can access authenticated user
```

## 7.2: Role-Based Access Control (RBAC)

### Entity Design

**User entity:**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;  // BCrypt hashed
    private Boolean enabled;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
```

**Role entity:**
```java
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;  // ROLE_ADMIN, ROLE_ANALYST, etc.
    private String description;
}
```

**Database structure:**
```
users:
id | username | password
1  | admin    | $2a$10$...

roles:
id | name        | description
1  | ROLE_ADMIN  | Administrator
2  | ROLE_ANALYST| Security Analyst

user_roles:
user_id | role_id
1       | 1
```

### @ManyToMany Relationship

**Why @ManyToMany?**
- One user can have multiple roles
- One role can belong to multiple users
- Requires join table

**@JoinTable explained:**
```java
@JoinTable(
    name = "user_roles",              // Join table name
    joinColumns = @JoinColumn(name = "user_id"),        // This entity's FK
    inverseJoinColumns = @JoinColumn(name = "role_id")  // Other entity's FK
)
```

**Why FetchType.EAGER?**
- Load roles immediately with user
- Needed for authorization checks
- Alternative: LAZY (load on demand)

**EAGER vs LAZY:**
```java
// EAGER: Single query with JOIN
SELECT u.*, r.* FROM users u 
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin';

// LAZY: Two queries
SELECT * FROM users WHERE username = 'admin';
// Later when accessing roles:
SELECT * FROM roles WHERE id IN (SELECT role_id FROM user_roles WHERE user_id = 1);
```

### Method-Level Security

**@PreAuthorize annotation:**
```java
@PostMapping("/networks")
@PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
public NetworkScope create(@RequestBody NetworkScope scope) {
    return service.create(scope);
}

@DeleteMapping("/networks/{id}")
@PreAuthorize("hasRole('ADMIN')")
public void delete(@PathVariable Long id) {
    service.delete(id);
}
```

**How it works:**
1. Spring AOP intercepts method call
2. Evaluates @PreAuthorize expression
3. Checks current user's roles
4. Allows or denies access

**SpEL expressions:**
```java
hasRole('ADMIN')                    // Has specific role
hasAnyRole('ADMIN', 'ANALYST')      // Has any of these roles
hasAuthority('WRITE_PRIVILEGE')     // Has specific authority
isAuthenticated()                   // Is logged in
permitAll()                         // Allow everyone
```

### Password Encryption

**BCrypt hashing:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Why BCrypt?**
- Adaptive (configurable work factor)
- Salt included automatically
- Slow by design (prevents brute force)
- Industry standard

**How it works:**
```java
String plainPassword = "admin123";
String hashed = passwordEncoder.encode(plainPassword);
// Result: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

boolean matches = passwordEncoder.matches("admin123", hashed);
// Result: true
```

**Salt explained:**
```
Password: admin123
Salt: random string (different each time)
Hash: BCrypt(admin123 + salt)

Same password, different hashes:
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
$2a$10$XYZ123...different...ABC789
```

## 7.3: Network Scope Management

**Why NetworkScope entity?**
- Manage multiple networks
- Schedule scans per network
- Enable/disable networks
- Exclude specific IPs

**Entity:**
```java
@Entity
public class NetworkScope {
    private String name;           // "Office Network"
    private String cidr;           // "192.168.1.0/24"
    private String exclusions;     // "192.168.1.1,192.168.1.254"
    private Boolean enabled;       // true/false
    private String scanSchedule;   // "0 0 2 * * ?" (cron)
    private LocalDateTime lastScanned;
}
```

**Use case:**
```
Office Network: 192.168.1.0/24 (exclude gateway)
Guest Network: 192.168.2.0/24 (scan less frequently)
DMZ Network: 10.0.0.0/24 (scan more frequently)
```

## 7.4: Scan Orchestration

**ScanJob entity:**
```java
@Entity
public class ScanJob {
    @ManyToOne
    private NetworkScope networkScope;
    
    private ScanType scanType;      // DISCOVERY, PORT_SCAN, VULNERABILITY, FULL
    private ScanStatus status;      // QUEUED, RUNNING, SUCCESS, FAILED
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer devicesFound;
    private String errorMessage;
}
```

**Workflow:**
```
1. Create ScanJob (status = QUEUED)
2. Scheduler picks up job
3. Update status = RUNNING
4. Execute scan
5. Update status = SUCCESS/FAILED
6. Record results
```

## 7.5: Alert System

**Alert entity:**
```java
@Entity
public class Alert {
    @ManyToOne
    private Device device;
    
    private AlertType type;         // VULNERABILITY, SUSPICIOUS_TRAFFIC, etc.
    private AlertSeverity severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String title;
    private String description;
    private AlertStatus status;     // OPEN, ACKNOWLEDGED, DISMISSED, ESCALATED
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
}
```

**Workflow:**
```
1. Vulnerability found → Create alert (status = OPEN)
2. Analyst reviews → Acknowledge (status = ACKNOWLEDGED)
3. Decision:
   - False positive → Dismiss
   - Real issue → Escalate
```

## 7.6: Audit Trail

**AuditLog entity:**
```java
@Entity
public class AuditLog {
    private String username;
    private String action;      // "DISCONNECT_DEVICE", "CREATE_USER", etc.
    private String resource;    // "192.168.1.50", "admin", etc.
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;
}
```

**Usage:**
```java
auditLogService.log(
    "admin",
    "DISCONNECT_DEVICE",
    "192.168.1.50",
    "Device blocked due to vulnerability",
    "192.168.1.100"
);
```

**Why audit logging?**
- Compliance (SOC 2, ISO 27001)
- Forensics (who did what when)
- Accountability
- Legal evidence

## Summary: All Phases Key Decisions

**Phase 1:** Nmap integration, JPA entities, REST API
**Phase 2:** Vulnerability scanning, @ManyToOne relationships
**Phase 3:** Time-series bandwidth data, Stream API
**Phase 4:** Pcap4J packet capture, in-memory storage
**Phase 5:** Windows Firewall integration, audit logging
**Phase 6:** DTO-based reporting, multiple formats
**Phase 7:** JWT authentication, RBAC, enterprise features

**Total:** 11 entities, 40+ endpoints, production-ready backend!
