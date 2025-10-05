-- ========================================
-- V3: Create processing_policy table
-- ========================================
-- 파일 처리 정책을 저장하는 테이블 (향후 확장용)
-- PolicyKey 형식: {tenantId}:{userType}:{serviceType}
-- ProcessingConfig는 TEXT로 JSON 저장
-- ========================================

CREATE TABLE processing_policy (
    policy_key          VARCHAR(200)    NOT NULL COMMENT '정책 키 (tenantId:userType:serviceType)',
    processing_config   JSON            NULL COMMENT '처리 설정 (JSON)',
    is_active           BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '활성 상태',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (policy_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='처리 정책 (향후 확장용)';
