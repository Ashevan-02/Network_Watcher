# 🔧 Fix: Create Admin User Manually

## The Problem
DataInitializer didn't create the admin user in the database.

## Solution: Create User Manually

### Option 1: Using SQL Script (Easiest)

1. **Open Command Prompt** and run:
   ```bash
   cd "c:\Users\Cordial space\Downloads\network-watcher\network-watcher"
   psql -U postgres -d networkwatcher -f create_admin.sql
   ```

2. **Check if user was created**:
   ```bash
   psql -U postgres -d networkwatcher -c "SELECT username, email FROM users;"
   ```
   
   You should see:
   ```
   username | email
   ----------+---------------------------
   admin    | admin@networkwatcher.com
   ```

3. **Try login again** in browser

---

### Option 2: Using psql Commands

1. **Connect to database**:
   ```bash
   psql -U postgres -d networkwatcher
   ```

2. **Run these commands**:
   ```sql
   -- Create role
   INSERT INTO roles (name, description) 
   VALUES ('ROLE_ADMIN', 'Administrator with full access');

   -- Create user (password is 'admin123')
   INSERT INTO users (username, password, email, enabled) 
   VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@networkwatcher.com', true);

   -- Link user to role
   INSERT INTO user_roles (user_id, role_id)
   SELECT u.id, r.id FROM users u, roles r 
   WHERE u.username='admin' AND r.name='ROLE_ADMIN';

   -- Verify
   SELECT * FROM users;
   ```

3. **Exit psql**: Type `\q` and press Enter

4. **Try login again**

---

### Option 3: Fix DataInitializer

If DataInitializer isn't running, check backend logs for errors.

**Make sure**:
- PostgreSQL is running
- Database `networkwatcher` exists
- Backend can connect to database

**Check backend logs for**:
- "DataInitializer" - should appear when it runs
- "Hibernate: insert into users" - confirms user creation
- Any errors about database connection

---

## Verify User Exists

Run this to check:
```bash
psql -U postgres -d networkwatcher -c "SELECT username, email, enabled FROM users WHERE username='admin';"
```

**Expected output**:
```
 username |          email           | enabled 
----------+--------------------------+---------
 admin    | admin@networkwatcher.com | t
```

If you see this, the user exists and login should work!

---

## After Creating User

1. **Restart backend** (just to be safe):
   ```bash
   cd "c:\Users\Cordial space\Downloads\network-watcher\network-watcher"
   mvn spring-boot:run
   ```

2. **Refresh browser** (Ctrl+F5)

3. **Login**:
   - Username: `admin`
   - Password: `admin123`

---

## Still Not Working?

Check:
1. Backend logs for authentication errors
2. Browser console (F12) for errors
3. Database has the user: `psql -U postgres -d networkwatcher -c "SELECT * FROM users;"`

Share the output and I'll help further!
