# 🧪 Testing Guide

## Pre-Testing Checklist

### Backend Requirements
- [ ] Spring Boot backend running on `http://localhost:8080`
- [ ] Database connected and migrations applied
- [ ] At least one user account created
- [ ] CORS enabled for `http://localhost:5173`
- [ ] WebSocket endpoint configured at `/ws`

### Frontend Setup
```bash
cd network-watcher-frontend
npm install
npm run dev
```

## 🔐 Test Authentication

### 1. Login Page
- [ ] Navigate to `http://localhost:5173/login`
- [ ] Enter valid credentials
- [ ] Click "Login" button
- [ ] Should redirect to Dashboard
- [ ] Token should be stored in localStorage

### 2. Protected Routes
- [ ] Logout (click user icon in navbar)
- [ ] Try accessing `http://localhost:5173/devices`
- [ ] Should redirect to `/login`
- [ ] Login again
- [ ] Should redirect back to Dashboard

## 📊 Test Dashboard

### 1. Statistics Cards
- [ ] Total Devices count displays correctly
- [ ] Online Devices count displays correctly
- [ ] Critical Vulnerabilities count displays correctly

### 2. Recent Devices
- [ ] Device cards display with correct information
- [ ] Status badges show correct colors (green=online, red=offline)
- [ ] Click on a device card navigates to device details

### 3. Real-Time Updates
- [ ] Start a network scan from backend
- [ ] New devices should appear in real-time
- [ ] No page refresh needed

## 💻 Test Devices Page

### 1. Device List
- [ ] All devices display in table
- [ ] IP addresses show correctly
- [ ] Hostnames display (or "Unknown")
- [ ] MAC addresses display
- [ ] OS types show correctly
- [ ] Status badges display with correct colors
- [ ] Last seen times show relative format ("2 minutes ago")

### 2. Search Functionality
- [ ] Type IP address in search box
- [ ] Table filters to matching devices
- [ ] Type hostname in search box
- [ ] Table filters to matching devices
- [ ] Clear search shows all devices again

### 3. Navigation
- [ ] Click on any device row
- [ ] Should navigate to device details page

## 🔍 Test Device Details

### 1. Device Information
- [ ] Device name/hostname displays in header
- [ ] Status badge shows correct status
- [ ] IP address displays correctly
- [ ] MAC address displays correctly
- [ ] OS type displays correctly
- [ ] Manufacturer displays correctly
- [ ] First seen date displays correctly
- [ ] Last seen date displays correctly

### 2. Open Ports
- [ ] Open ports display as chips
- [ ] If no ports, shows "No open ports detected"

### 3. Vulnerabilities
- [ ] Device vulnerabilities display as alerts
- [ ] Severity colors match (Critical=red, High=red, Medium=orange, Low=blue)
- [ ] If no vulnerabilities, shows "No vulnerabilities detected"

## ⚠️ Test Vulnerabilities Page

### 1. Vulnerability List
- [ ] All vulnerabilities display
- [ ] Severity badges show correct colors
- [ ] Titles and descriptions display correctly

### 2. Filtering
- [ ] Click "All" tab - shows all vulnerabilities
- [ ] Click "Critical" tab - shows only critical
- [ ] Click "High" tab - shows only high severity
- [ ] Click "Medium" tab - shows only medium severity
- [ ] Click "Low" tab - shows only low severity

## 📡 Test Network Activity

### 1. Packet Table
- [ ] Recent packets display in table
- [ ] Timestamps show correctly
- [ ] Source IPs display
- [ ] Destination IPs display
- [ ] Protocols display (TCP, UDP, ICMP, etc.)
- [ ] Source ports display
- [ ] Destination ports display
- [ ] Packet sizes display in bytes

### 2. Real-Time Updates
- [ ] Generate network traffic
- [ ] New packets should appear automatically

## 🔍 Test Scans Page

### 1. Scan History
- [ ] All previous scans display in table
- [ ] Scan IDs display
- [ ] Network ranges display
- [ ] Status chips show correct colors
- [ ] Devices found count displays
- [ ] Start times display correctly
- [ ] Completed times display (or "In progress")

### 2. Start New Scan
- [ ] Click "Start New Scan" button
- [ ] New scan should appear in table
- [ ] Status should be "RUNNING" or "PENDING"
- [ ] Watch status change to "COMPLETED"
- [ ] Devices found count should update

## ⚙️ Test Settings Page

### 1. Theme Toggle
- [ ] Toggle dark mode switch
- [ ] Theme should change to dark
- [ ] Toggle again
- [ ] Theme should change to light
- [ ] Preference should persist on page refresh

## 🎨 Test UI/UX

### 1. Navigation
- [ ] Click each menu item in sidebar
- [ ] Active menu item highlights
- [ ] Page content changes correctly

### 2. Theme Toggle (Navbar)
- [ ] Click sun/moon icon in navbar
- [ ] Theme toggles between light and dark
- [ ] All pages respect theme

### 3. Responsive Design
- [ ] Resize browser window
- [ ] Layout adjusts for smaller screens
- [ ] Sidebar collapses on mobile
- [ ] Tables scroll horizontally if needed

### 4. Loading States
- [ ] Refresh any page
- [ ] Loading spinner should appear
- [ ] Content loads after API response

## 🔌 Test WebSocket Connection

### 1. Connection Status
- [ ] Open browser console
- [ ] Look for "WebSocket connected" message
- [ ] Should connect automatically on login

### 2. Real-Time Updates
- [ ] Keep Dashboard open
- [ ] Start a scan from backend or another browser
- [ ] New devices should appear without refresh
- [ ] Statistics should update automatically

### 3. Reconnection
- [ ] Stop backend server
- [ ] WebSocket should disconnect
- [ ] Start backend server again
- [ ] WebSocket should reconnect automatically (check console)

## 🐛 Common Issues & Solutions

### Issue: CORS Error
**Solution**: Add CORS configuration to backend:
```java
@CrossOrigin(origins = "http://localhost:5173")
```

### Issue: 401 Unauthorized
**Solution**: 
- Check if token is in localStorage
- Token may have expired - logout and login again
- Verify backend JWT configuration

### Issue: WebSocket Connection Failed
**Solution**:
- Check backend WebSocket endpoint is at `/ws`
- Verify STOMP configuration in backend
- Check browser console for error details

### Issue: No Data Showing
**Solution**:
- Verify backend is running
- Check API_BASE_URL in constants.js
- Open Network tab in browser DevTools
- Check for API errors

### Issue: Theme Not Persisting
**Solution**:
- Check localStorage in browser DevTools
- Clear localStorage and try again

## ✅ Testing Checklist Summary

- [ ] Authentication works (login/logout)
- [ ] Protected routes redirect correctly
- [ ] Dashboard displays all statistics
- [ ] Devices page shows all devices
- [ ] Search filters devices correctly
- [ ] Device details page shows complete info
- [ ] Vulnerabilities page filters by severity
- [ ] Network activity shows packets
- [ ] Scans page shows history and can start new scans
- [ ] Settings page toggles theme
- [ ] WebSocket updates work in real-time
- [ ] Navigation works between all pages
- [ ] Theme toggle works from navbar
- [ ] Responsive design works on mobile
- [ ] Loading states appear correctly
- [ ] No console errors

## 🎉 All Tests Passed?

Congratulations! Your Network Watcher frontend is fully functional! 🚀

## 📝 Report Issues

If you find any bugs:
1. Check browser console for errors
2. Check Network tab for failed API calls
3. Verify backend logs
4. Check WebSocket connection status

---

**Happy Testing! 🧪**
