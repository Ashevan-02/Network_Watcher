# Phase 2: Vulnerability Assessment - COMPLETE ✅

## What Was Added

### New Entities
- **Vulnerability** - Stores CVE details, severity, CVSS scores
  - Links to Device (Many-to-One relationship)
  - Tracks: CVE ID, description, severity, fix status

### New Services
- **VulnerabilityService** - Scans devices using Nmap NSE scripts
  - Parses CVE information from Nmap output
  - Auto-determines severity levels
  - Extracts CVSS scores

### New Endpoints

```bash
# Scan device for vulnerabilities
POST /api/vulnerabilities/scan/{ip}

# Get vulnerabilities for specific device
GET /api/vulnerabilities/device/{deviceId}

# Get all vulnerabilities
GET /api/vulnerabilities

# Get unfixed vulnerabilities only
GET /api/vulnerabilities/unfixed

# Mark vulnerability as fixed
PUT /api/vulnerabilities/{id}/fix
```

## Usage Examples

### 1. Scan Device for Vulnerabilities
```bash
curl -X POST http://localhost:8080/api/vulnerabilities/scan/192.168.1.1
```

Response:
```json
[
  {
    "id": 1,
    "cveId": "CVE-2023-12345",
    "description": "Critical vulnerability in SSH service",
    "severity": "CRITICAL",
    "cvssScore": 9.8,
    "discoveredAt": "2024-03-15T14:30:00",
    "isFixed": false
  }
]
```

### 2. Get Device Vulnerabilities
```bash
curl http://localhost:8080/api/vulnerabilities/device/1
```

### 3. Get All Unfixed Vulnerabilities
```bash
curl http://localhost:8080/api/vulnerabilities/unfixed
```

### 4. Mark as Fixed
```bash
curl -X PUT http://localhost:8080/api/vulnerabilities/1/fix
```

## How It Works

1. **Nmap NSE Scripts** - Uses `nmap --script vuln` to scan
2. **CVE Detection** - Parses output for CVE identifiers
3. **Severity Analysis** - Determines severity from description
4. **CVSS Extraction** - Extracts CVSS scores if available
5. **Database Storage** - Saves all findings to database
6. **Device Flagging** - Sets `isVulnerable=true` on device

## Severity Levels

- **CRITICAL** - Immediate action required
- **HIGH** - High priority fix
- **MEDIUM** - Should be addressed
- **LOW** - Minor issue
- **INFO** - Informational only

## Database Schema

```sql
CREATE TABLE vulnerabilities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id BIGINT NOT NULL,
    cve_id VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    severity VARCHAR(20) NOT NULL,
    cvss_score DOUBLE,
    discovered_at TIMESTAMP,
    is_fixed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (device_id) REFERENCES devices(id)
);
```

## Integration with Phase 1

- Automatically flags devices as vulnerable
- Updates `Device.isVulnerable` field
- Links vulnerabilities to discovered devices
- Can scan after network discovery

## Workflow Example

```bash
# 1. Discover devices
POST /api/scan/network?range=192.168.1.0/24

# 2. Scan for vulnerabilities
POST /api/vulnerabilities/scan/192.168.1.1

# 3. View vulnerable devices
GET /api/devices/vulnerable

# 4. Get vulnerability details
GET /api/vulnerabilities/device/1

# 5. Mark as fixed after patching
PUT /api/vulnerabilities/1/fix
```

## Next: Phase 3 - Bandwidth Monitoring
