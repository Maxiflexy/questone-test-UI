-- V1__Create_Initial_RBAC_Tables.sql
-- Initial database schema for Flexible Role-Based Access Control System
-- Creates all necessary tables for users, roles, permissions, and their relationships

-- Create permissions table
CREATE TABLE permission_tbl (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    category VARCHAR(100),
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create roles table (roles are labels/categories)
CREATE TABLE role_tbl (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    level INTEGER, -- For hierarchical purposes (1 = highest)
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create users table - FIXED: Use VARCHAR instead of UUID for compatibility with String ID in Java
CREATE TABLE user_tbl (
    id VARCHAR(255) PRIMARY KEY,
    microsoft_id VARCHAR(255) UNIQUE,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    preferred_username VARCHAR(255),
    is_invited BOOLEAN DEFAULT false NOT NULL,
    is_microsoft_verified BOOLEAN DEFAULT false NOT NULL,
    role_id BIGINT NOT NULL,
    last_login TIMESTAMP,
    invited_by VARCHAR(255),
    invited_at TIMESTAMP,
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role_tbl(id)
);

-- Create user-permission junction table - FIXED: Use VARCHAR to match user_tbl.id
CREATE TABLE user_permission_tbl (
    user_id VARCHAR(255) NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, permission_id),
    CONSTRAINT fk_user_permission_user FOREIGN KEY (user_id) REFERENCES user_tbl(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_permission_permission FOREIGN KEY (permission_id) REFERENCES permission_tbl(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_user_email ON user_tbl(email);
CREATE INDEX idx_user_microsoft_id ON user_tbl(microsoft_id);
CREATE INDEX idx_user_role_id ON user_tbl(role_id);
CREATE INDEX idx_user_invited ON user_tbl(is_invited);
CREATE INDEX idx_user_active ON user_tbl(is_active);
CREATE INDEX idx_permission_name ON permission_tbl(name);
CREATE INDEX idx_permission_category ON permission_tbl(category);
CREATE INDEX idx_permission_active ON permission_tbl(is_active);
CREATE INDEX idx_role_name ON role_tbl(name);
CREATE INDEX idx_role_level ON role_tbl(level);
CREATE INDEX idx_role_active ON role_tbl(is_active);
CREATE INDEX idx_user_permission_user_id ON user_permission_tbl(user_id);
CREATE INDEX idx_user_permission_permission_id ON user_permission_tbl(permission_id);

-- Insert default permissions
INSERT INTO permission_tbl (name, description, category) VALUES
-- Admin Management Permissions
('INVITE_ADMIN', 'Invite Admin', 'ADMIN_MANAGEMENT'),
('VIEW_OTHER_ADMIN_USERS', 'View other Admin Users', 'ADMIN_MANAGEMENT'),
('EDIT_ADMIN_PROFILE', 'Edit an Admin Profile', 'ADMIN_MANAGEMENT'),
('VIEW_ADMIN', 'View Admin', 'ADMIN_MANAGEMENT'),
('ASSIGN_AND_UNASSIGN', 'Assign and unassign', 'ADMIN_MANAGEMENT'),
('DISABLE_ENABLE_ADMIN', 'Disable and enable an Admin', 'ADMIN_MANAGEMENT'),
('APPROVE_DECLINE_ADMIN_REQUEST', 'Approve or decline an Admin request', 'ADMIN_MANAGEMENT'),

-- Customer Management Permissions
('VIEW_CUSTOMERS', 'View Customers', 'CUSTOMER_MANAGEMENT'),
('EDIT_CUSTOMER_PROFILE', 'Edit a Customer profile', 'CUSTOMER_MANAGEMENT'),
('RESET_CUSTOMER_PASSWORD', 'Reset a customer''s password', 'CUSTOMER_MANAGEMENT'),
('DISABLE_ENABLE_CUSTOMER', 'Disable and enable a customer', 'CUSTOMER_MANAGEMENT');

-- Insert default roles
INSERT INTO role_tbl (name, description, level) VALUES
('SUPER_ADMIN', 'Super Administrator', 1),
('ADMIN', 'Administrator', 2);

-- Insert default super admin user - FIXED: Generate UUID-like string for ID
INSERT INTO user_tbl (id, email, name, role_id, is_invited, is_microsoft_verified)
SELECT
    REPLACE(gen_random_uuid()::text, '-', ''), -- Generate UUID-like string without hyphens
    'onyekachi.ejemba@fundquestnigeria.com',
    'Super Administrator',
    r.id,
    true,
    false
FROM role_tbl r
WHERE r.name = 'SUPER_ADMIN';

-- Assign all permissions to super admin user
INSERT INTO user_permission_tbl (user_id, permission_id)
SELECT
    u.id,
    p.id
FROM user_tbl u
CROSS JOIN permission_tbl p
WHERE u.email = 'onyekachi.ejemba@fundquestnigeria.com'
AND p.is_active = true;

-- Add comments for documentation
COMMENT ON TABLE permission_tbl IS 'Stores all available permissions in the system';
COMMENT ON TABLE role_tbl IS 'Stores roles which act as labels/categories for users';
COMMENT ON TABLE user_tbl IS 'Stores user information with role and invitation status';
COMMENT ON TABLE user_permission_tbl IS 'Junction table for flexible user-specific permission assignments';

COMMENT ON COLUMN user_tbl.role_id IS 'References role_tbl.id - role acts as a label, not permission definer';
COMMENT ON COLUMN user_tbl.is_invited IS 'Whether user has been invited to access the system';
COMMENT ON COLUMN user_tbl.is_microsoft_verified IS 'Whether user has completed Microsoft OAuth verification';
COMMENT ON COLUMN user_tbl.microsoft_id IS 'Microsoft OID from OAuth token';
COMMENT ON COLUMN permission_tbl.category IS 'Groups permissions by functional area';
COMMENT ON COLUMN role_tbl.level IS 'Hierarchical level - lower numbers = higher authority';