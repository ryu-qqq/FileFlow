-- V3: FileAsset ownerUserId nullable 변경
-- 목적: 익명 업로드 및 외부 다운로드 지원 (S3 직접 업로드는 사용자 없이 발생 가능)

ALTER TABLE file_assets
MODIFY COLUMN owner_user_id BIGINT NULL
COMMENT '파일 소유자 ID (익명 업로드 시 NULL 허용)';

