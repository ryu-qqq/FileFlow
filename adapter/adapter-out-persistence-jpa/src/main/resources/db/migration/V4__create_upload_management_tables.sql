-- =====================================================
-- FileFlow Database Schema Migration
-- Version: V4 - Upload Management Tables
-- Description: 업로드 세션 및 정책 관리 테이블 생성
-- Author: FileFlow Team
-- Date: 2025-01-20
-- =====================================================

-- =====================================================
-- Table: upload_policies (업로드 정책)
-- Description: 테넌트 및 조직별 업로드 정책
-- =====================================================
CREATE TABLE IF NOT EXISTS upload_policies (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '정책 ID',
    tenant_id               VARCHAR(50)     NOT NULL COMMENT '테넌트 ID',
    organization_id         BIGINT          NULL COMMENT '조직 ID (NULL=테넌트 기본)',
    policy_code             VARCHAR(50)     NOT NULL COMMENT '정책 코드',
    policy_name             VARCHAR(100)    NOT NULL COMMENT '정책명',
    description             TEXT            NULL COMMENT '정책 설명',
    policy_type             ENUM('DEFAULT', 'CUSTOM', 'OVERRIDE') NOT NULL DEFAULT 'CUSTOM' COMMENT '정책 타입',
    priority                INT             NOT NULL DEFAULT 100 COMMENT '우선순위 (낮을수록 높음)',
    allowed_file_types      JSON            NOT NULL DEFAULT ('[]') COMMENT '허용 파일 타입',
    blocked_file_types      JSON            NULL DEFAULT ('[]') COMMENT '차단 파일 타입',
    allowed_mime_types      JSON            NULL DEFAULT ('[]') COMMENT '허용 MIME 타입',
    max_file_size           BIGINT          NOT NULL DEFAULT 104857600 COMMENT '최대 파일 크기 (bytes, 기본 100MB)',
    min_file_size           BIGINT          NULL DEFAULT 1 COMMENT '최소 파일 크기 (bytes)',
    max_total_size          BIGINT          NULL DEFAULT 1073741824 COMMENT '세션당 최대 총 크기 (1GB)',
    max_files_per_session   INT             NULL DEFAULT 100 COMMENT '세션당 최대 파일 수',
    allowed_sources         JSON            NULL DEFAULT ('["PRESIGNED","EXTERNAL_URL"]') COMMENT '허용 업로드 소스',
    allowed_ip_ranges       JSON            NULL COMMENT '허용 IP 범위',
    require_virus_scan      BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '바이러스 스캔 필수 여부',
    require_checksum        BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '체크섬 검증 필수 여부',
    auto_process            BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '자동 처리 활성화',
    processing_pipeline     VARCHAR(100)    NULL COMMENT '기본 처리 파이프라인',
    processing_priority     ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL' COMMENT '처리 우선순위',
    storage_class           ENUM('STANDARD', 'INFREQUENT_ACCESS', 'ARCHIVE', 'GLACIER') NOT NULL DEFAULT 'STANDARD' COMMENT '스토리지 클래스',
    retention_days          INT             NULL DEFAULT 365 COMMENT '보관 기간 (일)',
    auto_delete             BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '자동 삭제 활성화',
    metadata_rules          JSON            NULL DEFAULT ('{}') COMMENT '메타데이터 규칙',
    naming_convention       VARCHAR(255)    NULL COMMENT '파일명 규칙 (정규식)',
    duplicate_handling      ENUM('ALLOW', 'REJECT', 'RENAME', 'REPLACE') NOT NULL DEFAULT 'RENAME' COMMENT '중복 파일 처리',
    rate_limits             JSON            NULL DEFAULT ('{}') COMMENT 'Rate limiting 설정',
    notification_config     JSON            NULL DEFAULT ('{}') COMMENT '알림 설정',
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 상태',
    version                 INT             NOT NULL DEFAULT 1 COMMENT '버전',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    created_by              VARCHAR(100)    NULL COMMENT '생성자',
    PRIMARY KEY (id),
    UNIQUE KEY uk_policy_code (policy_code),
    UNIQUE KEY uk_tenant_org_code (tenant_id, organization_id, policy_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_organization_id (organization_id),
    INDEX idx_is_active (is_active),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='업로드 정책';

-- =====================================================
-- Table: upload_sessions (업로드 세션)
-- Description: 파일 업로드 프로세스 추적
-- =====================================================
CREATE TABLE IF NOT EXISTS upload_sessions (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '세션 ID',
    session_id              VARCHAR(36)     NOT NULL COMMENT '세션 고유 식별자 (UUID)',
    tenant_id               VARCHAR(50)     NOT NULL COMMENT '테넌트 ID',
    organization_id         BIGINT          NOT NULL COMMENT '조직 ID',
    user_context_id         BIGINT          NOT NULL COMMENT '사용자 컨텍스트 ID',
    policy_id               BIGINT          NOT NULL COMMENT '업로드 정책 ID',
    upload_type             ENUM('DIRECT_PRESIGNED', 'DIRECT_API', 'EXTERNAL_URL', 'BATCH') NOT NULL COMMENT '업로드 타입',
    upload_method           ENUM('SINGLE', 'MULTIPART', 'CHUNKED', 'STREAMING') NOT NULL DEFAULT 'SINGLE' COMMENT '업로드 방식',
    status                  ENUM('INITIALIZED', 'UPLOADING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'INITIALIZED' COMMENT '세션 상태',
    external_url            VARCHAR(2048)   NULL COMMENT '외부 URL (EXTERNAL_URL 타입)',
    external_headers        JSON            NULL COMMENT '외부 URL 요청 헤더',
    source_info             JSON            NULL DEFAULT ('{}') COMMENT '소스 정보 (IP, User-Agent 등)',
    session_config          JSON            NULL DEFAULT ('{}') COMMENT '세션 설정',
    presigned_url           TEXT            NULL COMMENT 'Presigned URL',
    presigned_url_expires_at DATETIME        NULL COMMENT 'Presigned URL 만료 시각',
    total_files             INT             NOT NULL DEFAULT 0 COMMENT '전체 파일 수',
    uploaded_files          INT             NOT NULL DEFAULT 0 COMMENT '업로드된 파일 수',
    total_size              BIGINT          NOT NULL DEFAULT 0 COMMENT '전체 크기 (bytes)',
    uploaded_size           BIGINT          NOT NULL DEFAULT 0 COMMENT '업로드된 크기 (bytes)',
    multipart_upload_id     VARCHAR(255)    NULL COMMENT 'S3 멀티파트 업로드 ID',
    total_parts             INT             NULL COMMENT '전체 파트 수 (멀티파트)',
    uploaded_parts          INT             NULL DEFAULT 0 COMMENT '업로드된 파트 수',
    checksum_algorithm      ENUM('MD5', 'SHA256', 'SHA1', 'CRC32') NULL DEFAULT 'SHA256' COMMENT '체크섬 알고리즘',
    expected_checksum       VARCHAR(64)     NULL COMMENT '예상 체크섬',
    idempotency_key         VARCHAR(255)    NULL COMMENT '멱등성 키',
    retry_count             INT             NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    error_message           TEXT            NULL COMMENT '에러 메시지',
    error_code              VARCHAR(50)     NULL COMMENT '에러 코드',
    callback_url            VARCHAR(500)    NULL COMMENT '완료 콜백 URL',
    callback_status         ENUM('PENDING', 'SUCCESS', 'FAILED', 'NONE') NULL DEFAULT 'NONE' COMMENT '콜백 상태',
    metadata                JSON            NULL DEFAULT ('{}') COMMENT '추가 메타데이터',
    version                 INT             NOT NULL DEFAULT 1 COMMENT '버전 (낙관적 락)',
    started_at              DATETIME        NULL COMMENT '업로드 시작 시각',
    completed_at            DATETIME        NULL COMMENT '완료 시각',
    expires_at              DATETIME        NOT NULL COMMENT '세션 만료 시각',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_id (session_id),
    UNIQUE KEY uk_idempotency_key (idempotency_key),
    INDEX idx_tenant_org (tenant_id, organization_id, created_at DESC),
    INDEX idx_user_context_id (user_context_id, status, created_at DESC),
    INDEX idx_policy_id (policy_id),
    INDEX idx_status (status, created_at DESC),
    INDEX idx_upload_type (upload_type, status),
    INDEX idx_expires_at (expires_at),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='업로드 세션';

-- =====================================================
-- Table: upload_parts (멀티파트 업로드 파트)
-- Description: 대용량 파일의 멀티파트 업로드 관리
-- =====================================================
CREATE TABLE IF NOT EXISTS upload_parts (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '파트 ID',
    session_id              VARCHAR(36)     NOT NULL COMMENT '세션 ID',
    part_number             INT             NOT NULL COMMENT '파트 번호',
    etag                    VARCHAR(255)    NULL COMMENT 'ETag (S3)',
    size                    BIGINT          NOT NULL COMMENT '파트 크기 (bytes)',
    checksum                VARCHAR(64)     NULL COMMENT '파트 체크섬',
    status                  ENUM('PENDING', 'UPLOADING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '파트 상태',
    retry_count             INT             NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    error_message           TEXT            NULL COMMENT '에러 메시지',
    started_at              DATETIME        NULL COMMENT '업로드 시작 시각',
    completed_at            DATETIME        NULL COMMENT '완료 시각',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_part (session_id, part_number),
    INDEX idx_session_id (session_id, status),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='멀티파트 업로드 파트';

-- =====================================================
-- Table: upload_chunks (청크 업로드)
-- Description: 청크 단위 업로드 지원
-- =====================================================
CREATE TABLE IF NOT EXISTS upload_chunks (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '청크 ID',
    session_id              VARCHAR(36)     NOT NULL COMMENT '세션 ID',
    chunk_index             INT             NOT NULL COMMENT '청크 인덱스',
    chunk_size              BIGINT          NOT NULL COMMENT '청크 크기 (bytes)',
    offset                  BIGINT          NOT NULL COMMENT '파일 내 오프셋',
    checksum                VARCHAR(64)     NULL COMMENT '청크 체크섬',
    storage_path            VARCHAR(1024)   NULL COMMENT '임시 저장 경로',
    status                  ENUM('PENDING', 'UPLOADED', 'VERIFIED', 'MERGED') NOT NULL DEFAULT 'PENDING' COMMENT '청크 상태',
    uploaded_at             DATETIME        NULL COMMENT '업로드 시각',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_chunk (session_id, chunk_index),
    INDEX idx_session_id (session_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='청크 업로드';

-- =====================================================
-- Table: external_downloads (외부 다운로드 작업)
-- Description: 외부 URL로부터 파일 다운로드 관리
-- =====================================================
CREATE TABLE IF NOT EXISTS external_downloads (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '다운로드 ID',
    session_id              VARCHAR(36)     NOT NULL COMMENT '세션 ID',
    external_url            VARCHAR(2048)   NOT NULL COMMENT '외부 URL',
    http_method             VARCHAR(10)     NOT NULL DEFAULT 'GET' COMMENT 'HTTP 메서드',
    request_headers         JSON            NULL COMMENT '요청 헤더',
    auth_type               ENUM('NONE', 'BASIC', 'BEARER', 'API_KEY', 'OAUTH2') NOT NULL DEFAULT 'NONE' COMMENT '인증 타입',
    auth_credentials        TEXT            NULL COMMENT '인증 정보 (암호화)',
    status                  ENUM('PENDING', 'DOWNLOADING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '상태',
    response_code           INT             NULL COMMENT 'HTTP 응답 코드',
    response_headers        JSON            NULL COMMENT '응답 헤더',
    content_type            VARCHAR(100)    NULL COMMENT 'Content-Type',
    content_length          BIGINT          NULL COMMENT 'Content-Length',
    downloaded_size         BIGINT          NOT NULL DEFAULT 0 COMMENT '다운로드된 크기',
    download_speed          BIGINT          NULL COMMENT '다운로드 속도 (bytes/sec)',
    retry_count             INT             NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    max_retries             INT             NOT NULL DEFAULT 3 COMMENT '최대 재시도 횟수',
    error_message           TEXT            NULL COMMENT '에러 메시지',
    started_at              DATETIME        NULL COMMENT '시작 시각',
    completed_at            DATETIME        NULL COMMENT '완료 시각',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    INDEX idx_session_id (session_id),
    INDEX idx_status (status, created_at DESC),
    INDEX idx_external_url (external_url(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='외부 다운로드 작업';

-- =====================================================
-- Table: batch_uploads (배치 업로드)
-- Description: 여러 파일을 한번에 업로드하는 배치 작업
-- =====================================================
CREATE TABLE IF NOT EXISTS batch_uploads (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '배치 ID',
    batch_id                VARCHAR(36)     NOT NULL COMMENT '배치 고유 식별자',
    tenant_id               VARCHAR(50)     NOT NULL COMMENT '테넌트 ID',
    organization_id         BIGINT          NOT NULL COMMENT '조직 ID',
    user_context_id         BIGINT          NOT NULL COMMENT '사용자 컨텍스트 ID',
    batch_name              VARCHAR(200)    NULL COMMENT '배치명',
    source_type             ENUM('ZIP', 'FOLDER', 'CSV_LIST', 'API') NOT NULL COMMENT '소스 타입',
    total_files             INT             NOT NULL DEFAULT 0 COMMENT '전체 파일 수',
    processed_files         INT             NOT NULL DEFAULT 0 COMMENT '처리된 파일 수',
    successful_files        INT             NOT NULL DEFAULT 0 COMMENT '성공한 파일 수',
    failed_files            INT             NOT NULL DEFAULT 0 COMMENT '실패한 파일 수',
    status                  ENUM('PREPARING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PREPARING' COMMENT '상태',
    manifest                JSON            NULL COMMENT '배치 매니페스트',
    results                 JSON            NULL COMMENT '처리 결과',
    started_at              DATETIME        NULL COMMENT '시작 시각',
    completed_at            DATETIME        NULL COMMENT '완료 시각',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_batch_id (batch_id),
    INDEX idx_tenant_org (tenant_id, organization_id, created_at DESC),
    INDEX idx_user_context_id (user_context_id, created_at DESC),
    INDEX idx_status (status, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='배치 업로드';

-- =====================================================
-- Sample Data
-- =====================================================

-- Insert sample upload policies
INSERT INTO upload_policies (tenant_id, policy_code, policy_name, allowed_file_types, max_file_size, processing_pipeline) VALUES
('b2c_kr', 'B2C_IMAGE_STANDARD', '상품 이미지 표준', '["jpg","jpeg","png","webp","gif"]', 52428800, 'image_optimization'),
('b2c_kr', 'B2C_HTML_STANDARD', '상품 상세 HTML', '["html","htm"]', 10485760, 'html_processing'),
('b2b_global', 'B2B_EXCEL_STANDARD', 'Excel 문서 표준', '["xlsx","xls","csv"]', 104857600, 'excel_ai_mapping'),
('b2b_global', 'B2B_PDF_STANDARD', 'PDF 문서 표준', '["pdf"]', 209715200, 'pdf_processing');
