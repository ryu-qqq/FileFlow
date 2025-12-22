-- V15: file_asset 테이블의 session_id nullable 허용
-- External Download로 생성된 FileAsset은 Upload Session 없이 생성되므로 session_id가 null일 수 있음

ALTER TABLE file_asset MODIFY COLUMN session_id VARCHAR(36) NULL;
