-- Optimistic Locking 지원을 위한 version 컬럼 추가
-- 동일 객체를 한 플로우에서 2번 이상 persist하는 DownloadTask, TransformRequest에만 적용

ALTER TABLE download_task
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE transform_request
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
