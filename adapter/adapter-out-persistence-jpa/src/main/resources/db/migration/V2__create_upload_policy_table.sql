-- ========================================
-- V2: Create upload_policy table
-- ========================================
-- 파일 업로드 정책을 저장하는 테이블
-- PolicyKey 형식: {tenantId}:{userType}:{serviceType}
-- FileTypePolicies와 RateLimiting은 TEXT로 JSON 저장
-- ========================================

CREATE TABLE upload_policy (
    policy_key          VARCHAR(200)    NOT NULL COMMENT '정책 키 (tenantId:userType:serviceType)',
    file_type_policies  JSON            NOT NULL COMMENT '파일 타입별 정책 (JSON)',
    rate_limiting       JSON            NOT NULL COMMENT 'Rate Limiting 정책 (JSON)',
    version             INT             NOT NULL DEFAULT 1 COMMENT '정책 버전 (낙관적 잠금)',
    is_active           BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '활성 상태',
    effective_from      DATETIME        NOT NULL COMMENT '유효 시작 일시',
    effective_until     DATETIME        NOT NULL COMMENT '유효 종료 일시',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (policy_key),
    INDEX idx_upload_policy_is_active (is_active),
    INDEX idx_upload_policy_effective_period (effective_from, effective_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='업로드 정책';
