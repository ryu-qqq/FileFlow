-- ========================================
-- V6: Create upload_session table
-- ========================================
-- 파일 업로드 세션 정보를 저장하는 테이블
-- Presigned URL 발급부터 업로드 완료까지의
-- 전체 세션 생명주기를 추적합니다
-- ========================================

CREATE TABLE upload_session (
    id                   BIGINT          NOT NULL AUTO_INCREMENT COMMENT '업로드 세션 ID',
    session_id           VARCHAR(36)     NOT NULL COMMENT '세션 고유 식별자 (UUID)',
    tenant_id            VARCHAR(50)     NOT NULL COMMENT '테넌트 ID',
    policy_key           VARCHAR(200)    NOT NULL COMMENT '적용된 정책 키',
    file_name            VARCHAR(255)    NOT NULL COMMENT '원본 파일 이름',
    content_type         VARCHAR(100)    NOT NULL COMMENT 'MIME 타입',
    file_size            BIGINT          NOT NULL COMMENT '파일 크기 (bytes)',
    status               VARCHAR(20)     NOT NULL COMMENT '업로드 상태 (INITIATED, IN_PROGRESS, COMPLETED, FAILED, EXPIRED)',
    presigned_url        TEXT            NULL COMMENT 'S3 Presigned URL',
    s3_key               VARCHAR(500)    NULL COMMENT 'S3 객체 키',
    upload_started_at    DATETIME        NULL COMMENT '업로드 시작 시각',
    upload_completed_at  DATETIME        NULL COMMENT '업로드 완료 시각',
    expires_at           DATETIME        NOT NULL COMMENT '세션 만료 시각',
    created_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_id (session_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_upload_session_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id),
    CONSTRAINT fk_upload_session_policy FOREIGN KEY (policy_key) REFERENCES upload_policy(policy_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='업로드 세션';
