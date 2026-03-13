# 🌐 Real-World Testing Guide - Monitor Your Actual WiFi

## 📋 What You'll Discover About Your Network

When you run Network Watcher on your WiFi, you'll see:
- **All connected devices** (phones, laptops, smart TVs, IoT devices)
- **Who's using your WiFi** (family members, guests, unauthorized users)
- **Device details** (names, manufacturers, operating systems)
- **Security vulnerabilities** (outdated devices, open ports)
- **Bandwidth usage** (which device is hogging the internet)
- **Web activity** (HTTP traffic - unencrypted websites)

---

## 🚀 Step-by-Step: Test on Your WiFi

### **Step 1: Find Your Network Range**

Open Command Prompt and run:
```cmd
ipconfig
```

Look for your WiFi adapter:
```
Wireless LAN adapter Wi-Fi:
   IPv4 Address. . . . . . . . . . . : 192.168.1.100
   Subnet Mask . . . . . . . . . . . : 255.255.255.0
   Default Gateway . . . . . . . . . : 192.168.1.1
```

**Your network range is:** `192.168.1.0/24`
- This means: 192.168.1.1 to 192.168.1.254 (254 possible devices)

Common ranges:
- `192.168.1.0/24` (most home routers)
- `192.168.0.0/24` (some routers)
- `10.0.0.0/24` (Apple routers)

---

### **Step 2: Install Nmap (Required)**

1. Download Nmap: https://nmap.org/download.html
2. Install with default settings
3. Verify installation:
```cmd
nmap --version
```

---

### **Step 3: Start Backend (Run as Administrator)**

**IMPORTANT:** Must run as Administrator for packet capture and device control!

```cmd
cd c:\Users\Cordial space\Downloads\network-watcher\network-watcher
mvnw.cmd spring-boot:run
```

Wait for: `Started NetworkWatcherApplication`

---

### **Step 4: Start Frontend**

Open new terminal:
```cmd
cd c:\Users\Cordial space\Desktop\networkwatcher_Frontend\network-watcher-frontend
npm run dev
```

Open browser: http://localhost:5173

---

### **Step 5: Login**

Default credentials:
- Username: `admin`
- Password: `admin123`

---

### **Step 6: Discover Devices on Your WiFi**

#### **Option A: Using Frontend (Easy)**
1. Go to **Scans** page
2. Enter your network range: `192.168.1.0/24`
3. Click **Start Scan**
4. Watch progress bar (takes 1-3 minutes)
5. Go to **Devices** page to see results

#### **Option B: Using API (Testing)**
```cmd
curl -X POST "http://localhost:8080/api/scan/network?range=192.168.1.0/24" ^
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### **Step 7: View Discovered Devices**

Go to **Devices** page. You'll see devices like:

| IP | Hostname | MAC Vendor | OS | Status |
|----|----------|------------|-----|--------|
| 192.168.1.1 | router.home | TP-Link | Linux | Online |
| 192.168.1.50 | Johns-iPhone | Apple Inc | iOS 17 | Online |
| 192.168.1.75 | DESKTOP-ABC | Dell Inc | Windows 11 | Online |
| 192.168.1.100 | Smart-TV | Samsung | Linux | Online |

---

### **Step 8: Get Detailed Info on Specific Device**

Click on any device to see:
- Full OS details
- Open ports
- Services running
- Connection history
- Bandwidth usage

Or use API:
```cmd
curl http://localhost:8080/api/devices/ip/192.168.1.50 ^
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### **Step 9: Scan for Vulnerabilities**

#### **Scan All Devices:**
```cmd
curl -X POST http://localhost:8080/api/vulnerabilities/scan/all ^
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### **Scan Specific Device:**
```cmd
curl -X POST http://localhost:8080/api/vulnerabilities/scan/192.168.1.50 ^
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**What it checks:**
- Open ports (FTP, Telnet, SSH, HTTP, etc.)
- Outdated services
- Known vulnerabilities
- Weak security configurations

---

### **Step 10: Monitor Bandwidth Usage**

Go to **Dashboard** to see:
- Total network bandwidth
- Upload/Download speeds
- Per-device usage chart

Or record bandwidth for specific device:
```cmd
curl -X POST http://localhost:8080/api/bandwidth/record/192.168.1.50 ^
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### **Step 11: Capture HTTP Packets (See Web Activity)**

**Start packet capture:**
```cmd
curl -X POST "http://localhost:8080/api/packets/start?interfaceIp=192.168.1.100" ^
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**View captured packets:**
Go to **Network Activity** page or:
```cmd
curl http://localhost:8080/api/packets/http ^
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**You'll see:**
- Which device visited which website
- HTTP requests (not HTTPS - those are encrypted)
- User agents
- Request methods (GET, POST)

**Example:**
```json
{
  "sourceIp": "192.168.1.50",
  "method": "GET",
  "url": "http://example.com/page",
  "userAgent": "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0)",
  "host": "example.com"
}
```

---

### **Step 12: Block Suspicious Device**

If you find unauthorized device:

**Using Frontend:**
1. Go to device details
2. Click **Disconnect Device**

**Using API:**
```cmd
curl -X POST http://localhost:8080/api/devices/disconnect/192.168.1.50 ^
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**What happens:**
- Windows Firewall rule created
- Device blocked from network
- Device status changes to "BLOCKED"

**To unblock:**
```cmd
curl -X POST http://localhost:8080/api/devices/reconnect/192.168.1.50 ^
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### **Step 13: Generate Report**

**JSON Report:**
```cmd
curl http://localhost:8080/api/reports/json ^
  -H "Authorization: Bearer YOUR_JWT_TOKEN" > network-report.json
