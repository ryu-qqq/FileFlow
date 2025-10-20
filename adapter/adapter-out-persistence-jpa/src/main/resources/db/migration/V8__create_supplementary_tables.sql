-- =====================================================
-- FileFlow Database Schema Migration
-- Version: V8 - Supplementary Tables
-- Description: 보조 테이블 및 추가 기능 테이블 생성
-- Author: FileFlow Team
-- Date: 2025-01-20
-- =====================================================

-- =====================================================
-- Table: file_categories (파일 카테고리)
-- Description: 파일 분류 관리 (상품, 리뷰, 문서 등)
-- =====================================================
CREATE TABLE IF NOT EXISTS file_categories (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '카테고리 ID',
    category_code           VARCHAR(50)     NOT NULL COMMENT '카테고리 코드',
    category_name           VARCHAR(100)    NOT NULL COMMENT '카테고리명',
    parent_id               BIGINT          NULL COMMENT '부모 카테고리 ID',
    category_path           VARCHAR(500)    NULL COMMENT '카테고리 경로',
    category_level          INT             NOT NULL DEFAULT 1 COMMENT '카테고리 레벨',
    display_order           INT             NOT NULL DEFAULT 100 COMMENT '표시 순서',
    icon                    VARCHAR(100)    NULL COMMENT '아이콘',
    description             TEXT            NULL COMMENT '설명',
    allowed_file_types      JSON            NULL DEFAULT ('[]') COMMENT '허용 파일 타입',
    metadata_schema         JSON            NULL COMMENT '메타데이터 스키마',
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 상태',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_code (category_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_category_level (category_level),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일 카테고리';

-- =====================================================
-- Table: file_thumbnails_queue (썸네일 생성 대기열)
-- Description: 비동기 썸네일 생성을 위한 작업 큐
-- =====================================================
CREATE TABLE IF NOT EXISTS file_thumbnails_queue (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '큐 ID',
    queue_id                VARCHAR(36)     NOT NULL COMMENT '큐 고유 식별자',
    file_id                 BIGINT          NOT NULL COMMENT '파일 ID',
    priority                ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL' COMMENT '우선순위',
    thumbnail_sizes         JSON            NOT NULL DEFAULT ('[[200,200],[400,400],[800,800]]') COMMENT '생성할 썸네일 크기',
    processing_params       JSON            NULL DEFAULT ('{}') COMMENT '처리 파라미터',
    status                  ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '상태',
    retry_count             INT             NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    max_retries             INT             NOT NULL DEFAULT 3 COMMENT '최대 재시도 횟수',
    worker_id               VARCHAR(100)    NULL COMMENT '처리 워커 ID',
    error_message           TEXT            NULL COMMENT '에러 메시지',
    started_at              DATETIME        NULL COMMENT '처리 시작 시각',
    completed_at            DATETIME        NULL COMMENT '완료 시각',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_queue_id (queue_id),
    UNIQUE KEY uk_file_id_pending (file_id, status),
    INDEX idx_status_priority (status, priority, created_at),
    INDEX idx_worker_id (worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='썸네일 생성 대기열';

-- =====================================================
-- Table: virus_scan_results (바이러스 스캔 결과)
-- Description: 파일 바이러스 스캔 결과 저장
-- =====================================================
CREATE TABLE IF NOT EXISTS virus_scan_results (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '스캔 ID',
    scan_id                 VARCHAR(36)     NOT NULL COMMENT '스캔 고유 식별자',
    file_id                 BIGINT          NOT NULL COMMENT '파일 ID',
    scan_engine             VARCHAR(50)     NOT NULL COMMENT '스캔 엔진',
    scan_engine_version     VARCHAR(20)     NULL COMMENT '엔진 버전',
    scan_result             ENUM('CLEAN', 'INFECTED', 'SUSPICIOUS', 'ERROR', 'TIMEOUT') NOT NULL COMMENT '스캔 결과',
    threat_name             VARCHAR(255)    NULL COMMENT '위협 이름',
    threat_type             VARCHAR(100)    NULL COMMENT '위협 타입',
    threat_severity         ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NULL COMMENT '위협 심각도',
    scan_details            JSON            NULL COMMENT '상세 스캔 정보',
    quarantine_status       ENUM('NOT_QUARANTINED', 'QUARANTINED', 'DELETED', 'RESTORED') NULL DEFAULT 'NOT_QUARANTINED' COMMENT '격리 상태',
    quarantine_path         VARCHAR(500)    NULL COMMENT '격리 경로',
    scan_duration_ms        INT             NULL COMMENT '스캔 소요 시간 (밀리초)',
    scanned_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '스캔 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_scan_id (scan_id),
    INDEX idx_file_id (file_id, scanned_at DESC),
    INDEX idx_scan_result (scan_result, scanned_at DESC),
    INDEX idx_threat_severity (threat_severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='바이러스 스캔 결과';

-- =====================================================
-- Table: file_events (파일 이벤트)
-- Description: Event Sourcing 패턴 - 파일 변경 이벤트 저장
-- =====================================================
CREATE TABLE IF NOT EXISTS file_events (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '이벤트 ID',
    event_id                VARCHAR(36)     NOT NULL COMMENT '이벤트 고유 식별자',
    file_id                 BIGINT          NOT NULL COMMENT '파일 ID',
    event_type              VARCHAR(50)     NOT NULL COMMENT '이벤트 타입',
    event_name              VARCHAR(100)    NOT NULL COMMENT '이벤트명',
    event_data              JSON            NOT NULL COMMENT '이벤트 데이터',
    aggregate_version       INT             NOT NULL COMMENT '집계 버전',
    user_context_id         BIGINT          NULL COMMENT '사용자 ID',
    correlation_id          VARCHAR(36)     NULL COMMENT '상관관계 ID',
    metadata                JSON            NULL DEFAULT ('{}') COMMENT '메타데이터',
    occurred_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발생 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_event_id (event_id),
    INDEX idx_file_id (file_id, occurred_at DESC),
    INDEX idx_event_type (event_type, occurred_at DESC),
    INDEX idx_correlation_id (correlation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일 이벤트';

-- =====================================================
-- Table: schema_migrations_history (스키마 마이그레이션 히스토리)
-- Description: 스키마 변경 이력 추적
-- =====================================================
CREATE TABLE IF NOT EXISTS schema_migrations_history (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '히스토리 ID',
    version                 VARCHAR(20)     NOT NULL COMMENT '버전',
    description             VARCHAR(500)    NOT NULL COMMENT '설명',
    script_name             VARCHAR(255)    NOT NULL COMMENT '스크립트명',
    checksum                VARCHAR(64)     NULL COMMENT '체크섬',
    installed_by            VARCHAR(100)    NOT NULL COMMENT '실행자',
    installed_on            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '실행 시각',
    execution_time_ms       INT             NULL COMMENT '실행 시간 (밀리초)',
    success                 BOOLEAN         NOT NULL COMMENT '성공 여부',
    error_message           TEXT            NULL COMMENT '에러 메시지',
    PRIMARY KEY (id),
    UNIQUE KEY uk_version (version),
    INDEX idx_installed_on (installed_on DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스키마 마이그레이션 히스토리';

-- =====================================================
-- Table: notification_templates (알림 템플릿)
-- Description: 이메일, SMS, 슬랙 등 알림 템플릿
-- =====================================================
CREATE TABLE IF NOT EXISTS notification_templates (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '템플릿 ID',
    template_code           VARCHAR(50)     NOT NULL COMMENT '템플릿 코드',
    template_name           VARCHAR(100)    NOT NULL COMMENT '템플릿명',
    notification_type       ENUM('EMAIL', 'SMS', 'SLACK', 'WEBHOOK', 'PUSH') NOT NULL COMMENT '알림 타입',
    subject                 VARCHAR(500)    NULL COMMENT '제목',
    body_template           TEXT            NOT NULL COMMENT '본문 템플릿',
    variables               JSON            NULL DEFAULT ('[]') COMMENT '변수 목록',
    locale                  VARCHAR(10)     NOT NULL DEFAULT 'ko' COMMENT '언어',
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 상태',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_code_locale (template_code, locale),
    INDEX idx_notification_type (notification_type, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림 템플릿';

-- =====================================================
-- Table: notification_queue (알림 큐)
-- Description: 발송 대기 중인 알림 큐
-- =====================================================
CREATE TABLE IF NOT EXISTS notification_queue (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '큐 ID',
    notification_id         VARCHAR(36)     NOT NULL COMMENT '알림 고유 식별자',
    template_code           VARCHAR(50)     NOT NULL COMMENT '템플릿 코드',
    notification_type       ENUM('EMAIL', 'SMS', 'SLACK', 'WEBHOOK', 'PUSH') NOT NULL COMMENT '알림 타입',
    recipient               VARCHAR(255)    NOT NULL COMMENT '수신자',
    subject                 VARCHAR(500)    NULL COMMENT '제목',
    body                    TEXT            NOT NULL COMMENT '본문',
    priority                ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL' COMMENT '우선순위',
    status                  ENUM('PENDING', 'SENDING', 'SENT', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '상태',
    retry_count             INT             NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    max_retries             INT             NOT NULL DEFAULT 3 COMMENT '최대 재시도 횟수',
    error_message           TEXT            NULL COMMENT '에러 메시지',
    metadata                JSON            NULL DEFAULT ('{}') COMMENT '메타데이터',
    scheduled_at            DATETIME        NULL COMMENT '예약 발송 시각',
    sent_at                 DATETIME        NULL COMMENT '발송 시각',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_id (notification_id),
    INDEX idx_status_priority (status, priority, scheduled_at),
    INDEX idx_scheduled_at (scheduled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림 큐';

-- =====================================================
-- Table: system_configs (시스템 설정)
-- Description: 전역 시스템 설정 관리
-- =====================================================
CREATE TABLE IF NOT EXISTS system_configs (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '설정 ID',
    config_key              VARCHAR(100)    NOT NULL COMMENT '설정 키',
    config_value            TEXT            NULL COMMENT '설정 값',
    config_type             ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'ENCRYPTED') NOT NULL DEFAULT 'STRING' COMMENT '값 타입',
    category                VARCHAR(50)     NOT NULL DEFAULT 'GENERAL' COMMENT '카테고리',
    description             TEXT            NULL COMMENT '설명',
    is_encrypted            BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '암호화 여부',
    is_editable             BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '편집 가능 여부',
    is_visible              BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '표시 여부',
    default_value           TEXT            NULL COMMENT '기본값',
    validation_rules        JSON            NULL COMMENT '검증 규칙',
    cache_ttl_seconds       INT             NULL DEFAULT 300 COMMENT '캐시 TTL (초)',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    updated_by              VARCHAR(100)    NULL COMMENT '수정자',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key),
    INDEX idx_category (category),
    INDEX idx_is_editable (is_editable, is_visible)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='시스템 설정';

-- =====================================================
-- Sample Data
-- =====================================================

-- Insert file categories
INSERT INTO file_categories (category_code, category_name, parent_id, category_level, display_order, allowed_file_types) VALUES
('PRODUCT_IMAGE', '상품 이미지', NULL, 1, 100, '["jpg","jpeg","png","webp"]'),
('PRODUCT_DETAIL', '상품 상세', NULL, 1, 200, '["html","htm"]'),
('ORDER_DOCUMENT', '주문 문서', NULL, 1, 300, '["xlsx","xls","csv","pdf"]'),
('REVIEW_IMAGE', '리뷰 이미지', NULL, 1, 400, '["jpg","jpeg","png"]'),
('COMPANY_DOCUMENT', '회사 문서', NULL, 1, 500, '["pdf","docx","xlsx"]');

-- Insert notification templates
INSERT INTO notification_templates (template_code, template_name, notification_type, subject, body_template, variables, locale) VALUES
('FILE_UPLOAD_SUCCESS', '파일 업로드 성공', 'EMAIL', '파일 업로드가 완료되었습니다', '안녕하세요 {{user_name}}님,\n\n{{file_name}} 파일이 성공적으로 업로드되었습니다.\n\n감사합니다.', '["user_name","file_name"]', 'ko'),
('FILE_UPLOAD_SUCCESS', 'File Upload Success', 'EMAIL', 'File Upload Completed', 'Hello {{user_name}},\n\nYour file {{file_name}} has been successfully uploaded.\n\nThank you.', '["user_name","file_name"]', 'en'),
('PIPELINE_COMPLETE', '파이프라인 처리 완료', 'SLACK', NULL, '파이프라인 처리가 완료되었습니다.\n파일: {{file_name}}\n처리시간: {{duration}}초', '["file_name","duration"]', 'ko');

-- Insert system configs
INSERT INTO system_configs (config_key, config_value, config_type, category, description) VALUES
('MAX_FILE_SIZE', '104857600', 'NUMBER', 'UPLOAD', '최대 파일 크기 (bytes)'),
('ALLOWED_ORIGINS', '["https://example.com","https://app.example.com"]', 'JSON', 'SECURITY', 'CORS 허용 오리진'),
('DEFAULT_THUMBNAIL_SIZES', '[[200,200],[400,400],[800,800]]', 'JSON', 'IMAGE', '기본 썸네일 크기'),
('VIRUS_SCAN_ENABLED', 'true', 'BOOLEAN', 'SECURITY', '바이러스 스캔 활성화'),
('S3_BUCKET_NAME', 'fileflow-prod', 'STRING', 'STORAGE', 'S3 버킷명'),
('CDN_BASE_URL', 'https://cdn.fileflow.example.com', 'STRING', 'CDN', 'CDN 베이스 URL');

-- Record migration history
INSERT INTO schema_migrations_history (version, description, script_name, installed_by, execution_time_ms, success) VALUES
('V1', 'Create tenant & organization tables', 'V1__create_tenant_organization_tables.sql', 'flyway', 150, TRUE),
('V2', 'Create user & permission tables', 'V2__create_user_permission_tables.sql', 'flyway', 120, TRUE),
('V3', 'Create file management tables', 'V3__create_file_management_tables.sql', 'flyway', 180, TRUE),
('V4', 'Create upload management tables', 'V4__create_upload_management_tables.sql', 'flyway', 160, TRUE),
('V5', 'Create pipeline processing tables', 'V5__create_pipeline_processing_tables.sql', 'flyway', 140, TRUE),
('V6', 'Create data extraction tables', 'V6__create_data_extraction_tables.sql', 'flyway', 130, TRUE),
('V7', 'Create audit & logging tables', 'V7__create_audit_logging_tables.sql', 'flyway', 170, TRUE),
('V8', 'Create supplementary tables', 'V8__create_supplementary_tables.sql', 'flyway', 110, TRUE);
