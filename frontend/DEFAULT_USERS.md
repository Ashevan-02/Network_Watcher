# 👤 Default Users

## Login Credentials

### Admin User
- **Username**: `admin`
- **Password**: `admin123`
- **Email**: admin@networkwatcher.com
- **Role**: ROLE_ADMIN (Full access)

## Available Roles

1. **ROLE_ADMIN** - Administrator with full access
2. **ROLE_ANALYST** - Security analyst
3. **ROLE_OPERATOR** - Network operator
4. **ROLE_VIEWER** - Read-only viewer

## How to Login

1. Open: http://localhost:5173
2. Enter username: `admin`
3. Enter password: `admin123`
4. Click "Login"

## Create New Users

Currently, only the admin user is created by default. To create more users, you can:

1. Add them in `DataInitializer.java`
2. Create a user registration endpoint
3. Use the database directly

## Security Note

⚠️ **Change the default password in production!**

The default credentials are:
- Username: `admin`
- Password: `admin123`

These should be changed immediately in a production environment.
