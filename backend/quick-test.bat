@echo off
echo ========================================
echo   Network Watcher - Quick WiFi Test
echo ========================================
echo.

echo [1/4] Detecting your network...
echo.

REM Get IP address and calculate network range
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4 Address"') do (
    set IP=%%a
    goto :found
)

:found
set IP=%IP: =%
echo Your IP: %IP%

REM Extract first 3 octets for network range
for /f "tokens=1,2,3 delims=." %%a in ("%IP%") do (
    set NETWORK=%%a.%%b.%%c.0/24
)

echo Network Range: %NETWORK%
echo.

echo [2/4] Checking if Nmap is installed...
nmap --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Nmap not found!
    echo Please install from: https://nmap.org/download.html
    pause
    exit /b 1
)
echo Nmap found!
echo.

echo [3/4] Checking if backend is running...
curl -s http://localhost:8080/api/devices >nul 2>&1
if errorlevel 1 (
    echo WARNING: Backend not running on port 8080
    echo Please start backend first:
    echo   cd network-watcher
    echo   mvnw.cmd spring-boot:run
    echo.
    pause
    exit /b 1
)
echo Backend is running!
echo.

echo [4/4] Starting network scan...
echo This will scan: %NETWORK%
echo Scanning 254 possible IP addresses...
echo This may take 2-5 minutes...
echo.

REM Note: You need to get JWT token first by logging in
echo To scan, you need to:
echo 1. Login to http://localhost:5173
echo 2. Go to Scans page
echo 3. Enter network range: %NETWORK%
echo 4. Click "Start Scan"
echo.

echo OR use this curl command (after getting JWT token):
echo curl -X POST "http://localhost:8080/api/scan/network?range=%NETWORK%" -H "Authorization: Bearer YOUR_TOKEN"
echo.

echo ========================================
echo Opening frontend in browser...
echo ========================================
start http://localhost:5173

pause
