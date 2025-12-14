-- V7에서 잘못 정의된 tenant_id, organization_id 컬럼 타입 수정
-- BIGINT → VARCHAR(36) (UUIDv7 String 저장용)

-- external_download 테이블 수정
ALTER TABLE external_download
    MODIFY COLUMN tenant_id VARCHAR(36) NOT NULL COMMENT '테넌트 ID (UUIDv7)',
    MODIFY COLUMN organization_id VARCHAR(36) NOT NULL COMMENT '조직 ID (UUIDv7)';
