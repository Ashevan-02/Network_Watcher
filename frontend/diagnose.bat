@echo off
echo ========================================
echo Network Watcher Diagnostic
echo ========================================
echo.

echo Checking Node.js...
node --version
echo.

echo Checking npm...
npm --version
echo.

echo Checking if node_modules exists...
if exist node_modules (
    echo [OK] node_modules folder exists
) else (
    echo [ERROR] node_modules folder missing - run: npm install
)
echo.

echo Checking critical files...
if exist src\App.jsx (echo [OK] App.jsx) else (echo [ERROR] App.jsx missing)
if exist src\main.jsx (echo [OK] main.jsx) else (echo [ERROR] main.jsx missing)
if exist src\hooks\useNotification.js (echo [OK] useNotification.js) else (echo [ERROR] useNotification.js missing)
if exist src\components\BandwidthChart.jsx (echo [OK] BandwidthChart.jsx) else (echo [ERROR] BandwidthChart.jsx missing)
if exist src\services\bandwidthService.js (echo [OK] bandwidthService.js) else (echo [ERROR] bandwidthService.js missing)
echo.

echo Checking if port 5173 is in use...
netstat -ano | findstr :5173
if %errorlevel%==0 (
    echo [WARNING] Port 5173 is in use
) else (
    echo [OK] Port 5173 is free
)
echo.

echo Checking backend connection...
curl -s http://localhost:8080 >nul 2>&1
if %errorlevel%==0 (
    echo [OK] Backend is responding on port 8080
) else (
    echo [ERROR] Backend is not responding - start backend first
)
echo.

echo ========================================
echo Diagnostic Complete
echo ========================================
echo.
echo Next steps:
echo 1. If node_modules missing: npm install
echo 2. If backend not responding: Start backend first
echo 3. If port in use: Kill process or use different port
echo 4. Then run: npm run dev
echo.
pause
