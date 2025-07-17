-- Create extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create users table
CREATE TABLE users_tbl (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    microsoft_id VARCHAR(255) NOT NULL UNIQUE,
    given_name VARCHAR(255),
    family_name VARCHAR(255),
    preferred_username VARCHAR(255),
    job_title VARCHAR(255),
    department VARCHAR(255),
    office_location VARCHAR(255),
    mobile_phone VARCHAR(255),
    business_phones TEXT,
    profile_picture_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login TIMESTAMP,
    login_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Create indexes for performance
CREATE INDEX idx_users_email ON users_tbl(email);
CREATE INDEX idx_users_microsoft_id ON users_tbl(microsoft_id);
CREATE INDEX idx_users_active ON users_tbl(is_active);
CREATE INDEX idx_users_created_at ON users_tbl(created_at);
CREATE INDEX idx_users_last_login ON users_tbl(last_login);
CREATE INDEX idx_users_login_count ON users_tbl(login_count);

-- Create a partial index for active users
CREATE INDEX idx_users_active_email ON users_tbl(email) WHERE is_active = TRUE;
CREATE INDEX idx_users_active_microsoft_id ON users_tbl(microsoft_id) WHERE is_active = TRUE;

-- Add comments to table and columns
COMMENT ON TABLE users_tbl IS 'User accounts for FundQuest Auth Service';
COMMENT ON COLUMN users_tbl.id IS 'Primary key - UUID';
COMMENT ON COLUMN users_tbl.email IS 'User email address - unique';
COMMENT ON COLUMN users_tbl.name IS 'User display name';
COMMENT ON COLUMN users_tbl.microsoft_id IS 'Microsoft Azure AD Object ID - unique';
COMMENT ON COLUMN users_tbl.given_name IS 'User first name from Microsoft';
COMMENT ON COLUMN users_tbl.family_name IS 'User last name from Microsoft';
COMMENT ON COLUMN users_tbl.preferred_username IS 'Preferred username from Microsoft';
COMMENT ON COLUMN users_tbl.job_title IS 'User job title from Microsoft';
COMMENT ON COLUMN users_tbl.department IS 'User department from Microsoft';
COMMENT ON COLUMN users_tbl.office_location IS 'User office location from Microsoft';
COMMENT ON COLUMN users_tbl.mobile_phone IS 'User mobile phone from Microsoft';
COMMENT ON COLUMN users_tbl.business_phones IS 'User business phones from Microsoft (JSON array as text)';
COMMENT ON COLUMN users_tbl.profile_picture_url IS 'URL to user profile picture';
COMMENT ON COLUMN users_tbl.is_active IS 'Whether user account is active';
COMMENT ON COLUMN users_tbl.is_email_verified IS 'Whether user email is verified';
COMMENT ON COLUMN users_tbl.last_login IS 'Timestamp of last login';
COMMENT ON COLUMN users_tbl.login_count IS 'Number of times user has logged in';
COMMENT ON COLUMN users_tbl.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN users_tbl.updated_at IS 'Timestamp when record was last updated';
COMMENT ON COLUMN users_tbl.created_by IS 'User or system that created the record';
COMMENT ON COLUMN users_tbl.updated_by IS 'User or system that last updated the record';