-- Download Domain Tables
-- Download Task + Callback Outbox

CREATE TABLE download_task (
    id           VARCHAR(36)  NOT NULL,
    source_url   TEXT         NOT NULL,
    bucket       VARCHAR(100) NOT NULL,
    s3_key       VARCHAR(512) NOT NULL,
    access_type  VARCHAR(20)  NOT NULL,
    purpose      VARCHAR(100) NOT NULL,
    source       VARCHAR(100) NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    retry_count  INT          NOT NULL DEFAULT 0,
    max_retries  INT          NOT NULL DEFAULT 3,
    callback_url TEXT         NULL,
    last_error   TEXT         NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    started_at   DATETIME(6)  NULL,
    completed_at DATETIME(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_download_task_status (status),
    INDEX idx_download_task_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE callback_outbox (
    id               VARCHAR(36)  NOT NULL,
    download_task_id VARCHAR(36)  NOT NULL,
    callback_url     TEXT         NOT NULL,
    task_status      VARCHAR(20)  NOT NULL,
    outbox_status    VARCHAR(20)  NOT NULL,
    retry_count      INT          NOT NULL DEFAULT 0,
    last_error       TEXT         NULL,
    created_at       DATETIME(6)  NOT NULL,
    processed_at     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_callback_outbox_download_task_id (download_task_id),
    INDEX idx_callback_outbox_outbox_status (outbox_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
