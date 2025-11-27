-- External Download 테이블 생성
-- 외부 URL 다운로드 요청 및 Outbox 패턴을 위한 테이블

-- ExternalDownload 테이블
CREATE TABLE external_download (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_url VARCHAR(2048) NOT NULL COMMENT '다운로드할 외부 이미지 URL',
    tenant_id BIGINT NOT NULL COMMENT '테넌트 ID',
    organization_id BIGINT NOT NULL COMMENT '조직 ID',
    status VARCHAR(20) NOT NULL COMMENT '상태 (PENDING, PROCESSING, COMPLETED, FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수 (최대 2회)',
    file_asset_id VARCHAR(36) NULL COMMENT '생성된 FileAsset ID (UUID)',
    error_message VARCHAR(500) NULL COMMENT '에러 메시지',
    webhook_url VARCHAR(2048) NULL COMMENT '콜백 웹훅 URL',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (id),
    INDEX idx_external_download_tenant_id (tenant_id),
    INDEX idx_external_download_organization_id (organization_id),
    INDEX idx_external_download_status (status),
    INDEX idx_external_download_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='외부 다운로드 요청';

-- ExternalDownloadOutbox 테이블 (Outbox 패턴)
CREATE TABLE external_download_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    external_download_id BIGINT NOT NULL COMMENT 'ExternalDownload ID',
    published BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'SQS 발행 여부',
    published_at DATETIME(6) NULL COMMENT 'SQS 발행 시각',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (id),
    INDEX idx_external_download_outbox_published (published),
    INDEX idx_external_download_outbox_created_at (created_at),
    INDEX idx_external_download_outbox_external_download_id (external_download_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='외부 다운로드 Outbox (SQS 발행용)';
