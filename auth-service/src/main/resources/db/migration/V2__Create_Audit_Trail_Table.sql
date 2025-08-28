-- V2__Create_Audit_Trail_Table.sql
-- Creates audit trail table for comprehensive activity logging

CREATE TABLE audit_trail_tbl (
    id BIGSERIAL PRIMARY KEY,

    -- User Information
    user_email VARCHAR(255),
    user_name VARCHAR(255),
    user_role VARCHAR(100),

    -- Action Information
    action_type VARCHAR(100) NOT NULL, -- e.g., 'CREATE', 'UPDATE', 'DELETE', 'ACTIVATE', 'DEACTIVATE'
    action_description TEXT NOT NULL, -- Detailed description of what was done

    -- Resource Information
    resource_type VARCHAR(100), -- e.g., 'USER', 'PERMISSION', 'ROLE'
    resource_id VARCHAR(255), -- ID of the affected resource
    resource_identifier VARCHAR(255), -- Human-readable identifier (email, name, etc.)

    -- Request Information
    endpoint VARCHAR(500), -- The API endpoint that was called
    http_method VARCHAR(10), -- POST, PUT, DELETE, etc.
    request_parameters TEXT, -- JSON string of request parameters

    -- Timestamp Information
    initiated_date DATE NOT NULL,
    initiated_time TIME NOT NULL,
    initiated_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Additional Context
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id VARCHAR(255),

    -- Status Information
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS', -- SUCCESS, FAILED, PARTIAL
    error_message TEXT, -- Only populated if status is FAILED

    -- Metadata
    service_name VARCHAR(100) DEFAULT 'auth-service',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_audit_user_email ON audit_trail_tbl(user_email);
CREATE INDEX idx_audit_action_type ON audit_trail_tbl(action_type);
CREATE INDEX idx_audit_resource_type ON audit_trail_tbl(resource_type);
CREATE INDEX idx_audit_initiated_timestamp ON audit_trail_tbl(initiated_timestamp);
CREATE INDEX idx_audit_initiated_date ON audit_trail_tbl(initiated_date);
CREATE INDEX idx_audit_status ON audit_trail_tbl(status);
CREATE INDEX idx_audit_service_name ON audit_trail_tbl(service_name);

-- Composite indexes for common query patterns
CREATE INDEX idx_audit_user_date ON audit_trail_tbl(user_email, initiated_date);
CREATE INDEX idx_audit_action_date ON audit_trail_tbl(action_type, initiated_date);
CREATE INDEX idx_audit_resource_date ON audit_trail_tbl(resource_type, initiated_date);

-- Add comments for documentation
COMMENT ON TABLE audit_trail_tbl IS 'Comprehensive audit trail for all system activities';
COMMENT ON COLUMN audit_trail_tbl.action_type IS 'High-level categorization of the action performed';
COMMENT ON COLUMN audit_trail_tbl.action_description IS 'Human-readable description of what was done';
COMMENT ON COLUMN audit_trail_tbl.resource_type IS 'Type of resource that was affected';
COMMENT ON COLUMN audit_trail_tbl.resource_id IS 'Technical ID of the affected resource';
COMMENT ON COLUMN audit_trail_tbl.resource_identifier IS 'Human-readable identifier of the affected resource';
COMMENT ON COLUMN audit_trail_tbl.status IS 'Whether the operation completed successfully';
COMMENT ON COLUMN audit_trail_tbl.service_name IS 'Name of the microservice that performed the action';