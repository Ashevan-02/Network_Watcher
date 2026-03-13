@echo off
echo ========================================
echo STEP 2: Check Nmap Installation
echo ========================================
echo.

echo Checking if Nmap is installed...
echo.

nmap --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Nmap is NOT installed
    echo.
    echo You need to install Nmap:
    echo 1. Go to: https://nmap.org/download.html
    echo 2. Download "Latest stable release self-installer"
    echo 3. Install with default settings
    echo 4. Restart this script
    echo.
    echo Opening download page...
    start https://nmap.org/download.html
    pause
    exit /b 1
) else (
    echo ✅ Nmap is installed!
    nmap --version
    echo.
    echo Ready to scan!
)

echo.
pause
