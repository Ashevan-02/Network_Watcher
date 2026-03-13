# 🚀 START HERE - Quick Setup

## ✅ Backend and Frontend are NOW LINKED!

I've updated your backend CORS configuration to allow the frontend to connect.

## 🎯 Start Both Applications

### Terminal 1 - Backend
```bash
cd "c:\Users\Cordial space\Downloads\network-watcher\network-watcher"
mvn spring-boot:run
```
Wait for: "Started NetworkWatcherApplication"

### Terminal 2 - Frontend
```bash
cd "c:\Users\Cordial space\Desktop\networkwatcher_Frontend\network-watcher-frontend"
npm run dev
```
Wait for: "Local: http://localhost:5173"

## 🌐 Open Application

Open browser: **http://localhost:5173**

## 🔐 Login

Use your backend credentials (check DataInitializer.java for default user)

## ✅ What's Connected

- ✅ CORS configured for `http://localhost:5173`
- ✅ WebSocket endpoint `/ws` accessible
- ✅ All API endpoints under `/api/**`
- ✅ JWT authentication working
- ✅ Frontend configured to call `http://localhost:8080/api`

## 🎉 You're Ready!

Once both are running, the frontend will automatically connect to the backend!

---

**Need help?** Check `CONNECTION_GUIDE.md` for detailed troubleshooting.
