# 🔐 Login Issues - Troubleshooting

## Issue: "Invalid credentials"

### Step 1: Check Backend is Running
Open a new terminal and run:
```bash
curl http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

**Expected response**:
```json
{"token":"eyJhbGc...","username":"admin"}
```

**If you get "Connection refused"**: Backend is not running
**If you get "Invalid credentials"**: User doesn't exist in database

### Step 2: Check Database
The backend uses PostgreSQL. Check if the database exists:

```bash
psql -U postgres -d networkwatcher -c "SELECT username FROM users;"
```

**If database doesn't exist**: Create it
```bash
psql -U postgres -c "CREATE DATABASE networkwatcher;"
```

### Step 3: Restart Backend
The DataInitializer only runs if the database is empty. If you had issues, try:

1. Stop backend (Ctrl+C)
2. Drop and recreate database:
   ```bash
   psql -U postgres -c "DROP DATABASE IF EXISTS networkwatcher;"
   psql -U postgres -c "CREATE DATABASE networkwatcher;"
   ```
3. Start backend again:
   ```bash
   cd "c:\Users\Cordial space\Downloads\network-watcher\network-watcher"
   mvn spring-boot:run
   ```
4. Wait for "Started NetworkWatcherApplication"
5. Check logs for "DataInitializer" - should create admin user

### Step 4: Check Backend Logs
Look for these in backend terminal:
- ✅ "Started NetworkWatcherApplication"
- ✅ "Hibernate: insert into users..."
- ❌ "Connection refused" (database not running)
- ❌ "Authentication failed" (wrong credentials)

### Step 5: Test with curl
```bash
# Test login
curl -v http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

Look for:
- Status code 200 = Success
- Status code 401 = Invalid credentials
- Status code 500 = Server error
- Connection refused = Backend not running

### Step 6: Check Frontend Network Tab
1. Open browser (F12)
2. Go to Network tab
3. Try to login
4. Look for the `/auth/login` request
5. Check:
   - Request URL: Should be `http://localhost:8080/api/auth/login`
   - Request Method: POST
   - Status: 200 (success) or 401 (invalid)
   - Response: Should have `token` and `username`

### Common Issues

#### Issue: Backend not starting
**Solution**: Check PostgreSQL is running
```bash
# Windows
net start postgresql-x64-14
```

#### Issue: Database doesn't exist
**Solution**: Create it
```bash
psql -U postgres -c "CREATE DATABASE networkwatcher;"
```

#### Issue: User not created
**Solution**: Check backend logs for DataInitializer execution

#### Issue: CORS error
**Solution**: Already fixed in WebConfig.java

#### Issue: Wrong password
**Solution**: Default is `admin123`, check DataInitializer.java

### Manual User Creation

If DataInitializer didn't run, create user manually:

```sql
-- Connect to database
psql -U postgres -d networkwatcher

-- Create role
INSERT INTO roles (name, description) VALUES ('ROLE_ADMIN', 'Administrator');

-- Create user (password is BCrypt hash of 'admin123')
INSERT INTO users (username, password, email, enabled) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@networkwatcher.com', true);

-- Link user to role
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username='admin' AND r.name='ROLE_ADMIN';
```

### Quick Test Script

Run `test-backend.bat` to test backend connection.

### Still Not Working?

Share:
1. Backend terminal output
2. Browser console errors (F12)
3. Network tab request/response (F12)
4. Result of: `psql -U postgres -d networkwatcher -c "SELECT * FROM users;"`
