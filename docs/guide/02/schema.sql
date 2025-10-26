-- FileFlow Phase 2 schema.sql (Upload Management - Full Implementation)
-- Engine: MySQL 8.x (FK intentionally omitted; soft-delete + indexes included)
-- Based on: 01-upload-management.md (Epic Document)
-- Dependencies: Phase 1 schema (tenants, organizations, user_contexts) must be applied first

-- ============================================================================
-- 1) Upload Sessions (Extended)
-- ============================================================================
-- 업로드 세션 관리 (모든 업로드 타입의 통합 세션)
CREATE TABLE IF NOT EXISTS upload_sessions (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id               CHAR(27) NOT NULL UNIQUE,         -- "usn_xxx" 형태 (외부 노출 키)

  -- 멀티테넌트 경계
  tenant_id                VARCHAR(50) NOT NULL,             -- No FK to tenants
  organization_id          BIGINT NULL,                      -- No FK to organizations
  uploader_user_context_id BIGINT NOT NULL,                  -- No FK to user_contexts

  -- 파일 힌트 (클라이언트가 미리 생성한 UUID, 선택)
  file_id_hint             CHAR(36) NULL,

  -- 파일 메타데이터
  original_filename        VARCHAR(500) NOT NULL,
  expected_mime            VARCHAR(150) NULL,
  expected_size_bytes      BIGINT NULL,

  -- 저장소 정보
  storage_provider         ENUM('S3','GCS','AZURE','LOCAL') NOT NULL DEFAULT 'S3',
  storage_bucket           VARCHAR(200) NOT NULL,
  storage_key              VARCHAR(512) NOT NULL,

  -- 가시성
  visibility               ENUM('PRIVATE','INTERNAL','PUBLIC') NOT NULL DEFAULT 'PRIVATE',

  -- 정책 스냅샷 (IAM ABAC + 설정 평가 결과)
  policy_snapshot_json     JSON NULL,

  -- 세션 상태 (6가지)
  status                   ENUM('INIT','IN_PROGRESS','COMPLETED','ABORTED','EXPIRED','FAILED') NOT NULL DEFAULT 'INIT',

  -- Presigned URL 정보 (싱글파트용)
  presigned_url            VARCHAR(2000) NULL,
  presigned_expires_at     DATETIME NULL,

  -- 세션 수명
  expires_at               DATETIME NOT NULL,                -- 세션 TTL (예: 15분)

  -- 타임스탬프
  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  completed_at             DATETIME NULL,
  deleted_at               DATETIME NULL,                    -- Soft delete

  -- 인덱스
  UNIQUE KEY uk_session_id (session_id),
  INDEX idx_us_scope_status (tenant_id, organization_id, status),
  INDEX idx_us_uploader_time (uploader_user_context_id, created_at),
  INDEX idx_us_expires (expires_at),
  INDEX idx_us_status (status),
  INDEX idx_us_deleted (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 2) Upload Multipart (멀티파트 업로드 관리)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upload_multipart (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id               CHAR(27) NOT NULL,                -- upload_sessions.session_id (앱 레벨 참조)

  -- 스토리지 제공자의 멀티파트 업로드 ID (S3 UploadId 등)
  provider_upload_id       VARCHAR(200) NOT NULL,

  -- 파트 정보
  total_parts              INT NULL,                         -- 예상 파트 수
  uploaded_parts           INT NOT NULL DEFAULT 0,           -- 업로드 완료된 파트 수

  -- 상태
  status                   ENUM('INIT','IN_PROGRESS','COMPLETED','ABORTED','FAILED') NOT NULL DEFAULT 'INIT',

  -- 타임스탬프
  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  completed_at             DATETIME NULL,

  -- 인덱스
  UNIQUE KEY uk_um_session (session_id),
  INDEX idx_um_status (status),
  INDEX idx_um_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 3) Upload Parts (멀티파트 파트별 상태)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upload_parts (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id               CHAR(27) NOT NULL,                -- upload_sessions.session_id

  -- 파트 정보
  part_no                  INT NOT NULL,                     -- 파트 번호 (1부터 시작)

  -- S3 ETag (업로드 완료 후 반환)
  etag                     VARCHAR(200) NULL,

  -- 파트 크기
  size_bytes               BIGINT NULL,

  -- Presigned URL (발급 시점)
  presigned_url            VARCHAR(2000) NULL,
  presigned_expires_at     DATETIME NULL,

  -- 상태
  status                   ENUM('PRESIGNED','UPLOADED','COMPLETED','FAILED','EXPIRED') NOT NULL DEFAULT 'PRESIGNED',

  -- 타임스탬프
  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- 인덱스
  UNIQUE KEY uk_up_session_part (session_id, part_no),
  INDEX idx_up_session (session_id),
  INDEX idx_up_status (status),
  INDEX idx_up_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 4) Upload Chunks (청크 업로드, 선택 사항)
-- ============================================================================
-- 애플리케이션 레벨에서 작은 조각 단위로 업로드하는 경우
CREATE TABLE IF NOT EXISTS upload_chunks (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id               CHAR(27) NOT NULL,                -- upload_sessions.session_id

  -- 청크 정보
  chunk_seq                INT NOT NULL,                     -- 청크 순서 (0부터 시작)
  size_bytes               BIGINT NULL,

  -- 체크섬 (MD5)
  checksum_md5             CHAR(32) NULL,

  -- 상태
  status                   ENUM('RECEIVED','COMMITTED','FAILED','EXPIRED') NOT NULL DEFAULT 'RECEIVED',

  -- 타임스탬프
  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- 인덱스
  UNIQUE KEY uk_uc_session_chunk (session_id, chunk_seq),
  INDEX idx_uc_session (session_id),
  INDEX idx_uc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 5) External Downloads (서버 측 인게스트)
-- ============================================================================
-- 외부 URL에서 서버가 파일을 다운로드하여 업로드 대행
CREATE TABLE IF NOT EXISTS external_downloads (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id               CHAR(27) NOT NULL,                -- upload_sessions.session_id

  -- 소스 URL
  source_url               TEXT NOT NULL,

  -- 다운로드 진행 상태
  byte_transferred         BIGINT NOT NULL DEFAULT 0,
  total_bytes              BIGINT NULL,                      -- Content-Length (있는 경우)

  -- 상태
  status                   ENUM('INIT','DOWNLOADING','COMPLETED','FAILED','ABORTED') NOT NULL DEFAULT 'INIT',

  -- 에러 정보
  error_code               VARCHAR(50) NULL,                 -- ex) EXT-404, EXT-TIMEOUT
  error_message            VARCHAR(500) NULL,

  -- 재시도 정보
  retry_count              INT NOT NULL DEFAULT 0,
  max_retry                INT NOT NULL DEFAULT 3,

  -- 타임스탬프
  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  completed_at             DATETIME NULL,

  -- 인덱스
  UNIQUE KEY uk_ed_session (session_id),
  INDEX idx_ed_status (status),
  INDEX idx_ed_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 6) Upload Errors (에러 진단 로그)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upload_errors (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id               CHAR(27) NOT NULL,                -- upload_sessions.session_id

  -- 에러 정보
  error_code               VARCHAR(50) NOT NULL,             -- ex) UP-403-ABAC, UP-409-DUPSHA, UP-500-IO
  error_message            VARCHAR(500) NULL,
  error_detail             TEXT NULL,                        -- 스택 트레이스 등 (선택)

  -- 타임스탬프
  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  -- 인덱스
  INDEX idx_ue_session (session_id),
  INDEX idx_ue_code (error_code),
  INDEX idx_ue_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 7) Upload Policies (정책 템플릿)
