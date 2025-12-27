-- External Download 테이블 스키마 수정
-- 1. idempotency_key 컬럼 추가
-- 2. tenant_id, organization_id를 VARCHAR(36)으로 변경
-- 데이터가 없으므로 DROP 후 재생성

-- 1. 기존 테이블 삭제
DROP TABLE IF EXISTS external_download_outbox;
DROP TABLE IF EXISTS external_download;

-- 2. ExternalDownload 테이블 (수정된 스키마)
CREATE TABLE external_download (
    id BINARY(16) NOT NULL COMMENT 'UUID v7 (Time-Ordered)',
    idempotency_key VARCHAR(36) NOT NULL COMMENT '멱등성 키 (UUID)',
    source_url VARCHAR(2048) NOT NULL COMMENT '다운로드할 외부 이미지 URL',
    tenant_id VARCHAR(36) NOT NULL COMMENT '테넌트 ID (UUID)',
    organization_id VARCHAR(36) NOT NULL COMMENT '조직 ID (UUID)',
    s3_bucket VARCHAR(63) NOT NULL COMMENT 'S3 버킷 이름',
    s3_path_prefix VARCHAR(255) NOT NULL COMMENT 'S3 경로 prefix',
    status VARCHAR(20) NOT NULL COMMENT '상태 (PENDING, PROCESSING, COMPLETED, FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수 (최대 2회)',
    file_asset_id VARCHAR(36) NULL COMMENT '생성된 FileAsset ID (UUID)',
    error_message VARCHAR(500) NULL COMMENT '에러 메시지',
    webhook_url VARCHAR(2048) NULL COMMENT '콜백 웹훅 URL',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (id),
    CONSTRAINT uk_external_download_tenant_idempotency UNIQUE (tenant_id, idempotency_key),
    INDEX idx_external_download_tenant_id (tenant_id),
    INDEX idx_external_download_organization_id (organization_id),
    INDEX idx_external_download_status (status),
    INDEX idx_external_download_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='외부 다운로드 요청';

-- 3. ExternalDownloadOutbox 테이블
CREATE TABLE external_download_outbox (
    id BINARY(16) NOT NULL COMMENT 'UUID v7 (Time-Ordered)',
    external_download_id BINARY(16) NOT NULL COMMENT 'ExternalDownload ID (UUID v7)',
    published BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'SQS 발행 여부',
    published_at DATETIME(6) NULL COMMENT 'SQS 발행 시각',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (id),
    INDEX idx_external_download_outbox_published (published),
    INDEX idx_external_download_outbox_created_at (created_at),
    INDEX idx_external_download_outbox_external_download_id (external_download_id),
    FOREIGN KEY (external_download_id) REFERENCES external_download(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='외부 다운로드 Outbox (SQS 발행용)';
