-- Database Setup Script for FundQuest Auth Service
-- PostgreSQL Database Setup

-- Create database
CREATE DATABASE fundquest_auth;

-- Create user
CREATE USER fundquest_user WITH PASSWORD 'fundquest_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE fundquest_auth TO fundquest_user;

-- Connect to the database
\c fundquest_auth;

-- Grant schema privileges
GRANT ALL PRIVILEGES ON SCHEMA public TO fundquest_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO fundquest_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO fundquest_user;

-- Create extension for UUID generation (if not already available)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- The users table will be created automatically by Hibernate
-- But here's the expected structure for reference:

/*
CREATE TABLE users (
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
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_microsoft_id ON users(microsoft_id);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_last_login ON users(last_login);
*/

-- Insert sample data (optional - for testing)
-- This would typically be done after the application starts
/*
INSERT INTO users (email, name, microsoft_id, given_name, family_name, created_by, updated_by)
VALUES
('admin@fundquest.com', 'Admin User', 'admin-microsoft-id', 'Admin', 'User', 'SYSTEM', 'SYSTEM'),
('user@fundquest.com', 'Test User', 'test-microsoft-id', 'Test', 'User', 'SYSTEM', 'SYSTEM');
*/