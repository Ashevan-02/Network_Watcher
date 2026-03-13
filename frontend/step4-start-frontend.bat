@echo off
echo ========================================
echo STEP 4: Starting Frontend
echo ========================================
echo.

cd /d "c:\Users\Cordial space\Desktop\networkwatcher_Frontend\network-watcher-frontend"

echo Checking if Node.js is installed...
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js is NOT installed
    echo Please install Node.js from: https://nodejs.org
    pause
    exit /b 1
)

echo ✅ Node.js found!
echo.

echo Checking if dependencies are installed...
if not exist "node_modules" (
    echo Installing dependencies...
    echo This will take 2-3 minutes...
    call npm install
)

echo.
echo Starting frontend...
echo.
echo ⚠️ DO NOT CLOSE THIS WINDOW!
echo.
echo Browser will open automatically at: http://localhost:5173
echo.

npm run dev
