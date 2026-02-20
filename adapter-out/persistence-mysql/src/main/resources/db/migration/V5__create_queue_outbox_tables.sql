CREATE TABLE download_queue_outbox (
    id               VARCHAR(36)  NOT NULL,
    download_task_id VARCHAR(36)  NOT NULL,
    outbox_status    VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count      INT          NOT NULL DEFAULT 0,
    last_error       TEXT         NULL,
    created_at       DATETIME(6)  NOT NULL,
    processed_at     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_download_queue_outbox_status (outbox_status),
    INDEX idx_download_queue_outbox_status_created (outbox_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transform_queue_outbox (
    id                   VARCHAR(36)  NOT NULL,
    transform_request_id VARCHAR(36)  NOT NULL,
    outbox_status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count          INT          NOT NULL DEFAULT 0,
    last_error           TEXT         NULL,
    created_at           DATETIME(6)  NOT NULL,
    processed_at         DATETIME(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_transform_queue_outbox_status (outbox_status),
    INDEX idx_transform_queue_outbox_status_created (outbox_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
