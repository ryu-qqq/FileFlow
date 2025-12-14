-- file_asset 테이블에 이미지 크기 컬럼 추가

ALTER TABLE file_asset
    ADD COLUMN image_width INT NULL COMMENT '이미지 너비 (픽셀)' AFTER etag,
    ADD COLUMN image_height INT NULL COMMENT '이미지 높이 (픽셀)' AFTER image_width;
