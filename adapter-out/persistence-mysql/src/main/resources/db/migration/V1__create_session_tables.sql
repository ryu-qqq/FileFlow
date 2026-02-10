-- Session Domain Tables
-- Single Upload Session + Multipart Upload Session + Completed Part

CREATE TABLE single_upload_session (
    id           VARCHAR(36)  NOT NULL,
    s3_key       VARCHAR(512) NOT NULL,
    bucket       VARCHAR(100) NOT NULL,
    access_type  VARCHAR(20)  NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    presigned_url TEXT        NOT NULL,
    purpose      VARCHAR(100) NOT NULL,
    source       VARCHAR(100) NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    expires_at   DATETIME(6)  NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_single_upload_session_status (status),
    INDEX idx_single_upload_session_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE multipart_upload_session (
    id           VARCHAR(36)  NOT NULL,
    s3_key       VARCHAR(512) NOT NULL,
    bucket       VARCHAR(100) NOT NULL,
    access_type  VARCHAR(20)  NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    upload_id    VARCHAR(255) NOT NULL,
    part_size    BIGINT       NOT NULL,
    purpose      VARCHAR(100) NOT NULL,
    source       VARCHAR(100) NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    expires_at   DATETIME(6)  NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_multipart_upload_session_status (status),
    INDEX idx_multipart_upload_session_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE completed_part (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    session_id   VARCHAR(36)  NOT NULL,
    part_number  INT          NOT NULL,
    etag         VARCHAR(255) NOT NULL,
    size         BIGINT       NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_completed_part_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