-- ============================================================================
-- 조직별 업로드 정책 템플릿 (ABAC + 설정 통합)
CREATE TABLE IF NOT EXISTS upload_policies (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,

  -- 멀티테넌트 경계
  tenant_id                VARCHAR(50) NOT NULL,             -- No FK to tenants
  organization_id          BIGINT NULL,                      -- No FK to organizations (NULL = 테넌트 레벨)

  -- 정책 이름
  policy_name              VARCHAR(100) NOT NULL,

  -- 허용 MIME 타입 (JSON Array)
  allowed_mime_json        JSON NULL,                        -- ex) ["image/jpeg","image/png","application/pdf"]

  -- 최대 파일 크기 (MB)
  max_size_mb              INT NULL,

  -- 시간대 제한 (0~23시)
  time_window_start        TINYINT NULL,
  time_window_end          TINYINT NULL,

  -- 속도 제한 (분당 업로드 수)
  rate_limit_per_min       INT NULL,

  -- 쿼터 제한 (일일 업로드 용량, MB)
  daily_quota_mb           INT NULL,

  -- 활성화 여부
  is_active                BOOLEAN NOT NULL DEFAULT TRUE,

  -- 타임스탬프
  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- 인덱스
  UNIQUE KEY uk_upol_scope_name (tenant_id, organization_id, policy_name),
  INDEX idx_upol_tenant (tenant_id),
  INDEX idx_upol_org (organization_id),
  INDEX idx_upol_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 8) File Metadata (기존 테이블 유지, 이벤트 연동용)
