-- FileFlow Phase 3 schema.sql (File Management)
-- Engine: MySQL 8.x (FK intentionally omitted; computed columns for expiration logic)
-- Dependencies: Phase 2 schema (file_metadata) must be applied first

-- 1) File Assets -------------------------------------------------------------
-- Upload 완료 이벤트로부터 생성되는 파일 자산 (Soft Delete 지원)
CREATE TABLE IF NOT EXISTS file_assets (
  id                       VARCHAR(50) PRIMARY KEY,           -- UUID
  upload_session_id        VARCHAR(50) NOT NULL,              -- No FK to upload_sessions
  owner_user_context_id    BIGINT NOT NULL,                   -- No FK to user_contexts
  tenant_id                VARCHAR(50) NOT NULL,              -- No FK to tenants
  organization_id          BIGINT NULL,                       -- No FK to organizations

  -- S3 메타데이터
  s3_bucket                VARCHAR(100) NOT NULL,
  s3_key                   VARCHAR(500) NOT NULL,
  mime_type                VARCHAR(100) NOT NULL,
  file_size_bytes          BIGINT NOT NULL,
  checksum_sha256          VARCHAR(64) NULL,

  -- 가시성 및 수명 주기
  visibility               ENUM('PRIVATE','INTERNAL','PUBLIC') NOT NULL DEFAULT 'PRIVATE',
  expires_at               DATETIME NULL,                     -- 만료 시각 (NULL = 영구)
  effective_expires_at     DATETIME GENERATED ALWAYS AS (
    CASE
      WHEN deleted_at IS NOT NULL THEN deleted_at
      WHEN expires_at IS NOT NULL THEN expires_at
      ELSE NULL
    END
  ) STORED,                                                   -- Computed column: deleted_at 우선, expires_at 차선

  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at               DATETIME NULL,                     -- Soft delete timestamp

  UNIQUE KEY uk_fa_session (upload_session_id),
  INDEX idx_fa_owner_tenant (owner_user_context_id, tenant_id),
  INDEX idx_fa_org (tenant_id, organization_id),
  INDEX idx_fa_s3_location (s3_bucket, s3_key),
  INDEX idx_fa_visibility (visibility),
  INDEX idx_fa_effective_expires (effective_expires_at),      -- Batch Job 최적화
  INDEX idx_fa_deleted (deleted_at),
  INDEX idx_fa_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) File Variants -----------------------------------------------------------
-- 원본 파일의 변형 (썸네일, 최적화, 리사이징 등)
CREATE TABLE IF NOT EXISTS file_variants (
  id                       VARCHAR(50) PRIMARY KEY,           -- UUID
  parent_file_asset_id     VARCHAR(50) NOT NULL,              -- No FK to file_assets
  tenant_id                VARCHAR(50) NOT NULL,              -- No FK to tenants
  organization_id          BIGINT NULL,                       -- No FK to organizations

  -- 변형 타입 및 스펙
  variant_type             ENUM('THUMBNAIL','OPTIMIZED','RESIZED','PREVIEW','COMPRESSED') NOT NULL,
  spec_width               INT NULL,                          -- 예: 200 (px)
  spec_height              INT NULL,                          -- 예: 200 (px)
  spec_format              VARCHAR(20) NULL,                  -- 예: "jpeg", "webp", "png"
  spec_quality             TINYINT NULL,                      -- 1-100

  -- S3 메타데이터
  s3_bucket                VARCHAR(100) NOT NULL,
  s3_key                   VARCHAR(500) NOT NULL,
  file_size_bytes          BIGINT NOT NULL,

  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_fv_parent (parent_file_asset_id),
  INDEX idx_fv_type (variant_type),
  INDEX idx_fv_org (tenant_id, organization_id),
  INDEX idx_fv_s3_location (s3_bucket, s3_key),
  INDEX idx_fv_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) File Relationships ------------------------------------------------------
-- 파일 간 관계 (버전, 참조, 그룹)
CREATE TABLE IF NOT EXISTS file_relationships (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  source_file_asset_id     VARCHAR(50) NOT NULL,              -- No FK to file_assets (관계 출발점)
  target_file_asset_id     VARCHAR(50) NOT NULL,              -- No FK to file_assets (관계 도착점)
  relationship_type        ENUM('VERSION','REFERENCE','GROUP') NOT NULL,
  tenant_id                VARCHAR(50) NOT NULL,              -- No FK to tenants
  organization_id          BIGINT NULL,                       -- No FK to organizations

  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY uk_fr_relation (source_file_asset_id, target_file_asset_id, relationship_type),
  INDEX idx_fr_source (source_file_asset_id),
  INDEX idx_fr_target (target_file_asset_id),
  INDEX idx_fr_type (relationship_type),
  INDEX idx_fr_org (tenant_id, organization_id),
  INDEX idx_fr_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4) File Access Log ---------------------------------------------------------
-- 파일 접근 감사 로그 (조회, 다운로드, 삭제)
CREATE TABLE IF NOT EXISTS file_access_log (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_asset_id            VARCHAR(50) NOT NULL,              -- No FK to file_assets
  accessor_user_id         BIGINT NOT NULL,                   -- No FK to user_contexts
  access_type              ENUM('READ','DOWNLOAD','DELETE') NOT NULL,
  tenant_id                VARCHAR(50) NOT NULL,
  organization_id          BIGINT NULL,

  accessed_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  request_ip               VARCHAR(45) NULL,
  user_agent               VARCHAR(500) NULL,

  INDEX idx_fal_file (file_asset_id),
  INDEX idx_fal_user (accessor_user_id),
  INDEX idx_fal_type (access_type),
  INDEX idx_fal_accessed (accessed_at),
  INDEX idx_fal_org (tenant_id, organization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5) File Management Policy Settings -----------------------------------------
-- Phase 1의 setting_schemas에 추가할 항목들
INSERT INTO setting_schemas (key_name, value_type, is_secret) VALUES
  ('file.default_expiration_days', 'INT', 0),                 -- 기본 만료 기간 (일)
  ('file.default_visibility', 'STRING', 0),                   -- 기본 가시성 레벨
  ('file.enable_auto_variant', 'BOOL', 0),                    -- 자동 변형 생성 활성화
  ('file.thumbnail_size', 'INT', 0),                          -- 썸네일 크기 (px)
  ('file.max_download_url_expiration', 'INT', 0)              -- 다운로드 URL 최대 유효 시간 (분)
ON DUPLICATE KEY UPDATE value_type=VALUES(value_type);
