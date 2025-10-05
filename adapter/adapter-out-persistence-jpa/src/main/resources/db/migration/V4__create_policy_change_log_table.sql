-- ========================================
-- V4: Create policy_change_log table
-- ========================================
-- 정책 변경 이력을 추적하는 감사(Audit) 테이블
-- 모든 정책 변경 사항을 기록하여 변경 추적 및 복구 가능
-- ========================================

CREATE TABLE policy_change_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '로그 ID',
    policy_key      VARCHAR(200)    NOT NULL COMMENT '정책 키',
    change_type     VARCHAR(50)     NOT NULL COMMENT '변경 유형 (CREATE, UPDATE, DELETE, ACTIVATE, DEACTIVATE)',
    old_version     INT             NULL COMMENT '이전 버전',
    new_version     INT             NULL COMMENT '새 버전',
    old_value       JSON            NULL COMMENT '이전 값 (JSON)',
    new_value       JSON            NULL COMMENT '새 값 (JSON)',
    changed_by      VARCHAR(100)    NULL COMMENT '변경자',
    changed_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '변경 시각',
    PRIMARY KEY (id),
    INDEX idx_policy_change_log_policy_key (policy_key),
    INDEX idx_policy_change_log_changed_at (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='정책 변경 로그 (감사용)';
