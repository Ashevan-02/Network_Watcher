# Network Watcher - Phase 1: Network Scanning

## Prerequisites
- ✅ Nmap 7.98 installed
- Java 17
- Maven

## Running the Application

```bash
cd network-watcher
mvnw spring-boot:run
```

## API Endpoints

### Test Endpoints
```bash
# Check if API is running
GET http://localhost:8080/api/test/ping

# Verify Nmap installation
GET http://localhost:8080/api/test/nmap
```

### Device Management
```bash
# Get all devices
GET http://localhost:8080/api/devices

# Get device by ID
GET http://localhost:8080/api/devices/1

# Get device by IP
GET http://localhost:8080/api/devices/ip/192.168.1.1

# Get devices by status
GET http://localhost:8080/api/devices/status/ONLINE

# Get vulnerable devices
GET http://localhost:8080/api/devices/vulnerable
```

### Network Scanning
```bash
# Quick network scan (ping scan)
POST http://localhost:8080/api/scan/network?range=192.168.1.0/24

# Detailed device scan (OS detection, MAC, hostname)
POST http://localhost:8080/api/scan/device/192.168.1.1
```

## Example Usage

### 1. Scan Your Local Network
```bash
curl -X POST "http://localhost:8080/api/scan/network?range=192.168.1.0/24"
```

Response:
```json
{
  "networkRange": "192.168.1.0/24",
  "devicesFound": 5,
  "discoveredIps": ["192.168.1.1", "192.168.1.100", "192.168.1.105"],
  "status": "COMPLETED",
  "message": "Scan completed successfully"
}
```

### 2. Detailed Scan of Specific Device
```bash
curl -X POST "http://localhost:8080/api/scan/device/192.168.1.1"
```

### 3. View All Discovered Devices
```bash
curl http://localhost:8080/api/devices
```

## Database Access

H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:networkwatcher`
- Username: `sa`
- Password: (leave empty)

## Next Steps

Phase 2: Vulnerability Assessment
Phase 3: Bandwidth Monitoring
Phase 4: Packet Analysis
Phase 5: Device Control
Phase 6: Reporting
