# Phase 3: Bandwidth Monitoring - COMPLETE ✅

## What Was Added

### New Entities
- **BandwidthUsage** - Tracks network data usage per device
  - Links to Device (Many-to-One relationship)
  - Tracks: bytes sent/received, packets sent/received, timestamps

### New Services
- **BandwidthService** - Monitors network traffic using netstat
  - Records bandwidth usage snapshots
  - Calculates bandwidth summaries
  - Tracks historical usage

### New Endpoints

```bash
# Record bandwidth for device
POST /api/bandwidth/record/{ip}

# Get bandwidth history for device
GET /api/bandwidth/device/{deviceId}

# Get bandwidth summary for device
GET /api/bandwidth/device/{deviceId}/summary

# Get all bandwidth records
GET /api/bandwidth
```

## Usage Examples

### 1. Record Bandwidth Snapshot
```bash
curl -X POST http://localhost:8080/api/bandwidth/record/192.168.1.1
```

Response:
```json
{
  "id": 1,
  "bytesSent": 1048576,
  "bytesReceived": 2097152,
  "totalBytes": 3145728,
  "packetsSent": 1000,
  "packetsReceived": 1500,
  "recordedAt": "2024-03-15T14:30:00"
}
```

### 2. Get Device Bandwidth History
```bash
curl http://localhost:8080/api/bandwidth/device/1
```

### 3. Get Bandwidth Summary
```bash
curl http://localhost:8080/api/bandwidth/device/1/summary
```

Response:
```json
{
  "totalBytesSent": 10485760,
  "totalBytesReceived": 20971520,
  "totalBytes": 31457280,
  "totalMB": 30.0,
  "recordCount": 10
}
```

## How It Works

1. **netstat Command** - Uses Windows `netstat -e` to get network statistics
2. **Data Extraction** - Parses bytes and packets sent/received
3. **Database Storage** - Saves snapshots to database
4. **Historical Tracking** - Maintains usage history per device
5. **Summary Calculation** - Aggregates total usage

## Data Tracked

- **Bytes Sent** - Total data uploaded
- **Bytes Received** - Total data downloaded
- **Total Bytes** - Combined upload + download
- **Packets Sent** - Number of packets sent
- **Packets Received** - Number of packets received
- **Timestamp** - When measurement was taken

## Database Schema

```sql
CREATE TABLE bandwidth_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id BIGINT NOT NULL,
    bytes_sent BIGINT DEFAULT 0,
    bytes_received BIGINT DEFAULT 0,
    total_bytes BIGINT DEFAULT 0,
    packets_sent BIGINT DEFAULT 0,
    packets_received BIGINT DEFAULT 0,
    recorded_at TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id)
);
```

## Integration with Previous Phases

- Links to discovered devices (Phase 1)
- Can track bandwidth of vulnerable devices (Phase 2)
- Provides data for reports (Phase 6)

## Workflow Example

```bash
# 1. Discover device
POST /api/scan/network?range=192.168.1.0/24

# 2. Record initial bandwidth
POST /api/bandwidth/record/192.168.1.1

# 3. Wait some time...

# 4. Record again
POST /api/bandwidth/record/192.168.1.1

# 5. View usage history
GET /api/bandwidth/device/1

# 6. Get summary
GET /api/bandwidth/device/1/summary
```

## Use Cases

- **Monitor heavy users** - Identify devices using most bandwidth
- **Detect anomalies** - Unusual traffic patterns
- **Capacity planning** - Track network usage trends
- **Billing** - Track usage for cost allocation
- **Performance** - Identify network bottlenecks

## Limitations

- Uses system-wide netstat (not per-device on Windows)
- Requires periodic polling (not real-time)
- For production: Consider packet capture libraries (Pcap4J)

## Next: Phase 4 - Packet Analysis
