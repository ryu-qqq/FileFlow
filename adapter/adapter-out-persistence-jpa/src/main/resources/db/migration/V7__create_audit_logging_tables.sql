-- =====================================================
-- FileFlow Database Schema Migration
-- Version: V7 - Audit & Logging Tables
-- Description: 감사, 로깅, 모니터링 관련 테이블 생성
-- Author: FileFlow Team
-- Date: 2025-01-20
-- Note: 하이브리드 로깅 전략 (MySQL + S3 + CloudWatch)
-- =====================================================

-- =====================================================
-- Table: audit_logs (감사 로그)
-- Description: 시스템 전반의 중요한 작업과 변경사항 추적
-- Note: 30일 이후 S3로 아카이빙, 7년 보관
-- =====================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '로그 ID',
    audit_id                VARCHAR(36)     NOT NULL COMMENT '감사 고유 식별자',
    tenant_id               VARCHAR(50)     NULL COMMENT '테넌트 ID',
    organization_id         BIGINT          NULL COMMENT '조직 ID',
    user_id                 BIGINT          NULL COMMENT '사용자 ID',
    user_type               VARCHAR(50)     NULL COMMENT '사용자 타입',
    username                VARCHAR(100)    NOT NULL COMMENT '사용자명 (변경 방지용)',
    action_type             ENUM('CREATE', 'READ', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'UPLOAD', 'DOWNLOAD', 'APPROVE', 'REJECT', 'SYSTEM') NOT NULL COMMENT '작업 타입',
    resource_type           VARCHAR(50)     NOT NULL COMMENT '리소스 타입 (file, user, policy 등)',
    resource_id             VARCHAR(100)    NULL COMMENT '리소스 ID',
    resource_name           VARCHAR(255)    NULL COMMENT '리소스 명칭',
    action_detail           VARCHAR(500)    NOT NULL COMMENT '작업 상세 설명',
    old_value               JSON            NULL COMMENT '변경 전 값',
    new_value               JSON            NULL COMMENT '변경 후 값',
    change_summary          JSON            NULL COMMENT '변경 요약',
    request_method          VARCHAR(10)     NULL COMMENT 'HTTP 메서드',
    request_uri             VARCHAR(500)    NULL COMMENT '요청 URI',
    request_params          JSON            NULL COMMENT '요청 파라미터',
    ip_address              VARCHAR(45)     NOT NULL COMMENT 'IP 주소',
    user_agent              TEXT            NULL COMMENT 'User Agent',
    session_id              VARCHAR(100)    NULL COMMENT '세션 ID',
    correlation_id          VARCHAR(36)     NULL COMMENT '상관관계 ID (분산 추적)',
    response_code           INT             NULL COMMENT '응답 코드',
    response_time_ms        INT             NULL COMMENT '응답 시간 (밀리초)',
    error_message           TEXT            NULL COMMENT '에러 메시지',
    risk_level              ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'LOW' COMMENT '위험 수준',
    compliance_tags         JSON            NULL DEFAULT ('[]') COMMENT '규정 준수 태그',
    metadata                JSON            NULL DEFAULT ('{}') COMMENT '추가 메타데이터',
    is_sensitive            BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '민감 정보 포함 여부',
    retention_days          INT             NOT NULL DEFAULT 2555 COMMENT '보관 기간 (일, 기본 7년)',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_audit_id (audit_id),
    INDEX idx_tenant_org (tenant_id, organization_id, created_at DESC),
    INDEX idx_user_id (user_id, created_at DESC),
    INDEX idx_action_type (action_type, created_at DESC),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_ip_address (ip_address, created_at DESC),
    INDEX idx_risk_level (risk_level, created_at DESC),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='감사 로그'
