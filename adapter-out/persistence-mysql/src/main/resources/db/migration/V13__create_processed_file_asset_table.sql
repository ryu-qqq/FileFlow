-- =====================================================
-- V13: Create processed_file_asset table
-- =====================================================
-- 처리된 파일 에셋 테이블 (리사이징, 포맷 변환 결과)
-- =====================================================

CREATE TABLE processed_file_asset (
    id BINARY(16) NOT NULL COMMENT '처리된 파일 에셋 ID (UUID v7)',
    original_asset_id VARCHAR(36) NOT NULL COMMENT '원본 파일 에셋 ID',
    parent_asset_id VARCHAR(36) NULL COMMENT '부모 에셋 ID (HTML 추출 이미지용)',

    variant_type VARCHAR(20) NOT NULL COMMENT '이미지 변형 타입 (ORIGINAL, LARGE, MEDIUM, SMALL, THUMBNAIL)',
    format_type VARCHAR(20) NOT NULL COMMENT '이미지 포맷 타입 (JPEG, PNG, WEBP, GIF)',

    file_name VARCHAR(255) NOT NULL COMMENT '파일명',
    file_size BIGINT NOT NULL COMMENT '파일 크기 (bytes)',

    bucket VARCHAR(63) NOT NULL COMMENT 'S3 버킷명',
    s3_key VARCHAR(1024) NOT NULL COMMENT 'S3 키',

    user_id VARCHAR(36) NULL COMMENT '사용자 ID',
    organization_id VARCHAR(36) NOT NULL COMMENT '조직 ID',
    tenant_id VARCHAR(36) NOT NULL COMMENT '테넌트 ID',

    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',

    PRIMARY KEY (id),
    INDEX idx_processed_file_asset_original_id (original_asset_id),
    INDEX idx_processed_file_asset_parent_id (parent_asset_id),
    INDEX idx_processed_file_asset_variant (variant_type),
    INDEX idx_processed_file_asset_tenant_org (tenant_id, organization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='처리된 파일 에셋 (리사이징, 포맷 변환 결과)';
