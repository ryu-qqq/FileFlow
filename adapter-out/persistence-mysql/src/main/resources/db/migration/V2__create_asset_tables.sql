-- Asset Domain Tables
-- Asset + Asset Metadata

CREATE TABLE asset (
    id           VARCHAR(36)  NOT NULL,
    bucket       VARCHAR(100) NOT NULL,
    s3_key       VARCHAR(512) NOT NULL,
    access_type  VARCHAR(20)  NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    file_size    BIGINT       NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    etag         VARCHAR(255) NOT NULL,
    extension    VARCHAR(20)  NOT NULL,
    origin       VARCHAR(30)  NOT NULL,
    origin_id    VARCHAR(36)  NOT NULL,
    purpose      VARCHAR(100) NOT NULL,
    source       VARCHAR(100) NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    deleted_at   DATETIME(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_asset_origin (origin, origin_id),
    INDEX idx_asset_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE asset_metadata (
    id             VARCHAR(36)  NOT NULL,
    asset_id       VARCHAR(36)  NOT NULL,
    width          INT          NOT NULL,
    height         INT          NOT NULL,
    transform_type VARCHAR(30)  NULL,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_asset_metadata_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
