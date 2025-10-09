-- ========================================
-- V8: Create file_relationship table
-- ========================================
-- 파일 간 관계를 저장하는 테이블
-- 원본-썸네일, 원본-변환본 등의 파일 파생 관계를 추적합니다
-- JSON 메타데이터로 관계별 상세 정보를 유연하게 저장합니다
-- ========================================

CREATE TABLE file_relationship (
    id                    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '파일 관계 ID',
    source_file_id        VARCHAR(36)     NOT NULL COMMENT '원본 파일 ID (UUID)',
    target_file_id        VARCHAR(36)     NOT NULL COMMENT '대상 파일 ID (UUID)',
    relationship_type     VARCHAR(50)     NOT NULL COMMENT '관계 유형 (THUMBNAIL, OPTIMIZED, CONVERTED, DERIVATIVE, VERSION)',
    relationship_metadata JSON            NULL COMMENT '관계별 메타데이터 (크기, 포맷 등)',
    created_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    INDEX idx_source_file_id (source_file_id),
    INDEX idx_target_file_id (target_file_id),
    INDEX idx_relationship_type (relationship_type),
    INDEX idx_created_at (created_at),
    UNIQUE KEY uk_file_relationship (source_file_id, target_file_id, relationship_type),
    CONSTRAINT fk_file_relationship_source FOREIGN KEY (source_file_id) REFERENCES file_asset(file_id),
    CONSTRAINT fk_file_relationship_target FOREIGN KEY (target_file_id) REFERENCES file_asset(file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='파일 관계';
