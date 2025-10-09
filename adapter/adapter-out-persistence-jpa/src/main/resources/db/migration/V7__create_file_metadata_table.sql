-- ========================================
-- V7: Create file_metadata table
-- ========================================
-- 파일의 상세 메타데이터를 키-값 저장소 구조로 저장하는 테이블
-- 다양한 파일 타입(IMAGE, VIDEO, DOCUMENT, CUSTOM)의
-- 메타데이터를 유연하게 저장하고 조회할 수 있습니다
--
-- Prerequisites:
--   - file_asset 테이블이 먼저 생성되어야 합니다
--
-- Metadata Examples:
--   IMAGE: width, height, format, color_space, has_alpha
--   VIDEO: duration, resolution, codec, bitrate, frame_rate
--   DOCUMENT: page_count, author, title, created_date
--   CUSTOM: 사용자 정의 메타데이터
-- ========================================

CREATE TABLE file_metadata (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '메타데이터 레코드 ID',
    file_id         VARCHAR(36)     NOT NULL COMMENT '파일 ID (UUID, FK to file_asset)',
    metadata_key    VARCHAR(100)    NOT NULL COMMENT '메타데이터 키 (예: width, height, duration)',
    metadata_value  TEXT            NULL COMMENT '메타데이터 값 (문자열 형태로 저장)',
    value_type      VARCHAR(20)     NOT NULL COMMENT '값의 데이터 타입 (STRING, NUMBER, BOOLEAN, JSON)',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uk_file_metadata (file_id, metadata_key),
    INDEX idx_file_id (file_id),
    INDEX idx_metadata_key (metadata_key),

    CONSTRAINT fk_file_metadata_file_asset
        FOREIGN KEY (file_id)
        REFERENCES file_asset(file_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='파일 메타데이터 (키-값 저장소)';
