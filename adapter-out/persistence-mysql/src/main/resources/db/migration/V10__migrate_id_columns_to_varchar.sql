-- 모든 테이블의 user_id, organization_id, tenant_id를 VARCHAR(36)으로 마이그레이션
-- UUIDv7 String 저장을 위한 타입 변경

-- 1. file_asset 테이블
ALTER TABLE file_asset
    MODIFY COLUMN user_id VARCHAR(36) NULL COMMENT '사용자 ID (UUIDv7)',
    MODIFY COLUMN organization_id VARCHAR(36) NOT NULL COMMENT '조직 ID (UUIDv7)',
    MODIFY COLUMN tenant_id VARCHAR(36) NOT NULL COMMENT '테넌트 ID (UUIDv7)';

-- 2. single_upload_session 테이블
ALTER TABLE single_upload_session
    MODIFY COLUMN user_id VARCHAR(36) NULL COMMENT '사용자 ID (UUIDv7)',
    MODIFY COLUMN organization_id VARCHAR(36) NOT NULL COMMENT '조직 ID (UUIDv7)',
    MODIFY COLUMN tenant_id VARCHAR(36) NOT NULL COMMENT '테넌트 ID (UUIDv7)';

-- 3. multipart_upload_session 테이블
ALTER TABLE multipart_upload_session
    MODIFY COLUMN user_id VARCHAR(36) NULL COMMENT '사용자 ID (UUIDv7)',
    MODIFY COLUMN organization_id VARCHAR(36) NOT NULL COMMENT '조직 ID (UUIDv7)',
    MODIFY COLUMN tenant_id VARCHAR(36) NOT NULL COMMENT '테넌트 ID (UUIDv7)';
