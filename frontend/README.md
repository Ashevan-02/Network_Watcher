# Network Watcher Frontend

A modern React-based dashboard for monitoring network devices, vulnerabilities, and network activity in real-time.

## 🚀 Features

- **Real-time Device Monitoring** - Track online/offline devices with WebSocket updates
- **Vulnerability Management** - View and manage security vulnerabilities by severity
- **Network Activity** - Monitor packet traffic and bandwidth usage
- **Scan Management** - Initiate and track network scans
- **Dark/Light Theme** - Toggle between dark and light modes
- **Responsive Design** - Works on desktop, tablet, and mobile devices

## 📋 Prerequisites

- Node.js 18+ and npm
- Backend API running on `http://localhost:8080`

## 🛠️ Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## 🏗️ Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── Navbar.jsx
│   ├── Sidebar.jsx
│   ├── DeviceCard.jsx
│   ├── StatusBadge.jsx
│   ├── VulnerabilityAlert.jsx
│   └── ProtectedRoute.jsx
├── pages/              # Full page components
│   ├── Login.jsx
│   ├── Dashboard.jsx
│   ├── Devices.jsx
│   ├── DeviceDetails.jsx
│   ├── Vulnerabilities.jsx
│   ├── NetworkActivity.jsx
│   ├── Scans.jsx
│   └── Settings.jsx
├── services/           # API services
│   ├── api.js
│   ├── authService.js
│   ├── deviceService.js
│   ├── scanService.js
│   ├── vulnerabilityService.js
│   ├── packetService.js
│   └── websocketService.js
├── hooks/              # Custom React hooks
│   ├── useAuth.js
│   ├── useWebSocket.js
│   └── useDevices.js
├── context/            # React Context providers
│   ├── AuthContext.jsx
│   └── ThemeContext.jsx
├── utils/              # Helper functions
│   ├── formatters.js
│   └── constants.js
├── App.jsx             # Main app component
└── main.jsx            # Entry point
```

## 🔧 Configuration

Update API endpoints in `src/utils/constants.js`:

```javascript
export const API_BASE_URL = 'http://localhost:8080/api';
export const WS_BASE_URL = 'http://localhost:8080/ws';
```

## 🎨 Tech Stack

- **React 19** - UI library
- **Material-UI** - Component library
- **React Router** - Routing
- **Axios** - HTTP client
- **Recharts** - Charts and graphs
- **STOMP.js** - WebSocket communication
- **date-fns** - Date formatting
- **Vite** - Build tool

## 📱 Pages

1. **Dashboard** - Overview with stats and recent devices
2. **Devices** - List all devices with search functionality
3. **Device Details** - Detailed view of a single device
4. **Vulnerabilities** - Security alerts filtered by severity
5. **Network Activity** - Real-time packet monitoring
6. **Scans** - Manage network scans
7. **Settings** - User preferences

## 🔐 Authentication

The app uses JWT tokens stored in localStorage. Protected routes automatically redirect to login if not authenticated.

Default login endpoint: `POST /api/auth/login`

## 🌐 WebSocket Topics

- `/topic/devices` - Device updates
- `/topic/vulnerabilities` - New vulnerabilities
- `/topic/scans` - Scan progress

## 📝 License

MIT
