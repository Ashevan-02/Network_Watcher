@echo off
echo Testing Backend Connection...
echo.

echo 1. Testing if backend is running...
curl -s http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d "{\"username\":\"test\",\"password\":\"test\"}"
echo.
echo.

echo 2. Testing with admin credentials...
curl -s http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
echo.
echo.

echo 3. If you see a token above, backend is working!
echo    If you see "Invalid credentials", the user might not exist in database.
echo.

pause
