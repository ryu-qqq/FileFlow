-- ========================================
-- V7: Create file_asset table
-- ========================================
-- 업로드된 파일의 기본 정보를 저장하는 테이블
-- S3에 저장된 파일의 메타데이터와 CDN URL,
-- 체크섬 등 파일 관리에 필요한 정보를 추적합니다
-- ========================================

CREATE TABLE file_asset (
    id                   BIGINT          NOT NULL AUTO_INCREMENT COMMENT '파일 자산 ID',
    file_id              VARCHAR(36)     NOT NULL COMMENT '파일 고유 식별자 (UUID)',
    session_id           VARCHAR(36)     NOT NULL COMMENT '업로드 세션 ID',
    tenant_id            VARCHAR(50)     NOT NULL COMMENT '테넌트 ID',
    original_file_name   VARCHAR(255)    NOT NULL COMMENT '원본 파일 이름',
    stored_file_name     VARCHAR(255)    NOT NULL COMMENT '저장된 파일 이름',
    s3_bucket            VARCHAR(100)    NOT NULL COMMENT 'S3 버킷 이름',
    s3_key               VARCHAR(1024)   NOT NULL COMMENT 'S3 객체 키 (AWS 최대 1024 바이트)',
    s3_region            VARCHAR(50)     NOT NULL COMMENT 'S3 리전',
    cdn_url              VARCHAR(2048)   NULL COMMENT 'CDN URL (URL 표준 최대 2048자)',
    file_size            BIGINT          NOT NULL COMMENT '파일 크기 (bytes)',
    content_type         VARCHAR(100)    NOT NULL COMMENT 'MIME 타입',
    file_extension       VARCHAR(20)     NULL COMMENT '파일 확장자',
    checksum             VARCHAR(64)     NULL COMMENT '파일 체크섬 (SHA-256)',
    is_public            BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '공개 여부',
    created_at           DATETIME        NOT NULL COMMENT '생성 시각',
    updated_at           DATETIME        NOT NULL COMMENT '수정 시각',
    deleted_at           DATETIME        NULL COMMENT '삭제 시각 (soft delete)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_id (file_id),
    INDEX idx_session_id (session_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_s3_key (s3_key(255)),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_file_asset_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id),
    CONSTRAINT fk_file_asset_upload_session FOREIGN KEY (session_id) REFERENCES upload_session(session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='파일 자산';
