@echo off
echo ========================================
echo STEP 3: Starting Backend Server
echo ========================================
echo.

cd /d "c:\Users\Cordial space\Downloads\network-watcher\network-watcher"

echo Checking if Java is installed...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java is NOT installed
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

echo ✅ Java found!
echo.

echo Starting Spring Boot backend...
echo This will take 30-60 seconds...
echo.
echo ⚠️ DO NOT CLOSE THIS WINDOW!
echo.
echo Wait for message: "Started NetworkWatcherApplication"
echo.

mvnw.cmd spring-boot:run
