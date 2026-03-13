@echo off
echo ========================================
echo STEP 1: Check Your Connection
echo ========================================
echo.

echo Your network information:
echo.
ipconfig | findstr /C:"Wireless LAN adapter Wi-Fi" /C:"IPv4 Address" /C:"Default Gateway"

echo.
echo ========================================
echo Look for:
echo   IPv4 Address: 192.168.x.x
echo   Default Gateway: 192.168.x.1
echo.
echo Write down your IPv4 Address: ________________
echo.
pause
