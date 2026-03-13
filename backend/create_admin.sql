-- Create admin user manually in PostgreSQL
-- Run this with: psql -U postgres -d networkwatcher -f create_admin.sql

-- First, create roles if they don't exist
INSERT INTO roles (name, description) 
VALUES ('ROLE_ADMIN', 'Administrator with full access')
ON CONFLICT DO NOTHING;

INSERT INTO roles (name, description) 
VALUES ('ROLE_ANALYST', 'Security analyst')
ON CONFLICT DO NOTHING;

INSERT INTO roles (name, description) 
VALUES ('ROLE_OPERATOR', 'Network operator')
ON CONFLICT DO NOTHING;

INSERT INTO roles (name, description) 
VALUES ('ROLE_VIEWER', 'Read-only viewer')
ON CONFLICT DO NOTHING;

-- Create admin user
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (username, password, email, enabled) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@networkwatcher.com', true)
ON CONFLICT (username) DO NOTHING;

-- Link admin user to ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- Verify user was created
SELECT u.username, u.email, u.enabled, r.name as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin';
