-- file_processing_outbox 테이블 생성
-- Transactional Outbox 패턴을 위한 이벤트 메시지 큐

CREATE TABLE file_processing_outbox (
    id BINARY(16) NOT NULL COMMENT 'UUID v7 (Time-Ordered)',
    file_asset_id VARCHAR(36) NOT NULL COMMENT 'FileAsset ID',
    event_type VARCHAR(50) NOT NULL COMMENT '이벤트 타입',
    payload TEXT NOT NULL COMMENT '이벤트 페이로드 (JSON)',
    status VARCHAR(20) NOT NULL COMMENT '상태 (PENDING, SENT, FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    error_message VARCHAR(500) NULL COMMENT '에러 메시지',
    processed_at DATETIME(6) NULL COMMENT '처리 완료 시각',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (id),
    INDEX idx_file_processing_outbox_status (status),
    INDEX idx_file_processing_outbox_file_asset_id (file_asset_id),
    INDEX idx_file_processing_outbox_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일 처리 Outbox (이벤트 발행용)';
