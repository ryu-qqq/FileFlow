-- V9: download_task 테이블에 asset_id 컬럼 추가
-- 다운로드 완료 시 생성된 Asset의 ID를 저장하여 콜백/조회 시 반환
ALTER TABLE download_task
    ADD COLUMN asset_id VARCHAR(36) DEFAULT NULL AFTER callback_url;

CREATE INDEX idx_download_task_asset_id ON download_task (asset_id);
