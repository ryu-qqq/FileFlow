-- ========================================
-- V1: Create tenant table
-- ========================================
-- 테넌트 기본 정보를 저장하는 테이블
-- 각 테넌트는 고유한 tenantId를 가지며,
-- 이를 기반으로 정책이 분리됩니다
-- ========================================

CREATE TABLE tenant (
    tenant_id   VARCHAR(50)     NOT NULL COMMENT '테넌트 ID (예: b2c, b2b)',
    name        VARCHAR(100)    NOT NULL COMMENT '테넌트 이름',
    created_at  DATETIME        NOT NULL COMMENT '생성 시각',
    updated_at  DATETIME        NULL COMMENT '수정 시각',
    PRIMARY KEY (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='테넌트 정보';