-- ============================================================================
-- Phase 3에서 file_assets로 마이그레이션 예정
-- Phase 2에서는 UploadCompletedEvent 수신 후 생성
CREATE TABLE IF NOT EXISTS file_metadata (
  id                       VARCHAR(50) PRIMARY KEY,           -- UUID
  upload_session_id        CHAR(27) NOT NULL,                 -- upload_sessions.session_id (앱 레벨 참조)
  owner_user_context_id    BIGINT NOT NULL,                   -- No FK to user_contexts
  tenant_id                VARCHAR(50) NOT NULL,              -- No FK to tenants
  organization_id          BIGINT NULL,                       -- No FK to organizations
  original_filename        VARCHAR(500) NOT NULL,
  mime_type                VARCHAR(100) NOT NULL,
  file_size_bytes          BIGINT NOT NULL,
  s3_bucket                VARCHAR(100) NOT NULL,
  s3_key                   VARCHAR(500) NOT NULL,
  s3_version_id            VARCHAR(100) NULL,                 -- S3 Object Version ID (optional)
  checksum_sha256          VARCHAR(64) NULL,                  -- File integrity check
  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at               DATETIME NULL,                     -- Soft delete timestamp
  UNIQUE KEY uk_fm_s3_location (s3_bucket, s3_key),
  INDEX idx_fm_owner_tenant (owner_user_context_id, tenant_id),
  INDEX idx_fm_org (tenant_id, organization_id),
  INDEX idx_fm_deleted (deleted_at),
  INDEX idx_fm_created (created_at),
  INDEX idx_fm_session (upload_session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 9) Upload Session Events (이벤트 로그, 감사용)
-- ============================================================================
CREATE TABLE IF NOT EXISTS upload_session_events (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id               CHAR(27) NOT NULL,                 -- upload_sessions.session_id
  event_type               ENUM('CREATED','UPLOADING','COMPLETED','FAILED','EXPIRED','ABORTED') NOT NULL,
  previous_status          VARCHAR(20) NULL,
  new_status               VARCHAR(20) NOT NULL,
  reason                   TEXT NULL,                         -- 실패/만료 사유
  occurred_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_use_session (session_id),
  INDEX idx_use_type (event_type),
  INDEX idx_use_occurred (occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 10) File Access Log (파일 접근 감사, Phase 2용)
-- ============================================================================
CREATE TABLE IF NOT EXISTS file_access_log (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_id                  VARCHAR(50) NOT NULL,              -- file_metadata.id
  accessor_user_id         BIGINT NOT NULL,                   -- No FK to user_contexts
  access_type              ENUM('READ','DOWNLOAD','DELETE') NOT NULL,
  tenant_id                VARCHAR(50) NOT NULL,
  organization_id          BIGINT NULL,
  accessed_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  request_ip               VARCHAR(45) NULL,
  user_agent               VARCHAR(500) NULL,
  INDEX idx_fal_file (file_id),
  INDEX idx_fal_user (accessor_user_id),
  INDEX idx_fal_accessed (accessed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 11) Upload Policy Settings (EAV 통합)
-- ============================================================================
-- Phase 1의 setting_schemas에 추가할 항목들
INSERT INTO setting_schemas (key_name, value_type, is_secret) VALUES
  ('upload.max_file_size_bytes', 'INT', 0),
  ('upload.allowed_mimes', 'STRING', 0),
  ('upload.enable_virus_scan', 'BOOL', 0),
  ('upload.multipart_threshold_mb', 'INT', 0),                -- 멀티파트 전환 임계값
  ('upload.session_ttl_minutes', 'INT', 0),                   -- 세션 TTL (분)
  ('upload.enable_external_download', 'BOOL', 0),             -- 외부 다운로드 활성화
  ('upload.external_download_timeout_seconds', 'INT', 0)      -- 외부 다운로드 타임아웃
ON DUPLICATE KEY UPDATE value_type=VALUES(value_type);
