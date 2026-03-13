# Network Watcher - Complete Implementation ✅

## Project Overview
A comprehensive network monitoring and security tool for the NCSA organization that discovers devices, scans for vulnerabilities, monitors bandwidth, analyzes packets, controls device access, and generates reports.

## All Phases Complete (7 Phases)

### ✅ Phase 1: Network Discovery
- Discover all networks
- Scan network and display connected devices
- Display device details (IP, MAC, hostname, OS, vendor)

### ✅ Phase 2: Vulnerability Assessment
- Conduct vulnerability assessment of devices
- Alert if vulnerable device found
- Track vulnerability details

### ✅ Phase 3: Bandwidth Monitoring
- Present data usage (bandwidth)
- Track bytes sent/received
- Monitor network traffic per device

### ✅ Phase 4: Packet Analysis
- Scan through HTTP packets
- Display important HTTP data (method, URL, headers)
- Extract user-agent, host, content-type

### ✅ Phase 5: Device Control
- Disconnect vulnerable devices
- Disconnect any device if necessary
- Reconnect devices
- List blocked devices

### ✅ Phase 6: Reporting
- Generate comprehensive network reports
- JSON format for APIs
- Text format for download/email
- Summary statistics and device details

### ✅ Phase 7: Authentication & Enterprise Features (NEW)
- JWT authentication & authorization
- Role-Based Access Control (RBAC)
- Network scope management
- Scan orchestration
- Alert system with workflow
- Audit trail logging

## API Endpoints

### Authentication
```bash
POST /api/auth/login
```

### Network Management
```bash
GET    /api/networks
POST   /api/networks
PUT    /api/networks/{id}
DELETE /api/networks/{id}
```

### Network Scanning
```bash
POST /api/scan/network?range=192.168.1.0/24
GET  /api/scan/interfaces
```

### Device Management
```bash
GET    /api/devices
GET    /api/devices/{id}
GET    /api/devices/ip/{ipAddress}
GET    /api/devices/status/{status}
GET    /api/devices/vulnerable
POST   /api/devices
PUT    /api/devices/{id}
DELETE /api/devices/{id}
POST   /api/devices/disconnect/{ipAddress}
POST   /api/devices/reconnect/{ipAddress}
GET    /api/devices/blocked
```

### Vulnerability Scanning
```bash
POST /api/vulnerabilities/scan/{ipAddress}
POST /api/vulnerabilities/scan/all
GET  /api/vulnerabilities/device/{deviceId}
GET  /api/vulnerabilities
```

### Bandwidth Monitoring
```bash
POST /api/bandwidth/record/{ip}
GET  /api/bandwidth/device/{deviceId}
GET  /api/bandwidth/device/{deviceId}/summary
GET  /api/bandwidth
```

### Packet Analysis
```bash
POST   /api/packets/start?interfaceIp={ip}
POST   /api/packets/stop
GET    /api/packets
GET    /api/packets/http
DELETE /api/packets
```

### Reporting
```bash
GET /api/reports/json
GET /api/reports/text
```

### Scan Orchestration
```bash
GET  /api/scans
GET  /api/scans/{id}
POST /api/scans
PUT  /api/scans/{id}/status
```

### Alert Management
```bash
GET  /api/alerts
GET  /api/alerts/{id}
POST /api/alerts
POST /api/alerts/{id}/acknowledge
POST /api/alerts/{id}/dismiss
POST /api/alerts/{id}/escalate
```

## Complete Workflow Example

```bash
# 0. Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Save the token from response
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# 1. Create network scope
curl -X POST http://localhost:8080/api/networks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Office","cidr":"192.168.1.0/24","enabled":true}'

# 2. Discover devices on network
curl -X POST "http://localhost:8080/api/scan/network?range=192.168.1.0/24" \
  -H "Authorization: Bearer $TOKEN"

# 2. Get all discovered devices
curl http://localhost:8080/api/devices \
  -H "Authorization: Bearer $TOKEN"

# 3. Scan all devices for vulnerabilities
curl -X POST http://localhost:8080/api/vulnerabilities/scan/all \
  -H "Authorization: Bearer $TOKEN"

# 4. Get vulnerable devices
curl http://localhost:8080/api/devices/vulnerable \
  -H "Authorization: Bearer $TOKEN"

# 5. Create alert for vulnerable device
curl -X POST http://localhost:8080/api/alerts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"device":{"id":1},"type":"VULNERABILITY","severity":"HIGH","title":"Vulnerability found"}'

# 6. Record bandwidth usage
curl -X POST http://localhost:8080/api/bandwidth/record/192.168.1.50 \
  -H "Authorization: Bearer $TOKEN"

# 7. Start packet capture
curl -X POST "http://localhost:8080/api/packets/start?interfaceIp=192.168.1.100" \
  -H "Authorization: Bearer $TOKEN"

# 8. Get HTTP packet details
curl http://localhost:8080/api/packets/http \
  -H "Authorization: Bearer $TOKEN"

# 9. Disconnect vulnerable device
curl -X POST http://localhost:8080/api/devices/disconnect/192.168.1.50 \
  -H "Authorization: Bearer $TOKEN"

# 10. Generate comprehensive report
curl http://localhost:8080/api/reports/json \
  -H "Authorization: Bearer $TOKEN"

# 11. Download text report
curl http://localhost:8080/api/reports/text \
  -H "Authorization: Bearer $TOKEN" \
  -o network-report.txt
```

