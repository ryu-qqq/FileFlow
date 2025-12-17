-- Webhook Outbox 테이블 생성
-- Webhook 발송을 위한 Outbox 패턴 테이블

CREATE TABLE webhook_outbox (
    id BINARY(16) NOT NULL COMMENT 'UUID v7 기반 PK',
    external_download_id BINARY(16) NOT NULL COMMENT 'ExternalDownload ID (UUID)',
    webhook_url VARCHAR(2048) NOT NULL COMMENT 'Webhook 호출 URL',
    status VARCHAR(20) NOT NULL COMMENT '발송 상태 (PENDING, SENT, FAILED)',
    download_status VARCHAR(20) NOT NULL COMMENT '다운로드 결과 상태 (COMPLETED, FAILED)',
    file_asset_id BINARY(16) NULL COMMENT '생성된 FileAsset ID (UUID, 성공 시)',
    error_message VARCHAR(500) NULL COMMENT '다운로드 에러 메시지 (실패 시)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수 (최대 2회)',
    last_error_message VARCHAR(500) NULL COMMENT '마지막 Webhook 호출 에러 메시지',
    sent_at DATETIME(6) NULL COMMENT '발송 성공 시각',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (id),
    INDEX idx_webhook_outbox_status (status),
    INDEX idx_webhook_outbox_status_retry (status, retry_count),
    INDEX idx_webhook_outbox_created_at (created_at),
    INDEX idx_webhook_outbox_external_download_id (external_download_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook Outbox (Webhook 발송용)';