```

**Text Report:**
```cmd
curl http://localhost:8080/api/reports/text ^
  -H "Authorization: Bearer YOUR_JWT_TOKEN" > network-report.txt
```

**Report includes:**
- Total devices found
- Vulnerable devices
- Bandwidth statistics
- Device details
- Security recommendations

---

## 🎯 Real-World Use Cases

### **Use Case 1: Find Who's Stealing Your WiFi**
1. Scan network: `192.168.1.0/24`
2. Check devices list
3. Identify unknown devices
4. Block unauthorized devices

### **Use Case 2: Monitor Kids' Internet Usage**
1. Identify kids' devices by MAC vendor (Apple, Samsung)
2. Monitor bandwidth usage
3. Capture HTTP packets to see websites
4. Set alerts for high bandwidth usage

### **Use Case 3: Secure Your Home Network**
1. Scan all devices for vulnerabilities
2. Check for open ports (FTP, Telnet = dangerous)
3. Identify outdated devices
4. Block vulnerable devices until updated

### **Use Case 4: Office Network Monitoring**
1. Schedule automatic scans every hour
2. Get alerts when new device connects
3. Monitor bandwidth per department
4. Generate daily reports

---

## 🔍 What You'll Actually See

### **Example: Your Home Network**

**Devices Found:**
```
1. Router (192.168.1.1) - TP-Link
2. Your Laptop (192.168.1.100) - Windows 11
3. Your Phone (192.168.1.50) - iPhone
4. Smart TV (192.168.1.75) - Samsung
5. Guest Phone (192.168.1.120) - Android
6. Unknown Device (192.168.1.150) - ??? ⚠️
```

**Vulnerabilities:**
```
⚠️ Smart TV (192.168.1.75):
   - Port 23 (Telnet) OPEN - HIGH RISK
   - Outdated firmware
   - Recommendation: Update firmware or block

⚠️ Unknown Device (192.168.1.150):
   - Unauthorized access
   - Recommendation: Block immediately
```

**Bandwidth Usage:**
```
Smart TV: 5.2 GB (streaming Netflix)
Your Laptop: 1.8 GB (work)
Your Phone: 450 MB (social media)
Unknown Device: 2.1 GB (suspicious) ⚠️
```

---

## ⚠️ Important Notes

### **Limitations:**

1. **HTTPS Traffic**: Cannot see encrypted traffic (most modern websites)
   - Can see: http://example.com ✅
   - Cannot see: https://google.com ❌

2. **Device Control**: Only works on Windows (uses Windows Firewall)
   - Linux: Need iptables
   - macOS: Need pfctl

3. **Permissions**: Must run as Administrator
   - Packet capture requires admin rights
   - Firewall control requires admin rights

4. **Network Type**:
   - Works on: Home WiFi, Office LAN
   - Doesn't work on: Public WiFi (no admin access)

### **Legal & Ethical:**

⚠️ **ONLY scan networks you own or have permission to scan!**
- Scanning others' networks = illegal
- Use only on your home/office network
- Get written permission for office networks

---

## 🐛 Troubleshooting

### **Problem: No devices found**
**Solution:**
- Check if Nmap is installed: `nmap --version`
- Verify network range: `ipconfig`
- Run as Administrator
- Check firewall isn't blocking Nmap

### **Problem: Cannot capture packets**
**Solution:**
- Run backend as Administrator
- Install WinPcap or Npcap
- Check network adapter permissions

### **Problem: Cannot block devices**
**Solution:**
- Run as Administrator
- Check Windows Firewall is enabled
- Verify you have admin rights

### **Problem: Scan takes too long**
**Solution:**
- Reduce network range (e.g., 192.168.1.1-50 instead of /24)
- Use faster scan: `nmap -sn -T4` (modify NetworkScanService)

---

## 📊 Expected Results

**Small Home Network (5-10 devices):**
- Scan time: 1-2 minutes
- Devices found: 5-10
- Vulnerabilities: 1-3 (usually IoT devices)

**Medium Office Network (20-50 devices):**
- Scan time: 3-5 minutes
- Devices found: 20-50
- Vulnerabilities: 5-10

**Large Network (100+ devices):**
- Scan time: 10-15 minutes
- Devices found: 100+
- Vulnerabilities: 20+

---

## 🎉 Success Checklist

- [ ] Backend running on port 8080
- [ ] Frontend running on port 5173
- [ ] Logged in successfully
- [ ] Network scan completed
- [ ] Devices displayed in list
- [ ] Device details showing
- [ ] Vulnerability scan working
- [ ] Bandwidth chart showing data
- [ ] Packet capture working
- [ ] Device blocking working
- [ ] Report generated

---

## 🚀 Next Steps After Testing

1. **Schedule Automatic Scans**
   - Add cron job for hourly scans
   - Get email alerts for new devices

2. **Set Up Alerts**
   - Alert when vulnerable device found
   - Alert when unknown device connects
   - Alert when bandwidth exceeds threshold

3. **Deploy to Server**
   - Run on dedicated machine
   - Monitor 24/7
   - Access from anywhere

4. **Integrate with Router**
   - Some routers have APIs
   - Can get real-time connection data
   - Better device identification

---

## 📞 Need Help?

If something doesn't work:
1. Check logs: `target/logs/network-watcher.log`
2. Verify Nmap installation
3. Confirm admin privileges
4. Check network connectivity

**Your Network Watcher is ready to protect your network! 🛡️**