PARTITION BY RANGE (TO_DAYS(created_at)) (
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
    PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- =====================================================
-- Table: access_logs (접근 로그)
-- Description: 파일 및 리소스에 대한 모든 접근 기록
-- Note: 7일 이후 S3로 아카이빙, 90일 보관
-- =====================================================
CREATE TABLE IF NOT EXISTS access_logs (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '로그 ID',
    tenant_id               VARCHAR(50)     NULL COMMENT '테넌트 ID',
    file_id                 BIGINT          NULL COMMENT '파일 ID',
    user_id                 BIGINT          NULL COMMENT '사용자 ID',
    access_type             ENUM('VIEW', 'DOWNLOAD', 'PREVIEW', 'SHARE', 'EMBED') NOT NULL COMMENT '접근 타입',
    access_method           ENUM('WEB', 'API', 'DIRECT', 'CDN') NOT NULL COMMENT '접근 방법',
    request_uri             VARCHAR(2048)   NOT NULL COMMENT '요청 URI',
    query_params            TEXT            NULL COMMENT '쿼리 파라미터',
    referer                 TEXT            NULL COMMENT 'Referer',
    ip_address              VARCHAR(45)     NOT NULL COMMENT 'IP 주소',
    country_code            VARCHAR(2)      NULL COMMENT '국가 코드',
    user_agent              TEXT            NULL COMMENT 'User Agent',
    device_type             ENUM('DESKTOP', 'MOBILE', 'TABLET', 'BOT', 'UNKNOWN') NULL DEFAULT 'UNKNOWN' COMMENT '디바이스 타입',
    browser                 VARCHAR(50)     NULL COMMENT '브라우저',
    os                      VARCHAR(50)     NULL COMMENT '운영체제',
    response_code           INT             NOT NULL COMMENT 'HTTP 응답 코드',
    response_size           BIGINT          NULL COMMENT '응답 크기 (bytes)',
    response_time_ms        INT             NULL COMMENT '응답 시간 (밀리초)',
    cache_status            ENUM('HIT', 'MISS', 'BYPASS', 'EXPIRED') NULL COMMENT '캐시 상태',
    cdn_pop                 VARCHAR(50)     NULL COMMENT 'CDN PoP 위치',
    ssl_protocol            VARCHAR(20)     NULL COMMENT 'SSL 프로토콜',
    error_message           TEXT            NULL COMMENT '에러 메시지',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '접근 시각',
    PRIMARY KEY (id),
    INDEX idx_tenant_id (tenant_id, created_at DESC),
    INDEX idx_file_id (file_id, created_at DESC),
    INDEX idx_user_id (user_id, created_at DESC),
    INDEX idx_access_type (access_type, created_at DESC),
    INDEX idx_ip_address (ip_address, created_at DESC),
    INDEX idx_response_code (response_code),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='접근 로그'
PARTITION BY RANGE (TO_DAYS(created_at)) (
    PARTITION p20250120 VALUES LESS THAN (TO_DAYS('2025-01-21')),
    PARTITION p20250121 VALUES LESS THAN (TO_DAYS('2025-01-22')),
    PARTITION p20250122 VALUES LESS THAN (TO_DAYS('2025-01-23')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- =====================================================
-- Table: processing_errors (처리 오류 로그)
-- Description: 파이프라인 처리 중 발생한 오류 기록
-- Note: 30일 이후 S3로 아카이빙, 1년 보관
-- =====================================================
CREATE TABLE IF NOT EXISTS processing_errors (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '오류 ID',
    error_id                VARCHAR(36)     NOT NULL COMMENT '오류 고유 식별자',
    execution_id            BIGINT          NULL COMMENT '파이프라인 실행 ID',
    file_id                 BIGINT          NULL COMMENT '파일 ID',
    stage_id                BIGINT          NULL COMMENT '파이프라인 단계 ID',
    error_type              ENUM('VALIDATION', 'PROCESSING', 'TIMEOUT', 'RESOURCE', 'DEPENDENCY', 'SYSTEM', 'UNKNOWN') NOT NULL COMMENT '오류 타입',
    error_code              VARCHAR(50)     NOT NULL COMMENT '오류 코드',
    error_message           TEXT            NOT NULL COMMENT '오류 메시지',
    error_details           JSON            NULL COMMENT '상세 오류 정보',
    stack_trace             TEXT            NULL COMMENT '스택 트레이스',
    context_data            JSON            NULL COMMENT '컨텍스트 데이터',
    severity                ENUM('DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL') NOT NULL DEFAULT 'ERROR' COMMENT '심각도',
    component               VARCHAR(100)    NULL COMMENT '컴포넌트명',
    host_name               VARCHAR(255)    NULL COMMENT '호스트명',
    process_id              VARCHAR(50)     NULL COMMENT '프로세스 ID',
    thread_id               VARCHAR(50)     NULL COMMENT '스레드 ID',
    retry_count             INT             NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    is_resolved             BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '해결 여부',
    resolved_at             DATETIME        NULL COMMENT '해결 시각',
    resolved_by             BIGINT          NULL COMMENT '해결자 ID',
    resolution_notes        TEXT            NULL COMMENT '해결 노트',
    tags                    JSON            NULL DEFAULT ('[]') COMMENT '태그',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발생 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_error_id (error_id),
    INDEX idx_execution_id (execution_id),
    INDEX idx_file_id (file_id),
    INDEX idx_stage_id (stage_id),
    INDEX idx_error_type (error_type, created_at DESC),
    INDEX idx_error_code (error_code),
    INDEX idx_severity (severity, is_resolved),
    INDEX idx_is_resolved (is_resolved, created_at DESC),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='처리 오류 로그';

-- =====================================================
-- Table: security_events (보안 이벤트)
-- Description: 보안 관련 이벤트 추적
-- Note: 90일 이후 S3로 아카이빙, 3년 보관
-- =====================================================
CREATE TABLE IF NOT EXISTS security_events (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '이벤트 ID',
    event_id                VARCHAR(36)     NOT NULL COMMENT '이벤트 고유 식별자',
    event_type              ENUM('LOGIN_FAILED', 'UNAUTHORIZED_ACCESS', 'PERMISSION_DENIED', 'SUSPICIOUS_ACTIVITY', 'DATA_BREACH', 'MALWARE_DETECTED', 'BRUTE_FORCE', 'SQL_INJECTION', 'XSS_ATTEMPT') NOT NULL COMMENT '이벤트 타입',
    severity                ENUM('INFO', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL COMMENT '심각도',
    user_id                 BIGINT          NULL COMMENT '관련 사용자 ID',
    target_resource         VARCHAR(255)    NULL COMMENT '대상 리소스',
    attack_vector           VARCHAR(100)    NULL COMMENT '공격 벡터',
    ip_address              VARCHAR(45)     NOT NULL COMMENT 'IP 주소',
    geo_location            JSON            NULL COMMENT '지리적 위치',
    user_agent              TEXT            NULL COMMENT 'User Agent',
    request_data            JSON            NULL COMMENT '요청 데이터',
    threat_indicators       JSON            NULL COMMENT '위협 지표',
    detection_method        VARCHAR(100)    NULL COMMENT '탐지 방법',
    response_action         ENUM('BLOCKED', 'ALLOWED', 'MONITORED', 'QUARANTINED') NULL COMMENT '대응 조치',
    is_false_positive       BOOLEAN         NULL COMMENT '오탐 여부',
    investigation_status    ENUM('PENDING', 'INVESTIGATING', 'RESOLVED', 'ESCALATED') NOT NULL DEFAULT 'PENDING' COMMENT '조사 상태',
    investigation_notes     TEXT            NULL COMMENT '조사 노트',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발생 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_event_id (event_id),
    INDEX idx_event_type (event_type, created_at DESC),
    INDEX idx_severity (severity, investigation_status),
    INDEX idx_user_id (user_id),
    INDEX idx_ip_address (ip_address, created_at DESC),
    INDEX idx_investigation_status (investigation_status),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='보안 이벤트';

-- =====================================================
-- Table: performance_metrics (성능 메트릭)
-- Description: 시스템 및 애플리케이션 성능 메트릭
-- Note: Prometheus로 실시간 수집, 7일 보관
-- =====================================================
CREATE TABLE IF NOT EXISTS performance_metrics (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '메트릭 ID',
    metric_type             VARCHAR(50)     NOT NULL COMMENT '메트릭 타입',
    metric_name             VARCHAR(100)    NOT NULL COMMENT '메트릭명',
    metric_value            DECIMAL(20,4)   NOT NULL COMMENT '메트릭 값',
    unit                    VARCHAR(20)     NULL COMMENT '단위',
    component               VARCHAR(100)    NOT NULL COMMENT '컴포넌트',
    host_name               VARCHAR(255)    NULL COMMENT '호스트명',
    tags                    JSON            NULL DEFAULT ('{}') COMMENT '태그',
    dimensions              JSON            NULL DEFAULT ('{}') COMMENT '차원 정보',
    timestamp               DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '타임스탬프',
    PRIMARY KEY (id),
    INDEX idx_metric_type (metric_type, timestamp DESC),
    INDEX idx_metric_name (metric_name, timestamp DESC),
    INDEX idx_component (component, timestamp DESC),
    INDEX idx_timestamp (timestamp DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='성능 메트릭'
PARTITION BY RANGE (TO_DAYS(timestamp)) (
    PARTITION p20250120 VALUES LESS THAN (TO_DAYS('2025-01-21')),
    PARTITION p20250121 VALUES LESS THAN (TO_DAYS('2025-01-22')),
    PARTITION p20250122 VALUES LESS THAN (TO_DAYS('2025-01-23')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- =====================================================
-- Table: api_usage_logs (API 사용 로그)
-- Description: API 호출 기록
-- Note: 30일 보관, Rate limiting에 활용
-- =====================================================
CREATE TABLE IF NOT EXISTS api_usage_logs (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '로그 ID',
    tenant_id               VARCHAR(50)     NULL COMMENT '테넌트 ID',
    organization_id         BIGINT          NULL COMMENT '조직 ID',
    api_key                 VARCHAR(100)    NULL COMMENT 'API 키 (해시)',
    endpoint                VARCHAR(255)    NOT NULL COMMENT 'API 엔드포인트',
    method                  VARCHAR(10)     NOT NULL COMMENT 'HTTP 메서드',
    request_id              VARCHAR(36)     NOT NULL COMMENT '요청 ID',
    request_size            BIGINT          NULL COMMENT '요청 크기 (bytes)',
    response_size           BIGINT          NULL COMMENT '응답 크기 (bytes)',
    response_code           INT             NOT NULL COMMENT '응답 코드',
    response_time_ms        INT             NOT NULL COMMENT '응답 시간 (밀리초)',
    rate_limit_remaining    INT             NULL COMMENT '남은 Rate Limit',
    ip_address              VARCHAR(45)     NOT NULL COMMENT 'IP 주소',
    user_agent              TEXT            NULL COMMENT 'User Agent',
    error_message           TEXT            NULL COMMENT '에러 메시지',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '호출 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_request_id (request_id),
    INDEX idx_tenant_org (tenant_id, organization_id, created_at DESC),
    INDEX idx_api_key (api_key, created_at DESC),
    INDEX idx_endpoint (endpoint, method, created_at DESC),
    INDEX idx_response_code (response_code),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='API 사용 로그'
PARTITION BY RANGE (UNIX_TIMESTAMP(created_at)) (
    PARTITION p202501_h00 VALUES LESS THAN (UNIX_TIMESTAMP('2025-01-20 01:00:00')),
    PARTITION p202501_h01 VALUES LESS THAN (UNIX_TIMESTAMP('2025-01-20 02:00:00')),
    PARTITION p202501_h02 VALUES LESS THAN (UNIX_TIMESTAMP('2025-01-20 03:00:00')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- =====================================================
-- Table: compliance_logs (규정 준수 로그)
-- Description: 법적 규정 준수를 위한 특별 로그
-- Note: GDPR, CCPA 등 요구사항 충족
-- =====================================================
CREATE TABLE IF NOT EXISTS compliance_logs (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '로그 ID',
    compliance_type         ENUM('GDPR', 'CCPA', 'HIPAA', 'PCI_DSS', 'SOC2', 'ISO27001', 'CUSTOM') NOT NULL COMMENT '규정 타입',
    event_type              VARCHAR(100)    NOT NULL COMMENT '이벤트 타입',
    data_subject_id         VARCHAR(100)    NULL COMMENT '데이터 주체 ID',
    data_category           VARCHAR(100)    NULL COMMENT '데이터 카테고리',
    action                  VARCHAR(100)    NOT NULL COMMENT '수행 작업',
    lawful_basis            VARCHAR(100)    NULL COMMENT '법적 근거',
    consent_id              VARCHAR(100)    NULL COMMENT '동의 ID',
    purpose                 TEXT            NULL COMMENT '처리 목적',
    data_controller         VARCHAR(200)    NULL COMMENT '데이터 컨트롤러',
    data_processor          VARCHAR(200)    NULL COMMENT '데이터 프로세서',
    retention_period        INT             NULL COMMENT '보관 기간 (일)',
    cross_border_transfer   BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '국경 간 전송 여부',
    recipient_country       VARCHAR(2)      NULL COMMENT '수신국 코드',
    safeguards              JSON            NULL COMMENT '보호 조치',
    metadata                JSON            NULL DEFAULT ('{}') COMMENT '추가 메타데이터',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    INDEX idx_compliance_type (compliance_type, created_at DESC),
    INDEX idx_data_subject (data_subject_id),
    INDEX idx_consent_id (consent_id),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='규정 준수 로그';

-- =====================================================
-- Sample Data
-- =====================================================

-- Insert sample audit logs
INSERT INTO audit_logs (audit_id, tenant_id, user_id, username, action_type, resource_type, resource_id, action_detail, ip_address, risk_level) VALUES
(UUID(), 'b2c_kr', 1, 'seller001', 'UPLOAD', 'file', 'f123-456-789', '상품 이미지 업로드', '192.168.1.100', 'LOW'),
(UUID(), 'b2c_kr', 2, 'admin001', 'UPDATE', 'policy', 'p987-654-321', '업로드 정책 수정', '192.168.1.101', 'MEDIUM'),
(UUID(), 'b2b_global', 3, 'company001', 'DOWNLOAD', 'file', 'f456-789-012', 'Excel 파일 다운로드', '203.0.113.1', 'LOW');
