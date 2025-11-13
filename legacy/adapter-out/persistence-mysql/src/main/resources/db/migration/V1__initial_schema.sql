-- ================================================================
-- Fileflow Initial Schema Migration
-- ================================================================
-- Description: Create all tables for Fileflow application
-- Author: Claude Code
-- Version: 1.0
-- Date: 2025-11-03
-- ================================================================

-- ================================================================
-- IAM Tables (Identity & Access Management)
-- ================================================================

-- Tenants Table (Multi-tenancy support)
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- Organizations Table (Organizational hierarchy)
CREATE TABLE organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    org_code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_tenant_org_code UNIQUE (tenant_id, org_code)
);

-- User Contexts Table (User identity)
CREATE TABLE user_contexts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_user_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- Permissions Table (Atomic permissions for RBAC)
CREATE TABLE permissions (
    code VARCHAR(100) PRIMARY KEY,
    description VARCHAR(500) NOT NULL,
    default_scope VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME NULL
);

-- Roles Table (Role definitions for RBAC)
CREATE TABLE roles (
    code VARCHAR(100) PRIMARY KEY,
    description VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME NULL
);

-- Role Permissions Table (N:M join table for Roles and Permissions)
CREATE TABLE role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(100) NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    CONSTRAINT uk_role_permission UNIQUE (role_code, permission_code)
);

-- ================================================================
-- File Management Tables
-- ================================================================

-- Upload Session Table (Track upload session lifecycle)
CREATE TABLE upload_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_key VARCHAR(100) NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    upload_type VARCHAR(20) NOT NULL,
    storage_key VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    completed_at DATETIME NULL
);

-- Multipart Upload Table (Track multipart upload state for large files)
CREATE TABLE upload_multipart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    upload_session_id BIGINT NOT NULL,
    provider_upload_id VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL,
    total_parts INT NULL,
    started_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    aborted_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- Upload Part Table (Individual multipart upload parts)
CREATE TABLE upload_part (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    multipart_upload_id BIGINT NOT NULL,
    part_number INT NOT NULL,
    etag VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    checksum VARCHAR(255) NULL,
    uploaded_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_multipart_part UNIQUE (multipart_upload_id, part_number)
);

-- External Download Table (Track external URL download progress)
CREATE TABLE external_download (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    upload_session_id BIGINT NOT NULL,
    source_url VARCHAR(2000) NOT NULL,
    bytes_transferred BIGINT NULL,
    total_bytes BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NULL,
    last_retry_at DATETIME NULL,
    error_code VARCHAR(50) NULL,
    error_message TEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- File Assets Table (Core file metadata with comprehensive indexing)
CREATE TABLE file_assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT NULL,
    owner_user_id BIGINT NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(150) NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    checksum_sha256 VARCHAR(64) NULL,
    upload_session_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    visibility VARCHAR(20) NOT NULL,
    uploaded_at DATETIME NOT NULL,
    processed_at DATETIME NULL,
    expires_at DATETIME NULL,
    retention_days INT NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- File Variants Table (Thumbnails, Previews, Transcoded versions)
CREATE TABLE file_variants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id BIGINT NOT NULL,
    parent_file_asset_id BIGINT NULL,
    variant_type VARCHAR(50) NOT NULL,
    variant_key VARCHAR(255) NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(150) NOT NULL,
    width INT NULL,
    height INT NULL,
    duration_seconds INT NULL,
    bitrate_kbps INT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_file_variant UNIQUE (file_id, variant_type, variant_key)
);

-- Extracted Data Table (Metadata extraction results)
CREATE TABLE extracted_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    extracted_uuid VARCHAR(36) NOT NULL,
    file_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    extraction_type VARCHAR(20) NOT NULL,
    extraction_method VARCHAR(20) NOT NULL,
    version INT NOT NULL,
    trace_id VARCHAR(100) NULL,
    text_data TEXT NULL,
    structured_data TEXT NULL,
    preview_data TEXT NULL,
    confidence_score DOUBLE NULL,
    quality_score DOUBLE NULL,
    validation_status VARCHAR(20) NOT NULL,
    notes TEXT NULL,
    extracted_at DATETIME NOT NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_business_key UNIQUE (file_id, extraction_type, extraction_method, version)
);

-- ================================================================
-- Settings Table (EAV Pattern for Multi-Tenant Configuration)
-- ================================================================

