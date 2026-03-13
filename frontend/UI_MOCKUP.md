# 🎨 UI Mockup Guide

## What Your Application Will Look Like

### 🔐 Login Page (`/login`)
```
┌─────────────────────────────────────────────────────────┐
│                                                          │
│                                                          │
│              ┌─────────────────────────┐                │
│              │                         │                │
│              │   🌐 Network Watcher    │                │
│              │                         │                │
│              │ Sign in to monitor      │                │
│              │    your network         │                │
│              │                         │                │
│              │  ┌───────────────────┐ │                │
│              │  │ Username          │ │                │
│              │  └───────────────────┘ │                │
│              │                         │                │
│              │  ┌───────────────────┐ │                │
│              │  │ Password          │ │                │
│              │  └───────────────────┘ │                │
│              │                         │                │
│              │  ┌───────────────────┐ │                │
│              │  │      LOGIN        │ │                │
│              │  └───────────────────┘ │                │
│              │                         │                │
│              └─────────────────────────┘                │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 📊 Dashboard (`/`)
```
┌──────────────────────────────────────────────────────────────────┐
│ 🌐 Network Watcher        🔔 Alerts  ☾ Dark  👤 Admin           │ ← Navbar
├──────────┬───────────────────────────────────────────────────────┤
│          │  📊 Dashboard                                         │
│ 🏠 Home  │                                                       │
│ 💻 Devices│  ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│ ⚠️  Vulns │  │ 📱 47    │ │ ✅ 42    │ │ ⚠️  3    │           │
│ 📡 Network│  │ Total    │ │ Online   │ │ Critical │           │
│ 🔍 Scans  │  │ Devices  │ │ Devices  │ │ Vulns    │           │
│ ⚙️  Settings│  └──────────┘ └──────────┘ └──────────┘           │
│          │                                                       │
│          │  📋 Recent Devices                                   │
│          │  ┌─────────┐ ┌─────────┐ ┌─────────┐               │
│          │  │ 💻      │ │ 💻      │ │ 💻      │               │
│          │  │ Router  │ │ Laptop  │ │ Phone   │               │
│          │  │ 🟢 Online│ │ 🟢 Online│ │ 🔴 Offline│             │
│          │  │192.168.1│ │192.168.2│ │192.168.3│               │
│          │  └─────────┘ └─────────┘ └─────────┘               │
│          │  ┌─────────┐ ┌─────────┐ ┌─────────┐               │
│          │  │ 💻      │ │ 💻      │ │ 💻      │               │
│          │  │ Server  │ │ Printer │ │ Camera  │               │
│          │  │ 🟢 Online│ │ 🟢 Online│ │ 🟢 Online│               │
│          │  │192.168.4│ │192.168.5│ │192.168.6│               │
│          │  └─────────┘ └─────────┘ └─────────┘               │
└──────────┴───────────────────────────────────────────────────────┘
```

### 💻 Devices Page (`/devices`)
```
┌──────────────────────────────────────────────────────────────────┐
│ 🌐 Network Watcher        🔔 Alerts  ☾ Dark  👤 Admin           │
├──────────┬───────────────────────────────────────────────────────┤
│          │  💻 Devices                                           │
│ 🏠 Home  │                                                       │
│ 💻 Devices│  ┌─────────────────────────────────────────────┐   │
│ ⚠️  Vulns │  │ 🔍 Search by IP, hostname, or MAC...        │   │
│ 📡 Network│  └─────────────────────────────────────────────┘   │
│ 🔍 Scans  │                                                       │
│ ⚙️  Settings│  ┌───────────────────────────────────────────────┐ │
│          │  │ IP Address │ Hostname │ MAC  │ OS  │ Status │   │
│          │  ├───────────────────────────────────────────────┤ │
│          │  │192.168.1.1 │ Router   │ AA:BB│Linux│ 🟢     │   │
│          │  │192.168.1.2 │ Laptop   │ CC:DD│Win11│ 🟢     │   │
│          │  │192.168.1.3 │ Phone    │ EE:FF│iOS  │ 🔴     │   │
│          │  │192.168.1.4 │ Server   │ 11:22│Linux│ 🟢     │   │
│          │  │192.168.1.5 │ Printer  │ 33:44│Other│ 🟢     │   │
│          │  │192.168.1.6 │ Camera   │ 55:66│Linux│ 🟢     │   │
│          │  └───────────────────────────────────────────────┘ │
└──────────┴───────────────────────────────────────────────────────┘
```

### 🔍 Device Details (`/devices/:id`)
```
┌──────────────────────────────────────────────────────────────────┐
│ 🌐 Network Watcher        🔔 Alerts  ☾ Dark  👤 Admin           │
├──────────┬───────────────────────────────────────────────────────┤
│          │  Router                                    🟢 ONLINE  │
│ 🏠 Home  │                                                       │
│ 💻 Devices│  ┌─────────────────────┐ ┌─────────────────────┐   │
│ ⚠️  Vulns │  │ Device Information  │ │ Open Ports          │   │
│ 📡 Network│  │                     │ │                     │   │
│ 🔍 Scans  │  │ IP: 192.168.1.1    │ │ [22] [80] [443]    │   │
│ ⚙️  Settings│  │ MAC: AA:BB:CC:DD   │ │ [8080] [3306]      │   │
│          │  │ OS: Linux          │ │                     │   │
│          │  │ Manufacturer: Cisco│ │                     │   │
│          │  │ First: Jan 1, 2024 │ │                     │   │
│          │  │ Last: 2 mins ago   │ │                     │   │
│          │  └─────────────────────┘ └─────────────────────┘   │
│          │                                                       │
│          │  ⚠️  Vulnerabilities                                 │
│          │  ┌─────────────────────────────────────────────┐   │
│          │  │ 🔴 CRITICAL                                 │   │
│          │  │ SQL Injection Vulnerability                 │   │
│          │  │ Database is vulnerable to SQL injection...  │   │
│          │  └─────────────────────────────────────────────┘   │
│          │  ┌─────────────────────────────────────────────┐   │
│          │  │ 🟠 HIGH                                     │   │
│          │  │ Outdated Software Version                   │   │
│          │  │ Software version is outdated and needs...   │   │
│          │  └─────────────────────────────────────────────┘   │
└──────────┴───────────────────────────────────────────────────────┘
```

### ⚠️ Vulnerabilities Page (`/vulnerabilities`)
```
┌──────────────────────────────────────────────────────────────────┐
│ 🌐 Network Watcher        🔔 Alerts  ☾ Dark  👤 Admin           │
├──────────┬───────────────────────────────────────────────────────┤
│          │  ⚠️  Vulnerabilities                                  │
│ 🏠 Home  │                                                       │
│ 💻 Devices│  [All] [Critical] [High] [Medium] [Low]             │
│ ⚠️  Vulns │                                                       │
│ 📡 Network│  ┌─────────────────────────────────────────────┐   │
│ 🔍 Scans  │  │ 🔴 CRITICAL                                 │   │
│ ⚙️  Settings│  │ SQL Injection Vulnerability                 │   │
│          │  │ Database is vulnerable to SQL injection...  │   │
│          │  └─────────────────────────────────────────────┘   │
│          │  ┌─────────────────────────────────────────────┐   │
│          │  │ 🔴 CRITICAL                                 │   │
│          │  │ Remote Code Execution                       │   │
│          │  │ Server allows remote code execution...      │   │
│          │  └─────────────────────────────────────────────┘   │
│          │  ┌─────────────────────────────────────────────┐   │
│          │  │ 🟠 HIGH                                     │   │
│          │  │ Cross-Site Scripting (XSS)                  │   │
│          │  │ Application is vulnerable to XSS attacks... │   │
│          │  └─────────────────────────────────────────────┘   │
└──────────┴───────────────────────────────────────────────────────┘
```

### 📡 Network Activity (`/network-activity`)
```
┌──────────────────────────────────────────────────────────────────┐
│ 🌐 Network Watcher        🔔 Alerts  ☾ Dark  👤 Admin           │
├──────────┬───────────────────────────────────────────────────────┤
│          │  📡 Network Activity                                  │
│ 🏠 Home  │                                                       │
│ 💻 Devices│  ┌───────────────────────────────────────────────┐ │
│ ⚠️  Vulns │  │Time│Source IP│Dest IP│Proto│S.Port│D.Port│Size││
│ 📡 Network│  ├───────────────────────────────────────────────┤ │
│ 🔍 Scans  │  │10:30│192.168.1│8.8.8.8│ TCP │ 443  │ 80   │1KB ││
│ ⚙️  Settings│  │10:31│192.168.2│1.1.1.1│ UDP │ 53   │ 53   │512B││
│          │  │10:32│192.168.3│8.8.8.8│ TCP │ 443  │ 443  │2KB ││
│          │  │10:33│192.168.1│8.8.4.4│ICMP │ -    │ -    │64B ││
│          │  │10:34│192.168.4│1.1.1.1│ TCP │ 80   │ 8080 │4KB ││
│          │  └───────────────────────────────────────────────┘ │
└──────────┴───────────────────────────────────────────────────────┘
```

### 🔍 Scans Page (`/scans`)
```
┌──────────────────────────────────────────────────────────────────┐
│ 🌐 Network Watcher        🔔 Alerts  ☾ Dark  👤 Admin           │
├──────────┬───────────────────────────────────────────────────────┤
│          │  🔍 Scans                    [▶️ Start New Scan]     │
│ 🏠 Home  │                                                       │
│ 💻 Devices│  ┌───────────────────────────────────────────────┐ │
│ ⚠️  Vulns │  │ID│Network Range│Status│Devices│Started│Ended ││
│ 📡 Network│  ├───────────────────────────────────────────────┤ │
│ 🔍 Scans  │  │1 │192.168.1.0/24│✅ DONE│  47  │Jan 1 │Jan 1 ││
│ ⚙️  Settings│  │2 │192.168.2.0/24│✅ DONE│  23  │Jan 2 │Jan 2 ││
│          │  │3 │192.168.1.0/24│🔄 RUN │  12  │Jan 3 │-     ││
│          │  │4 │10.0.0.0/24   │⏳ PEND│  0   │Jan 3 │-     ││
│          │  └───────────────────────────────────────────────┘ │
└──────────┴───────────────────────────────────────────────────────┘
```

### ⚙️ Settings Page (`/settings`)
```
┌──────────────────────────────────────────────────────────────────┐
│ 🌐 Network Watcher        🔔 Alerts  ☾ Dark  👤 Admin           │
├──────────┬───────────────────────────────────────────────────────┤
│          │  ⚙️  Settings                                         │
│ 🏠 Home  │                                                       │
│ 💻 Devices│  ┌─────────────────────────────────────────────┐   │
│ ⚠️  Vulns │  │ Appearance                                  │   │
│ 📡 Network│  │                                             │   │
│ 🔍 Scans  │  │ Dark Mode  [🔘────────]                    │   │
│ ⚙️  Settings│  │                                             │   │
│          │  └─────────────────────────────────────────────┘   │
└──────────┴───────────────────────────────────────────────────────┘
```

## 🎨 Color Scheme

### Light Mode
- Background: White (#FFFFFF)
- Text: Dark Gray (#212121)
- Primary: Blue (#1976d2)
- Success: Green (#4caf50)
- Error: Red (#f44336)
- Warning: Orange (#ff9800)

### Dark Mode
- Background: Dark Gray (#121212)
- Text: White (#FFFFFF)
- Primary: Light Blue (#90caf9)
- Success: Light Green (#81c784)
- Error: Light Red (#e57373)
- Warning: Light Orange (#ffb74d)

## 📱 Responsive Design

### Desktop (1920x1080)
- Full sidebar visible
- 3-column grid for device cards
- Wide tables with all columns

### Tablet (768x1024)
- Collapsible sidebar
- 2-column grid for device cards
- Scrollable tables

### Mobile (375x667)
- Hidden sidebar (hamburger menu)
- 1-column grid for device cards
- Stacked table rows

## 🎯 Interactive Elements

### Buttons
- Primary: Blue background, white text
- Secondary: Outlined, blue border
- Hover: Slightly darker shade
- Click: Ripple effect (Material-UI)

### Cards
- Hover: Elevation increases (shadow)
- Click: Navigate to details
- Cursor: Pointer on hover

### Status Badges
- Online: Green chip with dot
- Offline: Red chip with dot
- Running: Blue chip with dot
- Completed: Green chip with checkmark

### Alerts
- Critical: Red background
- High: Red background
- Medium: Orange background
- Low: Blue background
- Info: Gray background

## 🌟 Animations

- Page transitions: Fade in
- Loading: Circular spinner
- Theme toggle: Smooth color transition
- Hover effects: Scale slightly
- Ripple effects: Material-UI default

---

**This is what your Network Watcher will look like! 🎨**
