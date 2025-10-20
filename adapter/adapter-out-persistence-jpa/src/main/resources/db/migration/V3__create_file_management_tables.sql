-- =====================================================
-- FileFlow Database Schema Migration
-- Version: V3 - File Management Tables
-- Description: 파일 자산 및 메타데이터 관리 테이블 생성
-- Author: FileFlow Team
-- Date: 2025-01-20
-- =====================================================

-- =====================================================
-- Table: file_assets (파일 자산)
-- Description: 모든 파일의 기본 정보 관리
-- =====================================================
CREATE TABLE IF NOT EXISTS file_assets (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '파일 ID',
    file_id                 VARCHAR(36)     NOT NULL COMMENT '파일 고유 식별자 (UUID)',
    tenant_id               VARCHAR(50)     NOT NULL COMMENT '테넌트 ID',
    organization_id         BIGINT          NOT NULL COMMENT '조직 ID',
    user_context_id         BIGINT          NOT NULL COMMENT '업로드 사용자 ID',
    session_id              VARCHAR(36)     NULL COMMENT '업로드 세션 ID',
    file_name               VARCHAR(255)    NOT NULL COMMENT '원본 파일명',
    normalized_name         VARCHAR(255)    NOT NULL COMMENT '정규화된 파일명',
    file_extension          VARCHAR(20)     NOT NULL COMMENT '파일 확장자',
    mime_type               VARCHAR(100)    NOT NULL COMMENT 'MIME 타입',
    file_size               BIGINT          NOT NULL COMMENT '파일 크기 (bytes)',
    file_type               ENUM('IMAGE', 'HTML', 'PDF', 'EXCEL', 'WORD', 'TEXT', 'VIDEO', 'AUDIO', 'OTHER') NOT NULL COMMENT '파일 타입',
    file_category           VARCHAR(50)     NULL COMMENT '파일 카테고리',
    storage_type            ENUM('S3', 'LOCAL', 'CDN', 'EXTERNAL') NOT NULL DEFAULT 'S3' COMMENT '저장소 타입',
    storage_bucket          VARCHAR(100)    NOT NULL COMMENT '저장소 버킷/경로',
    storage_path            VARCHAR(1024)   NOT NULL COMMENT '저장 경로',
    storage_url             VARCHAR(2048)   NULL COMMENT '접근 URL',
    cdn_url                 VARCHAR(2048)   NULL COMMENT 'CDN URL',
    checksum_algorithm      VARCHAR(20)     NOT NULL DEFAULT 'SHA256' COMMENT '체크섬 알고리즘',
    checksum_value          VARCHAR(128)    NOT NULL COMMENT '체크섬 값',
    status                  ENUM('UPLOADING', 'PROCESSING', 'AVAILABLE', 'ARCHIVED', 'DELETED', 'ERROR') NOT NULL DEFAULT 'UPLOADING' COMMENT '파일 상태',
    visibility              ENUM('PUBLIC', 'PRIVATE', 'PROTECTED', 'INTERNAL') NOT NULL DEFAULT 'PRIVATE' COMMENT '가시성',
    access_count            BIGINT          NOT NULL DEFAULT 0 COMMENT '접근 횟수',
    download_count          BIGINT          NOT NULL DEFAULT 0 COMMENT '다운로드 횟수',
    virus_scan_status       ENUM('PENDING', 'CLEAN', 'INFECTED', 'ERROR', 'SKIPPED') NULL DEFAULT 'PENDING' COMMENT '바이러스 스캔 상태',
    virus_scan_at           DATETIME        NULL COMMENT '바이러스 스캔 시각',
    metadata                JSON            NULL DEFAULT ('{}') COMMENT '파일 메타데이터',
    tags                    JSON            NULL DEFAULT ('[]') COMMENT '태그 목록',
    expires_at              DATETIME        NULL COMMENT '만료 시각',
    version                 INT             NOT NULL DEFAULT 1 COMMENT '버전',
    is_archived             BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '아카이브 여부',
    archived_at             DATETIME        NULL COMMENT '아카이브 시각',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    deleted_at              DATETIME        NULL COMMENT '삭제 시각 (soft delete)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_id (file_id),
    INDEX idx_tenant_org (tenant_id, organization_id, created_at DESC),
    INDEX idx_user_context (user_context_id, created_at DESC),
    INDEX idx_session_id (session_id),
    INDEX idx_file_type_status (file_type, status),
    INDEX idx_checksum (checksum_algorithm, checksum_value),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='파일 자산'
PARTITION BY RANGE (TO_DAYS(created_at)) (
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
    PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- =====================================================
-- Table: file_variants (파일 변종)
-- Description: 원본 파일의 변형 버전 (썸네일, 리사이즈 등)
-- =====================================================
CREATE TABLE IF NOT EXISTS file_variants (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '변종 ID',
    variant_id              VARCHAR(36)     NOT NULL COMMENT '변종 고유 식별자',
    original_file_id        BIGINT          NOT NULL COMMENT '원본 파일 ID',
    variant_type            ENUM('THUMBNAIL', 'PREVIEW', 'OPTIMIZED', 'CONVERTED', 'RESIZED', 'CROPPED') NOT NULL COMMENT '변종 타입',
    variant_name            VARCHAR(100)    NOT NULL COMMENT '변종명',
    width                   INT             NULL COMMENT '너비 (이미지)',
    height                  INT             NULL COMMENT '높이 (이미지)',
    format                  VARCHAR(20)     NULL COMMENT '포맷',
    quality                 INT             NULL COMMENT '품질 (1-100)',
    file_size               BIGINT          NOT NULL COMMENT '파일 크기',
    storage_path            VARCHAR(1024)   NOT NULL COMMENT '저장 경로',
    storage_url             VARCHAR(2048)   NULL COMMENT '접근 URL',
    cdn_url                 VARCHAR(2048)   NULL COMMENT 'CDN URL',
    processing_params       JSON            NULL DEFAULT ('{}') COMMENT '처리 파라미터',
    status                  ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '상태',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_variant_id (variant_id),
    INDEX idx_original_file (original_file_id, variant_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일 변종';

-- =====================================================
-- Table: file_metadata (파일 메타데이터)
-- Description: 파일의 상세 메타데이터 저장
-- =====================================================
CREATE TABLE IF NOT EXISTS file_metadata (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '메타데이터 ID',
    file_id                 BIGINT          NOT NULL COMMENT '파일 ID',
    metadata_key            VARCHAR(100)    NOT NULL COMMENT '메타데이터 키',
    metadata_value          TEXT            NULL COMMENT '메타데이터 값',
    value_type              ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'DATE') NOT NULL DEFAULT 'STRING' COMMENT '값 타입',
    category                VARCHAR(50)     NULL COMMENT '카테고리',
    is_indexed              BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '인덱싱 여부',
    is_searchable           BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '검색 가능 여부',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_metadata_key (file_id, metadata_key),
    INDEX idx_file_id (file_id),
    INDEX idx_metadata_key (metadata_key),
    INDEX idx_category (category),
    FULLTEXT idx_metadata_value (metadata_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일 메타데이터';

-- =====================================================
-- Table: file_relationships (파일 관계)
-- Description: 파일 간 관계 정의
-- =====================================================
CREATE TABLE IF NOT EXISTS file_relationships (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '관계 ID',
    source_file_id          BIGINT          NOT NULL COMMENT '소스 파일 ID',
    target_file_id          BIGINT          NOT NULL COMMENT '대상 파일 ID',
    relationship_type       ENUM('PARENT_CHILD', 'VERSION', 'RELATED', 'DUPLICATE', 'REFERENCE') NOT NULL COMMENT '관계 타입',
    description             TEXT            NULL COMMENT '관계 설명',
    metadata                JSON            NULL DEFAULT ('{}') COMMENT '관계 메타데이터',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    created_by              BIGINT          NULL COMMENT '생성자 ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_relationship (source_file_id, target_file_id, relationship_type),
    INDEX idx_source_file (source_file_id),
    INDEX idx_target_file (target_file_id),
    INDEX idx_relationship_type (relationship_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일 관계';

-- =====================================================
-- Table: file_tags (파일 태그)
-- Description: 파일 태그 관리
-- =====================================================
CREATE TABLE IF NOT EXISTS file_tags (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '태그 ID',
    file_id                 BIGINT          NOT NULL COMMENT '파일 ID',
    tag_name                VARCHAR(50)     NOT NULL COMMENT '태그명',
    tag_category            VARCHAR(50)     NULL COMMENT '태그 카테고리',
    tag_value               VARCHAR(255)    NULL COMMENT '태그 값',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    created_by              BIGINT          NULL COMMENT '생성자 ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_tag (file_id, tag_name),
    INDEX idx_file_id (file_id),
    INDEX idx_tag_name (tag_name),
    INDEX idx_tag_category (tag_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일 태그';

-- =====================================================
-- Table: file_shares (파일 공유)
-- Description: 파일 공유 정보 관리
-- =====================================================
CREATE TABLE IF NOT EXISTS file_shares (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '공유 ID',
    share_id                VARCHAR(36)     NOT NULL COMMENT '공유 고유 식별자',
    file_id                 BIGINT          NOT NULL COMMENT '파일 ID',
    shared_by               BIGINT          NOT NULL COMMENT '공유한 사용자 ID',
    share_type              ENUM('PUBLIC', 'PRIVATE', 'PASSWORD', 'TEMPORARY') NOT NULL COMMENT '공유 타입',
    share_url               VARCHAR(500)    NOT NULL COMMENT '공유 URL',
    password_hash           VARCHAR(255)    NULL COMMENT '비밀번호 해시',
    access_count            INT             NOT NULL DEFAULT 0 COMMENT '접근 횟수',
    max_access_count        INT             NULL COMMENT '최대 접근 횟수',
    allowed_actions         JSON            NULL DEFAULT ('["VIEW","DOWNLOAD"]') COMMENT '허용 액션',
    recipient_emails        JSON            NULL DEFAULT ('[]') COMMENT '수신자 이메일',
    expires_at              DATETIME        NULL COMMENT '만료 시각',
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 상태',
    metadata                JSON            NULL DEFAULT ('{}') COMMENT '추가 메타데이터',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    revoked_at              DATETIME        NULL COMMENT '취소 시각',
    revoked_by              BIGINT          NULL COMMENT '취소한 사용자 ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_share_id (share_id),
    UNIQUE KEY uk_share_url (share_url),
    INDEX idx_file_id (file_id),
    INDEX idx_shared_by (shared_by),
    INDEX idx_share_type (share_type),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일 공유';

-- =====================================================
-- Table: file_versions (파일 버전 관리)
-- Description: 파일 버전 히스토리
-- =====================================================
CREATE TABLE IF NOT EXISTS file_versions (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '버전 ID',
    file_id                 BIGINT          NOT NULL COMMENT '파일 ID',
    version_number          INT             NOT NULL COMMENT '버전 번호',
    version_label           VARCHAR(50)     NULL COMMENT '버전 라벨',
    change_type             ENUM('CREATE', 'UPDATE', 'REPLACE', 'RESTORE') NOT NULL COMMENT '변경 타입',
    change_description      TEXT            NULL COMMENT '변경 설명',
    previous_version_id     BIGINT          NULL COMMENT '이전 버전 ID',
    file_size               BIGINT          NOT NULL COMMENT '파일 크기',
    checksum_value          VARCHAR(128)    NOT NULL COMMENT '체크섬 값',
    storage_path            VARCHAR(1024)   NOT NULL COMMENT '저장 경로',
    metadata_changes        JSON            NULL COMMENT '메타데이터 변경사항',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    created_by              BIGINT          NOT NULL COMMENT '생성자 ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_version (file_id, version_number),
    INDEX idx_file_id (file_id, version_number DESC),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파일 버전 관리';
