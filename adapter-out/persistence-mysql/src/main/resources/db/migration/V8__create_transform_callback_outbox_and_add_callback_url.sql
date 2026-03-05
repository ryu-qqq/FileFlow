-- transform_callback_outbox 테이블 생성
CREATE TABLE transform_callback_outbox (
    id                   VARCHAR(36)  NOT NULL,
    transform_request_id VARCHAR(36)  NOT NULL,
    callback_url         TEXT         NOT NULL,
    task_status          VARCHAR(20)  NOT NULL,
    outbox_status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count          INT          NOT NULL DEFAULT 0,
    max_retries          INT          NOT NULL DEFAULT 5,
    last_error           TEXT         NULL,
    created_at           DATETIME(6)  NOT NULL,
    processed_at         DATETIME(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_transform_callback_outbox_status (outbox_status),
    INDEX idx_transform_callback_outbox_status_created (outbox_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- transform_request 테이블에 callback_url 컬럼 추가
ALTER TABLE transform_request
    ADD COLUMN callback_url TEXT NULL AFTER quality;
