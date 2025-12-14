-- =====================================================
-- V12: Create file_asset_status_history table
-- =====================================================
-- 파일 에셋 상태 변경 이력 테이블
-- Append-Only (수정/삭제 없음)
-- =====================================================

CREATE TABLE file_asset_status_history (
    id BINARY(16) NOT NULL COMMENT '히스토리 ID (UUID v7)',
    file_asset_id VARCHAR(36) NOT NULL COMMENT '파일 에셋 ID',
    from_status VARCHAR(20) NULL COMMENT '이전 상태 (최초 생성 시 NULL)',
    to_status VARCHAR(20) NOT NULL COMMENT '변경된 상태',
    message VARCHAR(500) NULL COMMENT '상태 메시지',
    actor VARCHAR(100) NULL COMMENT '변경 주체',
    actor_type VARCHAR(20) NULL COMMENT '변경 주체 타입 (SYSTEM, N8N, USER 등)',
    changed_at TIMESTAMP(6) NOT NULL COMMENT '변경 시각',
    duration_millis BIGINT NULL COMMENT '이전 상태에서 소요된 시간 (밀리초)',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',

    PRIMARY KEY (id),
    INDEX idx_file_asset_status_history_file_asset_id (file_asset_id),
    INDEX idx_file_asset_status_history_changed_at (changed_at),
    INDEX idx_file_asset_status_history_to_status (to_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='파일 에셋 상태 변경 이력';
