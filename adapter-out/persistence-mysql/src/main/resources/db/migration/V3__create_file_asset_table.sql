-- ========================================
-- V3: Create file_asset table
-- ========================================

CREATE TABLE file_asset (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    bucket VARCHAR(63) NOT NULL,
    s3_key VARCHAR(1024) NOT NULL,
    etag VARCHAR(64) NOT NULL,
    user_id BIGINT NULL,
    organization_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_file_asset_session_id (session_id),
    INDEX idx_file_asset_organization_id (organization_id),
    INDEX idx_file_asset_tenant_id (tenant_id),
    INDEX idx_file_asset_status (status),
    INDEX idx_file_asset_created_at (created_at),
    INDEX idx_file_asset_org_tenant_created (organization_id, tenant_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
