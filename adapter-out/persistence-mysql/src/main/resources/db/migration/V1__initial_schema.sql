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
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Tenant unique identifier',
    name VARCHAR(200) NOT NULL COMMENT 'Tenant name',
    status VARCHAR(20) NOT NULL COMMENT 'Tenant status: ACTIVE, SUSPENDED',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Soft delete flag',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Multi-tenant organization table';

-- Organizations Table (Organizational hierarchy)
CREATE TABLE organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Organization unique identifier',
    tenant_id BIGINT NOT NULL COMMENT 'FK to tenants.id (Long FK Strategy)',
    org_code VARCHAR(50) NOT NULL COMMENT 'Organization code (unique within tenant)',
    name VARCHAR(200) NOT NULL COMMENT 'Organization name',
    status VARCHAR(20) NOT NULL COMMENT 'Organization status: ACTIVE, INACTIVE',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Soft delete flag',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    UNIQUE KEY uk_tenant_org_code (tenant_id, org_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Organizational hierarchy table';

-- User Contexts Table (User identity)
CREATE TABLE user_contexts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'User context unique identifier',
    external_user_id VARCHAR(255) NOT NULL UNIQUE COMMENT 'IDP user ID (e.g., auth0|abc123)',
    email VARCHAR(255) NOT NULL COMMENT 'User email address',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Soft delete flag',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    INDEX idx_email (email),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User identity context table';

