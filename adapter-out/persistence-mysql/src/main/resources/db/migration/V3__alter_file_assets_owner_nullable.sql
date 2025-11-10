-- V3: FileAsset ownerUserId nullable 변경
-- 목적: 익명 업로드 및 외부 다운로드 지원 (S3 직접 업로드는 사용자 없이 발생 가능)
-- H2 호환성: ALTER COLUMN ... SET NULL 사용

ALTER TABLE file_assets
ALTER COLUMN owner_user_id SET NULL;

