-- ExternalDownload 테이블에 S3 관련 컬럼 추가
-- S3 업로드를 위한 버킷명과 경로 prefix 정보 저장

ALTER TABLE external_download
    ADD COLUMN s3_bucket VARCHAR(63) NOT NULL DEFAULT 'setof' COMMENT 'S3 버킷명 (connectly 또는 setof)' AFTER organization_id,
    ADD COLUMN s3_path_prefix VARCHAR(255) NOT NULL DEFAULT 'customer/' COMMENT 'S3 경로 prefix (admin/, seller-{id}/, customer/)' AFTER s3_bucket;

-- 기존 데이터에 대한 DEFAULT 값 제거 (신규 데이터는 NOT NULL 필수)
-- MySQL 8.0+에서는 ALTER COLUMN으로 DEFAULT 제거 가능
-- 하지만 안전하게 DEFAULT는 유지하고 애플리케이션에서 반드시 값을 넣도록 처리
