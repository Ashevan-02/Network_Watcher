# 🚀 Quick Reference Guide

## Start the Application

```bash
cd network-watcher-frontend
npm run dev
```

Open: http://localhost:5173

## Project Structure

```
src/
├── components/     # 7 reusable components
├── pages/          # 8 full pages
├── services/       # 8 API services
├── hooks/          # 4 custom hooks
├── context/        # 2 context providers
└── utils/          # 2 utility files
```

## Key Features

### 🔐 Authentication
- Login at `/login`
- JWT token stored in localStorage
- Auto-redirect on 401

### 📊 Dashboard (`/`)
- Device statistics
- Bandwidth chart
- Recent devices
- Real-time updates

### 💻 Devices (`/devices`)
- Paginated table (DataGrid)
- Search & filter
- Export to CSV
- Refresh button
- Click row for details

### 🔍 Device Details (`/devices/:id`)
- Complete device info
- Bandwidth chart
- Open ports
- Vulnerabilities

### ⚠️ Vulnerabilities (`/vulnerabilities`)
- Filter by severity
- Color-coded alerts
- Tabbed interface

### 📡 Network Activity (`/network-activity`)
- Recent packets
- Protocol info
- Source/destination IPs

### 🔍 Scans (`/scans`)
- Scan history
- Start new scan
- Progress indicator
- Real-time updates

### ⚙️ Settings (`/settings`)
- Scan configuration
- Notifications
- Auto-refresh
- Dark/light theme

## API Endpoints

```javascript
// Auth
POST /api/auth/login
POST /api/auth/register

// Devices
GET /api/devices
GET /api/devices/{id}
GET /api/devices/stats

// Vulnerabilities
GET /api/vulnerabilities
GET /api/vulnerabilities/device/{id}

// Scans
GET /api/scans
POST /api/scans/start

// Bandwidth
GET /api/bandwidth
GET /api/bandwidth/device/{id}
GET /api/bandwidth/recent?minutes=60

// Packets
GET /api/packets/recent?limit=100
```

## WebSocket Topics

```javascript
/topic/devices          // Device updates
/topic/vulnerabilities  // New vulnerabilities
/topic/scan-progress    // Scan progress
/topic/alerts           // Alert notifications
```

## Common Tasks

### Add New Page
1. Create in `src/pages/NewPage.jsx`
2. Add route in `src/App.jsx`
3. Add menu item in `src/components/Sidebar.jsx`

### Add New API Service
1. Create in `src/services/newService.js`
2. Import `api` from `./api`
3. Export functions

### Show Notification
```javascript
import { useNotification } from '../hooks/useNotification';

const { showNotification } = useNotification();
showNotification('Success!', 'success');
```

### Use WebSocket
```javascript
import { useWebSocket } from '../hooks/useWebSocket';

useWebSocket('/topic/devices', (data) => {
  console.log('New device:', data);
});
```

## Environment Variables

Create `.env`:
```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_BASE_URL=http://localhost:8080/ws
```

## Build for Production

```bash
npm run build
npm run preview
```

## Troubleshooting

### CORS Error
Add to backend:
```java
@CrossOrigin(origins = "http://localhost:5173")
```

### 401 Unauthorized
- Check backend is running
- Verify JWT token in localStorage
- Try logout and login again

### WebSocket Not Connecting
- Check backend WebSocket at `/ws`
- Verify STOMP configuration
- Check browser console

## File Locations

### Components
- `src/components/Navbar.jsx`
- `src/components/Sidebar.jsx`
- `src/components/DeviceCard.jsx`
- `src/components/StatusBadge.jsx`
- `src/components/VulnerabilityAlert.jsx`
- `src/components/ProtectedRoute.jsx`
- `src/components/BandwidthChart.jsx`

### Pages
- `src/pages/Login.jsx`
- `src/pages/Dashboard.jsx`
- `src/pages/Devices.jsx`
- `src/pages/DeviceDetails.jsx`
- `src/pages/Vulnerabilities.jsx`
- `src/pages/NetworkActivity.jsx`
- `src/pages/Scans.jsx`
- `src/pages/Settings.jsx`

### Services
- `src/services/api.js`
- `src/services/authService.js`
- `src/services/deviceService.js`
- `src/services/vulnerabilityService.js`
- `src/services/scanService.js`
- `src/services/packetService.js`
- `src/services/bandwidthService.js`
- `src/services/websocketService.js`

## Documentation

- `README.md` - Overview
- `QUICKSTART.md` - Quick start
- `IMPLEMENTATION.md` - Implementation details
- `TESTING.md` - Testing guide
- `ARCHITECTURE.md` - Architecture
- `UI_MOCKUP.md` - UI mockups
- `PROJECT_SUMMARY.md` - Summary
- `MISSING_FEATURES.md` - Implemented features
- `COMPLETE_CHECKLIST.md` - Checklist
- `QUICK_REFERENCE.md` - This file

## Keyboard Shortcuts (Future)

- `Ctrl+K` - Search devices
- `Ctrl+R` - Refresh current page
- `Ctrl+/` - Toggle sidebar
- `Ctrl+T` - Toggle theme

## Tips

1. Use browser DevTools Network tab to debug API calls
2. Check localStorage for JWT token
3. Monitor WebSocket in console
4. Use React DevTools for component inspection
5. Check TESTING.md for comprehensive testing

## Support

- Check documentation files
- Review browser console
- Verify backend is running
- Test API endpoints with Postman

---

**Quick Start**: `npm run dev` → http://localhost:5173 → Login → Explore! 🚀