## Technology Stack

### Backend
- **Spring Boot** - Application framework
- **Spring Security** - Authentication & authorization
- **JWT** - Token-based authentication
- **JPA/Hibernate** - Database ORM
- **H2 Database** - In-memory database (dev)
- **Lombok** - Reduce boilerplate code

### Network Tools
- **Nmap** - Network scanning and OS detection
- **Pcap4J** - Packet capture and analysis
- **Netstat** - Bandwidth monitoring
- **Windows Firewall** - Device control

### Build Tool
- **Maven** - Dependency management and build

## Project Structure

```
network-watcher/
├── src/main/java/com/networkwatcher/network_watcher/
│   ├── controller/
│   │   ├── BandwidthController.java
│   │   ├── DeviceController.java
│   │   ├── PacketController.java
│   │   ├── ReportController.java
│   │   ├── ScanController.java
│   │   └── VulnerabilityController.java
│   ├── dto/
│   │   ├── HttpPacketDetails.java
│   │   ├── NetworkReport.java
│   │   ├── ScanResult.java
│   │   └── VulnerabilityScanResult.java
│   ├── model/
│   │   ├── Alert.java
│   │   ├── AuditLog.java
│   │   ├── BandwidthUsage.java
│   │   ├── CapturedPacket.java
│   │   ├── Device.java
│   │   ├── NetworkScope.java
│   │   ├── Role.java
│   │   ├── ScanJob.java
│   │   ├── User.java
│   │   └── Vulnerability.java
│   ├── repository/
│   │   ├── BandwidthRepository.java
│   │   ├── DeviceRepository.java
│   │   └── VulnerabilityRepository.java
│   ├── service/
│   │   ├── BandwidthService.java
│   │   ├── DeviceControlService.java
│   │   ├── DeviceService.java
│   │   ├── NetworkScanService.java
│   │   ├── PacketService.java
│   │   ├── ReportService.java
│   │   └── VulnerabilityService.java
│   └── NetworkWatcherApplication.java
├── PHASE1_README.md
├── PHASE2_README.md
├── PHASE3_README.md
├── PHASE4_README.md
├── PHASE5_README.md
├── PHASE6_README.md
└── pom.xml
```

## Running the Application

### Prerequisites
- Java 17+
- Maven
- Nmap installed
- Administrator privileges (for packet capture and device control)

### Build and Run
```bash
cd network-watcher
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

### Access
- API: http://localhost:8080/api
- H2 Console: http://localhost:8080/h2-console

## Key Features Implemented

### Security Features
- ✅ Vulnerability scanning
- ✅ Device blocking/unblocking
- ✅ HTTP packet analysis
- ✅ Security reporting

### Monitoring Features
- ✅ Network discovery
- ✅ Device tracking
- ✅ Bandwidth monitoring
- ✅ Packet capture

### Management Features
- ✅ Device control
- ✅ Status tracking
- ✅ Report generation
- ✅ Historical data

## Database Schema

### users
- id, username, password, email, enabled
- created_at, last_login

### roles
- id, name, description

### user_roles
- user_id, role_id

### network_scopes
- id, name, cidr, exclusions, enabled
- scan_schedule, created_at, last_scanned

### scan_jobs
- id, network_scope_id, scan_type, status
- started_at, completed_at, devices_found

### alerts
- id, device_id, type, severity, title
- description, status, created_at

### audit_logs
- id, username, action, resource
- details, ip_address, created_at

### devices
- id, ip_address, mac_address, mac_vendor
- hostname, operating_system, status
- is_vulnerable, first_seen, last_seen

### vulnerabilities
- id, device_id, vulnerability_name
- severity, description, discovered_at

### bandwidth_usage
- id, device_id, bytes_sent, bytes_received
- total_bytes, packets_sent, packets_received, recorded_at

## Next Steps for Production

1. **Frontend Development**
   - React/Angular dashboard
   - Real-time updates with WebSocket
   - Charts and visualizations

2. **Security Enhancements**
   - Authentication (JWT)
   - Authorization (role-based)
   - API rate limiting
   - HTTPS/TLS

3. **Database**
   - Switch to PostgreSQL/MySQL
   - Add indexes
   - Implement caching

4. **Monitoring**
   - Scheduled scans
   - Email/SMS alerts
   - Logging and metrics

5. **Deployment**
   - Docker containerization
   - Kubernetes orchestration
   - CI/CD pipeline

## Documentation
- See individual PHASE*_README.md files for detailed documentation
- Each phase has usage examples and explanations

## License
Educational project for NCSA organization

## Contributors
Network Watcher Development Team
