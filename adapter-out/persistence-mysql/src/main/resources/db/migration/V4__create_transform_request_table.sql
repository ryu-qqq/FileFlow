-- Transform Domain Table
-- Transform Request

CREATE TABLE transform_request (
    id                     VARCHAR(36)  NOT NULL,
    source_asset_id        VARCHAR(36)  NOT NULL,
    source_content_type    VARCHAR(100) NOT NULL,
    type                   VARCHAR(20)  NOT NULL,
    status                 VARCHAR(20)  NOT NULL,
    result_asset_id        VARCHAR(36)  NULL,
    last_error             TEXT         NULL,
    width                  INT          NULL,
    height                 INT          NULL,
    maintain_aspect_ratio  TINYINT(1)   NOT NULL DEFAULT 0,
    target_format          VARCHAR(20)  NULL,
    quality                INT          NULL,
    created_at             DATETIME(6)  NOT NULL,
    updated_at             DATETIME(6)  NOT NULL,
    completed_at           DATETIME(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_transform_request_status (status),
    INDEX idx_transform_request_source_asset_id (source_asset_id),
    INDEX idx_transform_request_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
