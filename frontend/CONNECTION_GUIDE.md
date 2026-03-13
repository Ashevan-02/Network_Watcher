# 🔗 Backend-Frontend Connection Guide

## ✅ Backend Configuration (DONE)

### 1. CORS Configuration
**File**: `WebConfig.java`
- ✅ Allows `http://localhost:5173` (frontend)
- ✅ Allows credentials
- ✅ Allows all necessary HTTP methods

### 2. Security Configuration
**File**: `SecurityConfig.java`
- ✅ Permits `/api/auth/**` (login/register)
- ✅ Permits `/ws/**` (WebSocket)
- ✅ JWT authentication for other endpoints

### 3. WebSocket Configuration
**File**: `WebSocketConfig.java`
- ✅ Endpoint: `/ws`
- ✅ STOMP enabled

## 🚀 How to Connect

### Step 1: Start Backend
```bash
cd network-watcher
mvn spring-boot:run
```
Backend runs on: **http://localhost:8080**

### Step 2: Start Frontend
```bash
cd network-watcher-frontend
npm run dev
```
Frontend runs on: **http://localhost:5173**

### Step 3: Test Connection

1. **Open browser**: http://localhost:5173
2. **Login page** should appear
3. **Try to login** with your credentials

## 🔍 Verify Backend Endpoints

### Test Authentication
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

Expected response:
```json
{
  "token": "eyJhbGc...",
  "user": {
    "id": 1,
    "username": "admin"
  }
}
```

### Test Devices Endpoint
```bash
curl http://localhost:8080/api/devices \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 📡 API Endpoints Available

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Register

### Devices
- `GET /api/devices` - List all devices
- `GET /api/devices/{id}` - Get device by ID
- `GET /api/devices/status/online` - Online devices
- `GET /api/devices/status/offline` - Offline devices
- `GET /api/devices/stats` - Device statistics

### Vulnerabilities
- `GET /api/vulnerabilities` - List all vulnerabilities
- `GET /api/vulnerabilities/{id}` - Get vulnerability by ID
- `GET /api/vulnerabilities/device/{deviceId}` - Device vulnerabilities
- `GET /api/vulnerabilities/severity/{severity}` - By severity

### Scans
- `GET /api/scans` - List all scans
- `GET /api/scans/{id}` - Get scan by ID
- `POST /api/scans/start` - Start new scan
- `POST /api/scans/{id}/stop` - Stop scan
- `GET /api/scans/active` - Get active scan

### Bandwidth
- `GET /api/bandwidth` - All bandwidth data
- `GET /api/bandwidth/device/{id}` - Device bandwidth
- `GET /api/bandwidth/summary` - Bandwidth summary
- `GET /api/bandwidth/recent?minutes=60` - Recent bandwidth

### Packets
- `GET /api/packets/recent?limit=100` - Recent packets
- `GET /api/packets/device/{deviceId}` - Device packets
- `GET /api/packets/stats` - Packet statistics

## 🔌 WebSocket Topics

### Connection
- **URL**: `ws://localhost:8080/ws`
- **Protocol**: STOMP

### Topics
- `/topic/devices` - Device updates
- `/topic/vulnerabilities` - New vulnerabilities
- `/topic/scan-progress` - Scan progress
- `/topic/alerts` - Alert notifications

## 🐛 Troubleshooting

### Issue: CORS Error
**Symptom**: "Access to XMLHttpRequest has been blocked by CORS policy"

**Solution**: Backend CORS is now configured for `http://localhost:5173`

### Issue: 401 Unauthorized
**Symptom**: All API calls return 401

**Solutions**:
1. Check if you're logged in
2. Verify JWT token in localStorage
3. Check token hasn't expired
4. Try logout and login again

### Issue: WebSocket Connection Failed
**Symptom**: "WebSocket connection failed"

**Solutions**:
1. Verify backend is running
2. Check WebSocket endpoint: `http://localhost:8080/ws`
3. Check browser console for errors

### Issue: Backend Not Starting
**Symptom**: Backend fails to start

**Solutions**:
1. Check port 8080 is not in use
2. Verify database connection
3. Check application.properties

### Issue: Frontend Not Connecting
**Symptom**: API calls timeout

**Solutions**:
1. Verify backend is running on port 8080
2. Check `src/utils/constants.js` has correct URL
3. Check browser Network tab for failed requests

## ✅ Connection Checklist

- [ ] Backend running on port 8080
- [ ] Frontend running on port 5173
- [ ] Can access http://localhost:5173
- [ ] Login page loads
- [ ] Can login successfully
- [ ] Dashboard loads after login
- [ ] Device data appears
- [ ] WebSocket connects (check console)
- [ ] No CORS errors in console
- [ ] No 401 errors in console

## 🎯 Quick Test

1. **Start backend**: `mvn spring-boot:run`
2. **Start frontend**: `npm run dev`
3. **Open**: http://localhost:5173
4. **Login**: Use your credentials
5. **Check**: Dashboard should show data

## 📝 Default Credentials

If you have DataInitializer:
- **Username**: `admin`
- **Password**: `admin123` (or check your DataInitializer)

## 🔐 Security Notes

- JWT tokens expire after configured time
- Tokens stored in localStorage
- HTTPS recommended for production
- Change default credentials in production

## 🚀 Production Deployment

### Backend
- Deploy to AWS/Heroku/DigitalOcean
- Update CORS to production frontend URL
- Use environment variables for secrets

### Frontend
- Build: `npm run build`
- Deploy `dist` folder to Vercel/Netlify
- Update `constants.js` with production API URL

---

**Status**: ✅ Backend and Frontend are now properly configured to connect!

**Next Step**: Start both applications and test the connection!
