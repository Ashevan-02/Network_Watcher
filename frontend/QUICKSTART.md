# 🚀 Quick Start Guide

## Step 1: Install Dependencies
```bash
npm install
```

## Step 2: Configure Environment (Optional)
Copy `.env.example` to `.env` and update if needed:
```bash
cp .env.example .env
```

## Step 3: Start Backend
Make sure your Spring Boot backend is running on `http://localhost:8080`

## Step 4: Start Frontend
```bash
npm run dev
```

The app will open at `http://localhost:5173`

## Step 5: Login
Use your backend credentials to login. If you haven't created a user yet, register through the backend API.

## 🎯 What You'll See

### Dashboard (/)
- Total devices count
- Online devices count
- Critical vulnerabilities count
- Recent devices grid
- Real-time updates via WebSocket

### Devices (/devices)
- Searchable table of all devices
- Filter by IP, hostname, or MAC address
- Click any device to see details

### Device Details (/devices/:id)
- Complete device information
- Open ports
- Associated vulnerabilities

### Vulnerabilities (/vulnerabilities)
- All security vulnerabilities
- Filter by severity (Critical, High, Medium, Low)
- Color-coded alerts

### Network Activity (/network-activity)
- Recent packet captures
- Source/destination IPs
- Protocol information
- Packet sizes

### Scans (/scans)
- View all network scans
- Start new scans
- Track scan progress
- View devices found

### Settings (/settings)
- Toggle dark/light theme
- User preferences

## 🔧 Troubleshooting

### CORS Issues
If you get CORS errors, make sure your backend has CORS configured:
```java
@CrossOrigin(origins = "http://localhost:5173")
```

### WebSocket Connection Failed
Check that your backend WebSocket endpoint is at `/ws` and properly configured.

### 401 Unauthorized
Your JWT token may have expired. Logout and login again.

## 📱 Mobile View
The app is fully responsive. Try it on mobile by accessing your local IP:
```
http://YOUR_LOCAL_IP:5173
```

## 🎨 Customization

### Change Theme Colors
Edit `src/context/ThemeContext.jsx`:
```javascript
primary: { main: '#1976d2' },  // Change primary color
secondary: { main: '#dc004e' }  // Change secondary color
```

### Change API URL
Edit `src/utils/constants.js`:
```javascript
export const API_BASE_URL = 'http://your-api-url/api';
```

## 🚀 Production Build

```bash
# Build for production
npm run build

# Preview production build
npm run preview

# Deploy the 'dist' folder to your hosting service
```

## 📦 Deployment Options

- **Vercel**: `vercel deploy`
- **Netlify**: Drag & drop `dist` folder
- **AWS S3**: Upload `dist` folder to S3 bucket
- **Docker**: Create Dockerfile with nginx

## 🎉 You're Ready!

Start exploring your network with Network Watcher! 🌐
