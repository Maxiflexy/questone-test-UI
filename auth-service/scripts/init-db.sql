-- Database initialization script for FundQuest Auth Service
-- This script runs when the PostgreSQL container starts for the first time

-- Create extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create extension for crypto functions (if needed in future)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set timezone to UTC
SET timezone = 'UTC';

-- Create a schema for application tables (optional)
-- CREATE SCHEMA IF NOT EXISTS auth;

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE fundquest_auth TO fundquest_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO fundquest_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO fundquest_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO fundquest_user;

-- Create default admin user (optional - for testing)
-- This will be created by the application during first login
-- INSERT INTO users_tbl (email, name, microsoft_id, given_name, family_name, created_by, updated_by)
-- VALUES ('admin@fundquest.com', 'Admin User', 'admin-microsoft-id', 'Admin', 'User', 'SYSTEM', 'SYSTEM')
-- ON CONFLICT (email) DO NOTHING;

-- Log successful initialization
DO $$
BEGIN
    RAISE NOTICE 'FundQuest Auth Service database initialized successfully';
END $$;