-- Settings Table (Entity-Attribute-Value Pattern)
CREATE TABLE settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT NOT NULL,
    setting_type VARCHAR(20) NOT NULL,
    level VARCHAR(20) NOT NULL,
    context_id BIGINT NULL,
    is_secret BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_setting_key_level_context UNIQUE (setting_key, level, context_id)
);

-- ================================================================
-- Event-Driven Architecture Tables (Outbox Pattern)
-- ================================================================

-- Pipeline Outbox Table (Transactional outbox for thumbnail/metadata processing)
CREATE TABLE pipeline_outbox (
    pipeline_outbox_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    file_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- External Download Outbox Table (Transactional outbox for download events)
CREATE TABLE external_download_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    download_id BIGINT NOT NULL,
    upload_session_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);


-- ================================================================
-- Indexes (H2 Compatibility)
-- ================================================================
-- H2 does not support inline INDEX syntax in CREATE TABLE.
-- All indexes are created separately for cross-database compatibility.
-- ================================================================

CREATE INDEX idx_tenants_status ON tenants (status);
CREATE INDEX idx_tenants_deleted ON tenants (deleted);
CREATE INDEX idx_organizations_tenant_id ON organizations (tenant_id);
CREATE INDEX idx_organizations_status ON organizations (status);
CREATE INDEX idx_organizations_deleted ON organizations (deleted);
CREATE INDEX idx_user_contexts_email ON user_contexts (email);
CREATE INDEX idx_user_contexts_deleted ON user_contexts (deleted);
CREATE INDEX idx_role_permissions_role_code ON role_permissions (role_code);
CREATE INDEX idx_role_permissions_permission_code ON role_permissions (permission_code);
CREATE INDEX idx_upload_session_tenant_id ON upload_session (tenant_id);
CREATE INDEX idx_upload_session_status ON upload_session (status);
CREATE INDEX idx_upload_session_session_key ON upload_session (session_key);
CREATE INDEX idx_upload_multipart_upload_session_id ON upload_multipart (upload_session_id);
CREATE INDEX idx_upload_multipart_status ON upload_multipart (status);
CREATE INDEX idx_upload_part_multipart_upload_id ON upload_part (multipart_upload_id);
CREATE INDEX idx_external_download_upload_session_id ON external_download (upload_session_id);
CREATE INDEX idx_external_download_status ON external_download (status);
CREATE INDEX idx_file_assets_tenant_org_uploaded ON file_assets (tenant_id, organization_id, uploaded_at);
CREATE INDEX idx_file_assets_owner ON file_assets (owner_user_id);
CREATE INDEX idx_file_assets_status ON file_assets (status);
CREATE INDEX idx_file_assets_deleted ON file_assets (deleted_at);
CREATE INDEX idx_file_assets_upload_session_id ON file_assets (upload_session_id);
CREATE INDEX idx_file_assets_storage_key ON file_assets (storage_key);
CREATE INDEX idx_file_variants_file_id ON file_variants (file_id);
CREATE INDEX idx_file_variants_variant_type ON file_variants (variant_type);
CREATE INDEX idx_extracted_data_file ON extracted_data (file_id);
CREATE INDEX idx_extracted_data_tenant ON extracted_data (tenant_id, organization_id);
CREATE INDEX idx_extracted_data_trace ON extracted_data (trace_id);
CREATE INDEX idx_extracted_data_extracted_uuid ON extracted_data (extracted_uuid);
CREATE INDEX idx_settings_setting_key ON settings (setting_key);
CREATE INDEX idx_settings_level ON settings (level);
CREATE INDEX idx_settings_context_id ON settings (context_id);
CREATE INDEX idx_pipeline_outbox_status_created ON pipeline_outbox (status, created_at);
CREATE INDEX idx_pipeline_outbox_file_id ON pipeline_outbox (file_id);
CREATE INDEX idx_pipeline_outbox_idempotency_key ON pipeline_outbox (idempotency_key);
CREATE INDEX idx_external_download_outbox_status_created ON external_download_outbox (status, created_at);
CREATE INDEX idx_external_download_outbox_download_id ON external_download_outbox (download_id);
CREATE INDEX idx_external_download_outbox_idempotency_key ON external_download_outbox (idempotency_key);
-- ================================================================
-- Flyway Schema History (자동 생성됨)
-- ================================================================
-- Flyway가 자동으로 flyway_schema_history 테이블을 생성하므로
-- 여기서는 명시적으로 생성하지 않습니다.
-- ================================================================
