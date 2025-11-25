-- V1__create_session_tables.sql
-- FileFlow Session Management Tables

-- ============================================
-- Single Upload Session Table
-- ============================================
CREATE TABLE single_upload_session (
    id VARCHAR(36) NOT NULL,
    idempotency_key VARCHAR(36) NOT NULL,
    user_id BIGINT NULL,
    organization_id BIGINT NOT NULL,
    organization_name VARCHAR(100) NOT NULL,
    organization_namespace VARCHAR(50) NOT NULL,
    tenant_id BIGINT NOT NULL,
    tenant_name VARCHAR(50) NOT NULL,
    user_role VARCHAR(20) NOT NULL,
    email VARCHAR(255) NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    bucket VARCHAR(63) NOT NULL,
    s3_key VARCHAR(1024) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    presigned_url VARCHAR(2048) NULL,
    etag VARCHAR(64) NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    -- Idempotency key uniqueness per organization
    UNIQUE INDEX uk_single_session_idempotency (organization_id, idempotency_key),

    -- Query by organization and status
    INDEX idx_single_session_org_status (organization_id, status),

    -- Query by user
    INDEX idx_single_session_user (user_id),

    -- Expired session cleanup
    INDEX idx_single_session_expires (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================
-- Multipart Upload Session Table
-- ============================================
CREATE TABLE multipart_upload_session (
    id VARCHAR(36) NOT NULL,
    user_id BIGINT NULL,
    organization_id BIGINT NOT NULL,
    organization_name VARCHAR(100) NOT NULL,
    organization_namespace VARCHAR(50) NOT NULL,
    tenant_id BIGINT NOT NULL,
    tenant_name VARCHAR(50) NOT NULL,
    user_role VARCHAR(20) NOT NULL,
    email VARCHAR(255) NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    bucket VARCHAR(63) NOT NULL,
    s3_key VARCHAR(1024) NOT NULL,
    s3_upload_id VARCHAR(256) NOT NULL,
    total_parts INT NOT NULL,
    part_size BIGINT NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    merged_etag VARCHAR(64) NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    -- S3 Upload ID lookup
    INDEX idx_multipart_session_s3_upload (s3_upload_id),

    -- Query by organization and status
    INDEX idx_multipart_session_org_status (organization_id, status),

    -- Query by user
    INDEX idx_multipart_session_user (user_id),

    -- Expired session cleanup
    INDEX idx_multipart_session_expires (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================
-- Completed Part Table
-- ============================================
CREATE TABLE completed_part (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id VARCHAR(36) NOT NULL,
    part_number INT NOT NULL,
    presigned_url VARCHAR(2048) NOT NULL,
    etag VARCHAR(64) NOT NULL,
    size BIGINT NOT NULL,
    uploaded_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    -- Unique constraint: one part number per session
    UNIQUE INDEX uk_completed_part_session_part (session_id, part_number),

    -- Query all parts for a session
    INDEX idx_completed_part_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
