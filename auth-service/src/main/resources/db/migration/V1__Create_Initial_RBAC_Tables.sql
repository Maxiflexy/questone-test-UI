-- V1__Create_Initial_RBAC_Tables.sql
-- Initial database schema for Hierarchical Role-Based Access Control System
-- Creates all necessary tables for users, roles, permission groups, categories, permissions, and their relationships

-- Create permission groups table (top level)
CREATE TABLE permission_groups_tbl (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create categories table (middle level)
CREATE TABLE categories_tbl (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    perm_group_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_permission_group FOREIGN KEY (perm_group_id) REFERENCES permission_groups_tbl(id)
);

-- Create permissions table (bottom level)
CREATE TABLE permission_tbl (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    category_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_permission_category FOREIGN KEY (category_id) REFERENCES categories_tbl(id)
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

-- Create users table
CREATE TABLE user_tbl (
    id VARCHAR(255) PRIMARY KEY,
    microsoft_id VARCHAR(255) UNIQUE,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    preferred_username VARCHAR(255),
    phone_number VARCHAR(20),
    is_invited BOOLEAN DEFAULT false NOT NULL,
    is_microsoft_verified BOOLEAN DEFAULT false NOT NULL,
    role_id BIGINT NOT NULL,
    last_login TIMESTAMP,
    invited_by VARCHAR(255),
    invited_at TIMESTAMP,
    last_modified_by VARCHAR(255),
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role_tbl(id)
);

-- Create user-permission junction table
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
CREATE INDEX idx_user_phone_number ON user_tbl(phone_number);
CREATE INDEX idx_user_last_modified_by ON user_tbl(last_modified_by);

CREATE INDEX idx_permission_group_name ON permission_groups_tbl(name);
CREATE INDEX idx_permission_group_active ON permission_groups_tbl(is_active);

CREATE INDEX idx_category_name ON categories_tbl(name);
CREATE INDEX idx_category_perm_group_id ON categories_tbl(perm_group_id);
CREATE INDEX idx_category_active ON categories_tbl(is_active);

CREATE INDEX idx_permission_name ON permission_tbl(name);
CREATE INDEX idx_permission_category_id ON permission_tbl(category_id);
CREATE INDEX idx_permission_active ON permission_tbl(is_active);

CREATE INDEX idx_role_name ON role_tbl(name);
CREATE INDEX idx_role_level ON role_tbl(level);
CREATE INDEX idx_role_active ON role_tbl(is_active);

CREATE INDEX idx_user_permission_user_id ON user_permission_tbl(user_id);
CREATE INDEX idx_user_permission_permission_id ON user_permission_tbl(permission_id);

-- Insert permission groups
INSERT INTO permission_groups_tbl (name, description) VALUES
('Admin Permissions', 'Administrative permissions for system management'),
('Digital Banking (Retail)', 'Digital banking permissions for retail operations');

-- Insert categories
INSERT INTO categories_tbl (name, perm_group_id) VALUES
-- Admin Permissions categories
('Main Admin Permissions', (SELECT id FROM permission_groups_tbl WHERE name = 'Admin Permissions')),
('Other Admin Permissions', (SELECT id FROM permission_groups_tbl WHERE name = 'Admin Permissions')),
-- Digital Banking categories
('Customers', (SELECT id FROM permission_groups_tbl WHERE name = 'Digital Banking (Retail)')),
('Wallet and Transactions Activities', (SELECT id FROM permission_groups_tbl WHERE name = 'Digital Banking (Retail)')),
('Retail Loans', (SELECT id FROM permission_groups_tbl WHERE name = 'Digital Banking (Retail)')),
('Cards', (SELECT id FROM permission_groups_tbl WHERE name = 'Digital Banking (Retail)')),
('Investments/Deposits', (SELECT id FROM permission_groups_tbl WHERE name = 'Digital Banking (Retail)'));

-- Insert permissions under Main Admin Permissions category
INSERT INTO permission_tbl (name, description, category_id) VALUES
('VIEW_OTHER_ADMIN_USERS', 'Can view other Admin Users', (SELECT id FROM categories_tbl WHERE name = 'Main Admin Permissions')),
('INVITE_ADMIN', 'Can Invite Admin', (SELECT id FROM categories_tbl WHERE name = 'Main Admin Permissions')),
('VIEW_ADMIN_PERMISSIONS', 'Can view Admin Permissions', (SELECT id FROM categories_tbl WHERE name = 'Main Admin Permissions')),
('INITIATE_PERMISSION_ASSIGNMENT', 'Can initiate the assignment and unassignment of permissions', (SELECT id FROM categories_tbl WHERE name = 'Main Admin Permissions')),
('APPROVE_PERMISSION_ASSIGNMENT', 'Can review and approve permission assignment and unassignment requests', (SELECT id FROM categories_tbl WHERE name = 'Main Admin Permissions')),
('APPROVE_ADMIN_STATUS_CHANGE', 'Can review and approve requests to enable or disable an Admin account', (SELECT id FROM categories_tbl WHERE name = 'Main Admin Permissions'));

-- Insert permissions under Other Admin Permissions category
INSERT INTO permission_tbl (name, description, category_id) VALUES
('INITIATE_ADMIN_STATUS_CHANGE', 'Can initiate the request to enable or disable an Admin account', (SELECT id FROM categories_tbl WHERE name = 'Other Admin Permissions')),
('INITIATE_PERMISSION_CREATION', 'Can initiate permission creation', (SELECT id FROM categories_tbl WHERE name = 'Other Admin Permissions'));

-- Insert permissions under Customers category
INSERT INTO permission_tbl (name, description, category_id) VALUES
('CREATE_CUSTOMER', 'Can Create a Customer', (SELECT id FROM categories_tbl WHERE name = 'Customers')),
('VIEW_CUSTOMER_PROFILES', 'View Customer Profiles', (SELECT id FROM categories_tbl WHERE name = 'Customers')),
('INITIATE_CUSTOMER_PROFILE_UPDATE', 'Can initiate a customer profile update (Can edit address, update doc, update tier, BVN, etc)', (SELECT id FROM categories_tbl WHERE name = 'Customers')),
('APPROVE_CUSTOMER_PROFILE_UPDATE', 'Can review/approve a customer profile update (Can edit address, update doc, BVN, etc)', (SELECT id FROM categories_tbl WHERE name = 'Customers')),
('INITIATE_CUSTOMER_PASSWORD_RESET', 'Can initiate a Customer reset Password', (SELECT id FROM categories_tbl WHERE name = 'Customers')),
('APPROVE_CUSTOMER_PASSWORD_RESET', 'Can approve a Customer reset Password', (SELECT id FROM categories_tbl WHERE name = 'Customers')),
('INITIATE_CUSTOMER_STATUS_CHANGE', 'Can initiate a request to disable or enable a customer account', (SELECT id FROM categories_tbl WHERE name = 'Customers')),
('APPROVE_CUSTOMER_STATUS_CHANGE', 'Can review and approve customer disable/enable requests', (SELECT id FROM categories_tbl WHERE name = 'Customers')),
('INITIATE_DEVICE_UNASSIGNMENT', 'Can initiate unassigning a device from a customer account', (SELECT id FROM categories_tbl WHERE name = 'Customers')),
('APPROVE_DEVICE_UNASSIGNMENT', 'Can approve the unassignment of a device from a customer account', (SELECT id FROM categories_tbl WHERE name = 'Customers'));

-- Insert permissions under Wallet and Transactions Activities category
INSERT INTO permission_tbl (name, description, category_id) VALUES
('VIEW_CUSTOMER_WALLETS_TRANSACTIONS', 'View Customer Wallets and Transactions logs', (SELECT id FROM categories_tbl WHERE name = 'Wallet and Transactions Activities')),
('INITIATE_CUSTOMER_CREDIT_DEBIT', 'Can Initiate a Credit or Debit a Customer Account', (SELECT id FROM categories_tbl WHERE name = 'Wallet and Transactions Activities')),
('APPROVE_CUSTOMER_CREDIT_DEBIT', 'Can review or approve a credit or debit to a Customer Account', (SELECT id FROM categories_tbl WHERE name = 'Wallet and Transactions Activities')),
('PUT_CUSTOMER_ACCOUNT_PND', 'Can put a Customer Account on PND', (SELECT id FROM categories_tbl WHERE name = 'Wallet and Transactions Activities')),
('UPDATE_CUSTOMER_MIN_BALANCE', 'Can update a Customer Account Minimum Balance', (SELECT id FROM categories_tbl WHERE name = 'Wallet and Transactions Activities')),
('DOWNLOAD_CUSTOMER_STATEMENT', 'Can download a Customer statement of Account', (SELECT id FROM categories_tbl WHERE name = 'Wallet and Transactions Activities'));

-- Insert permissions under Retail Loans category
INSERT INTO permission_tbl (name, description, category_id) VALUES
('VIEW_CUSTOMER_LOAN_HISTORY', 'Can view a Customer Loan History', (SELECT id FROM categories_tbl WHERE name = 'Retail Loans')),
('APPROVE_CUSTOMER_LOAN', 'Can approve a Customer Loan', (SELECT id FROM categories_tbl WHERE name = 'Retail Loans')),
('INITIATE_MANUAL_LOAN_CREDIT_DEBIT', 'Can initiate a manual Loan Credit or Debit', (SELECT id FROM categories_tbl WHERE name = 'Retail Loans')),
('APPROVE_MANUAL_LOAN_CREDIT_DEBIT', 'Can approve a manual loan, Credit, or Debit', (SELECT id FROM categories_tbl WHERE name = 'Retail Loans'));

-- Insert permissions under Cards category
INSERT INTO permission_tbl (name, description, category_id) VALUES
('VIEW_CUSTOMER_CARD_HISTORY', 'Can view a Customer Card history', (SELECT id FROM categories_tbl WHERE name = 'Cards')),
('INITIATE_CARD_ACTIVATION_DEACTIVATION', 'Can initiate a card activation or deactivation', (SELECT id FROM categories_tbl WHERE name = 'Cards')),
('APPROVE_CARD_ACTIVATION_DEACTIVATION', 'Can approve a card activation or deactivation', (SELECT id FROM categories_tbl WHERE name = 'Cards'));

-- Insert permissions under Investments/Deposits category
INSERT INTO permission_tbl (name, description, category_id) VALUES
('VIEW_CUSTOMER_INVESTMENTS', 'Can view Customer Investments', (SELECT id FROM categories_tbl WHERE name = 'Investments/Deposits')),
('INITIATE_INVESTMENT_DEBIT', 'Can Initiate a Debit for a Customer Investment', (SELECT id FROM categories_tbl WHERE name = 'Investments/Deposits')),
('APPROVE_INVESTMENT_DEBIT', 'Can approve a Debit for a Customer Investment', (SELECT id FROM categories_tbl WHERE name = 'Investments/Deposits'));

-- Insert default roles
INSERT INTO role_tbl (name, description, level) VALUES
('SUPER_ADMIN', 'Super Administrator with full system access', 1),
('ADMIN', 'Administrator with delegated permissions', 2);

-- Insert default super admin user
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

-- Assign only Main Admin Permissions to super admin user
INSERT INTO user_permission_tbl (user_id, permission_id)
SELECT
    u.id,
    p.id
FROM user_tbl u
CROSS JOIN permission_tbl p
JOIN categories_tbl c ON p.category_id = c.id
WHERE u.email = 'onyekachi.ejemba@fundquestnigeria.com'
AND c.name = 'Main Admin Permissions'
AND p.is_active = true
AND c.is_active = true;

-- Add comments for documentation
COMMENT ON TABLE permission_groups_tbl IS 'Top-level grouping of permissions (e.g., Admin Permissions, Digital Banking)';
COMMENT ON TABLE categories_tbl IS 'Middle-level categorization of permissions within groups';
COMMENT ON TABLE permission_tbl IS 'Individual permissions belonging to categories';
COMMENT ON TABLE role_tbl IS 'Stores roles which act as labels/categories for users';
COMMENT ON TABLE user_tbl IS 'Stores user information with role and invitation status';
COMMENT ON TABLE user_permission_tbl IS 'Junction table for flexible user-specific permission assignments';

COMMENT ON COLUMN categories_tbl.perm_group_id IS 'References permission_groups_tbl.id';
COMMENT ON COLUMN permission_tbl.category_id IS 'References categories_tbl.id';
COMMENT ON COLUMN user_tbl.role_id IS 'References role_tbl.id - role acts as a label, not permission definer';
COMMENT ON COLUMN user_tbl.is_invited IS 'Whether user has been invited to access the system';
COMMENT ON COLUMN user_tbl.is_microsoft_verified IS 'Whether user has completed Microsoft OAuth verification';
COMMENT ON COLUMN user_tbl.microsoft_id IS 'Microsoft OID from OAuth token';
COMMENT ON COLUMN user_tbl.phone_number IS 'User phone number for contact purposes';
COMMENT ON COLUMN user_tbl.last_modified_by IS 'Email of the user who last modified this record';