-- Permissions Table (Atomic permissions for RBAC)
CREATE TABLE permissions (
    code VARCHAR(100) PRIMARY KEY COMMENT 'Permission code (e.g., file.upload, user.read)',
    description VARCHAR(500) NOT NULL COMMENT 'Permission description',
    default_scope VARCHAR(20) NOT NULL COMMENT 'Default scope: SYSTEM, TENANT, ORGANIZATION, SELF',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    deleted_at DATETIME NULL COMMENT 'Soft delete timestamp (null = active)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RBAC atomic permissions table';

-- Roles Table (Role definitions for RBAC)
CREATE TABLE roles (
    code VARCHAR(100) PRIMARY KEY COMMENT 'Role code (e.g., org.uploader, tenant.admin)',
    description VARCHAR(500) NOT NULL COMMENT 'Role description',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    deleted_at DATETIME NULL COMMENT 'Soft delete timestamp (null = active)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RBAC roles table';

-- Role Permissions Table (N:M join table for Roles and Permissions)
CREATE TABLE role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Role-Permission mapping unique identifier',
    role_code VARCHAR(100) NOT NULL COMMENT 'FK to roles.code',
    permission_code VARCHAR(100) NOT NULL COMMENT 'FK to permissions.code',
    INDEX idx_role_code (role_code),
    INDEX idx_permission_code (permission_code),
    UNIQUE KEY uk_role_permission (role_code, permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role-Permission mapping table (N:M)';

-- ================================================================
-- File Management Tables
-- ================================================================

-- Upload Session Table (Track upload session lifecycle)
CREATE TABLE upload_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Upload session unique identifier',
    session_key VARCHAR(100) NOT NULL UNIQUE COMMENT 'Unique session key',
    tenant_id BIGINT NOT NULL COMMENT 'FK to tenants.id (Long FK Strategy)',
    file_name VARCHAR(500) NOT NULL COMMENT 'Original file name',
    file_size BIGINT NOT NULL COMMENT 'File size in bytes',
    upload_type VARCHAR(20) NOT NULL COMMENT 'Upload type: SINGLE, MULTIPART, EXTERNAL_URL',
    storage_key VARCHAR(500) NULL COMMENT 'S3 object key',
    status VARCHAR(20) NOT NULL COMMENT 'Session status: INITIATED, IN_PROGRESS, COMPLETED, FAILED, EXPIRED',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_session_key (session_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Upload session lifecycle tracking table';

-- Multipart Upload Table (Track multipart upload state for large files)
CREATE TABLE upload_multipart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Multipart upload unique identifier',
    upload_session_id BIGINT NOT NULL COMMENT 'FK to upload_session.id (Long FK Strategy)',
    provider_upload_id VARCHAR(500) NULL COMMENT 'S3 multipart upload ID',
    status VARCHAR(20) NOT NULL COMMENT 'Multipart status: INITIATED, IN_PROGRESS, COMPLETED, FAILED, ABORTED',
    total_parts INT NULL COMMENT 'Total number of parts',
    started_at DATETIME NOT NULL COMMENT 'Multipart upload start timestamp',
    completed_at DATETIME NULL COMMENT 'Multipart upload completion timestamp',
    aborted_at DATETIME NULL COMMENT 'Multipart upload abortion timestamp',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    INDEX idx_upload_session_id (upload_session_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Multipart upload state tracking table';

-- Upload Part Table (Individual multipart upload parts)
CREATE TABLE upload_part (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Upload part unique identifier',
    multipart_upload_id BIGINT NOT NULL COMMENT 'FK to upload_multipart.id (Long FK Strategy)',
    part_number INT NOT NULL COMMENT 'Part number (1-based)',
    etag VARCHAR(255) NOT NULL COMMENT 'S3 ETag for part',
    size BIGINT NOT NULL COMMENT 'Part size in bytes',
    checksum VARCHAR(255) NULL COMMENT 'Part checksum (optional)',
    uploaded_at DATETIME NOT NULL COMMENT 'Part upload completion timestamp',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    INDEX idx_multipart_upload_id (multipart_upload_id),
    UNIQUE KEY uk_multipart_part (multipart_upload_id, part_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Multipart upload individual parts table';

-- External Download Table (Track external URL download progress)
CREATE TABLE external_download (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'External download unique identifier',
    upload_session_id BIGINT NOT NULL COMMENT 'FK to upload_session.id (Long FK Strategy)',
    source_url VARCHAR(2000) NOT NULL COMMENT 'External source URL',
    bytes_transferred BIGINT NULL COMMENT 'Bytes transferred so far',
    total_bytes BIGINT NULL COMMENT 'Total bytes to download',
    status VARCHAR(20) NOT NULL COMMENT 'Download status: PENDING, IN_PROGRESS, COMPLETED, FAILED, RETRYING',
    retry_count INT NULL COMMENT 'Number of retry attempts',
    last_retry_at DATETIME NULL COMMENT 'Last retry timestamp',
    error_code VARCHAR(50) NULL COMMENT 'Error code if failed',
    error_message TEXT NULL COMMENT 'Error message if failed',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    INDEX idx_upload_session_id (upload_session_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='External URL download tracking table';

-- File Assets Table (Core file metadata with comprehensive indexing)
CREATE TABLE file_assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'File asset unique identifier',
    tenant_id BIGINT NOT NULL COMMENT 'FK to tenants.id (Long FK Strategy)',
    organization_id BIGINT NULL COMMENT 'FK to organizations.id (Long FK Strategy)',
    owner_user_id BIGINT NOT NULL COMMENT 'FK to user_contexts.id (Long FK Strategy)',
    file_name VARCHAR(500) NOT NULL COMMENT 'File name',
    file_size BIGINT NOT NULL COMMENT 'File size in bytes',
    mime_type VARCHAR(150) NOT NULL COMMENT 'MIME type (e.g., image/jpeg)',
    storage_key VARCHAR(512) NOT NULL COMMENT 'S3 object key',
    checksum_sha256 VARCHAR(64) NULL COMMENT 'SHA-256 checksum for integrity',
    upload_session_id BIGINT NOT NULL COMMENT 'FK to upload_session.id (Long FK Strategy)',
    status VARCHAR(20) NOT NULL COMMENT 'File status: PROCESSING, AVAILABLE, DELETED, ERROR',
    visibility VARCHAR(20) NOT NULL COMMENT 'Visibility: PRIVATE, INTERNAL, PUBLIC',
    uploaded_at DATETIME NOT NULL COMMENT 'Upload completion timestamp',
    processed_at DATETIME NULL COMMENT 'Processing completion timestamp',
    expires_at DATETIME NULL COMMENT 'Expiration timestamp (null = no expiration)',
    retention_days INT NULL COMMENT 'Retention period in days',
    deleted_at DATETIME NULL COMMENT 'Soft delete timestamp (null = active)',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    INDEX idx_tenant_org_uploaded (tenant_id, organization_id, uploaded_at),
    INDEX idx_owner (owner_user_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted_at),
    INDEX idx_upload_session_id (upload_session_id),
    INDEX idx_storage_key (storage_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='File metadata with comprehensive indexing';

-- File Variants Table (Thumbnails, Previews, Transcoded versions)
CREATE TABLE file_variants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'File variant unique identifier',
    file_id BIGINT NOT NULL COMMENT 'FK to file_assets.id (Long FK Strategy)',
    variant_type VARCHAR(50) NOT NULL COMMENT 'Variant type: THUMBNAIL, PREVIEW, TRANSCODED',
    variant_key VARCHAR(255) NOT NULL COMMENT 'Variant key (e.g., thumbnail_small, preview_720p)',
    storage_key VARCHAR(512) NOT NULL COMMENT 'S3 object key for variant',
    file_size BIGINT NOT NULL COMMENT 'Variant file size in bytes',
    mime_type VARCHAR(150) NOT NULL COMMENT 'Variant MIME type',
    width INT NULL COMMENT 'Image/Video width in pixels',
    height INT NULL COMMENT 'Image/Video height in pixels',
    duration_seconds INT NULL COMMENT 'Video/Audio duration in seconds',
    bitrate_kbps INT NULL COMMENT 'Video/Audio bitrate in kbps',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    INDEX idx_file_id (file_id),
    INDEX idx_variant_type (variant_type),
    UNIQUE KEY uk_file_variant (file_id, variant_type, variant_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='File variants (thumbnails, previews, transcoded versions)';

-- Extracted Data Table (Metadata extraction results)
CREATE TABLE extracted_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Extracted data unique identifier',
    extracted_uuid VARCHAR(36) NOT NULL COMMENT 'Business UUID for extracted data',
    file_id BIGINT NOT NULL COMMENT 'FK to file_assets.id (Long FK Strategy)',
    tenant_id BIGINT NOT NULL COMMENT 'FK to tenants.id (Long FK Strategy)',
    organization_id BIGINT NOT NULL COMMENT 'FK to organizations.id (Long FK Strategy)',
    extraction_type VARCHAR(20) NOT NULL COMMENT 'Extraction type: TEXT, METADATA, PREVIEW',
    extraction_method VARCHAR(20) NOT NULL COMMENT 'Extraction method: OCR, NLP, EXIF, MANUAL',
    version INT NOT NULL COMMENT 'Extraction version',
    trace_id VARCHAR(100) NULL COMMENT 'Trace ID for debugging',
    text_data TEXT NULL COMMENT 'Extracted text data (JSON)',
    structured_data TEXT NULL COMMENT 'Structured metadata (JSON)',
    preview_data TEXT NULL COMMENT 'Preview data (JSON)',
    confidence_score DOUBLE NULL COMMENT 'Confidence score (0.0 ~ 1.0)',
    quality_score DOUBLE NULL COMMENT 'Quality score (0.0 ~ 1.0)',
    validation_status VARCHAR(20) NOT NULL COMMENT 'Validation status: PENDING, VALIDATED, REJECTED',
    notes TEXT NULL COMMENT 'Additional notes',
    extracted_at DATETIME NOT NULL COMMENT 'Extraction timestamp',
    deleted_at DATETIME NULL COMMENT 'Soft delete timestamp',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    INDEX idx_file (file_id),
    INDEX idx_tenant (tenant_id, organization_id),
    INDEX idx_trace (trace_id),
    INDEX idx_extracted_uuid (extracted_uuid),
    UNIQUE KEY uk_business_key (file_id, extraction_type, extraction_method, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Extracted metadata and text data';

-- ================================================================
-- Event-Driven Architecture Tables (Outbox Pattern)
-- ================================================================

-- Pipeline Outbox Table (Transactional outbox for thumbnail/metadata processing)
CREATE TABLE pipeline_outbox (
    pipeline_outbox_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Pipeline outbox unique identifier',
    idempotency_key VARCHAR(255) NOT NULL UNIQUE COMMENT 'Idempotency key (pattern: fileAsset-{fileAssetId})',
    file_id BIGINT NOT NULL COMMENT 'FK to file_assets.id (Long FK Strategy)',
    status VARCHAR(20) NOT NULL COMMENT 'Outbox status: PENDING, PROCESSING, COMPLETED, FAILED',
    retry_count INT NOT NULL DEFAULT 0 COMMENT 'Number of retry attempts',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL COMMENT 'Record last update timestamp',
    INDEX idx_status_created (status, created_at),
    INDEX idx_file_id (file_id),
    INDEX idx_idempotency_key (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Pipeline processing outbox (thumbnail + metadata)';

-- External Download Outbox Table (Transactional outbox for download events)
CREATE TABLE external_download_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'External download outbox unique identifier',
    idempotency_key VARCHAR(255) NOT NULL UNIQUE COMMENT 'Idempotency key for deduplication',
    download_id BIGINT NOT NULL COMMENT 'FK to external_download.id (Long FK Strategy)',
    upload_session_id BIGINT NOT NULL COMMENT 'FK to upload_session.id (Long FK Strategy)',
    status VARCHAR(50) NOT NULL COMMENT 'Outbox status: PENDING, PROCESSING, COMPLETED, FAILED',
    retry_count INT NOT NULL DEFAULT 0 COMMENT 'Number of retry attempts',
    created_at DATETIME NOT NULL COMMENT 'Record creation timestamp',
    INDEX idx_status_created (status, created_at),
    INDEX idx_download_id (download_id),
    INDEX idx_idempotency_key (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='External download event outbox';

-- ================================================================
-- Flyway Schema History (자동 생성됨)
-- ================================================================
-- Flyway가 자동으로 flyway_schema_history 테이블을 생성하므로
-- 여기서는 명시적으로 생성하지 않습니다.
-- ================================================================
