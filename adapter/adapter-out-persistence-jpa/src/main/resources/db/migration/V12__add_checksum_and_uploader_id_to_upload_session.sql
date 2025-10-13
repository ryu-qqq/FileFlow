-- ========================================
-- V12: Add checksum and uploader_id to upload_session
-- ========================================
-- Issue: KAN-136 - UploadSessionMapper critical issues 해결
--
-- Critical Issues 해결:
-- 1. checksum 필드 추가 - 파일 무결성 검증을 위한 해시값 저장
-- 2. uploader_id 필드 추가 - tenantId와 uploaderId의 의미적 분리
--
-- Breaking Changes:
-- - uploader_id는 NOT NULL 제약조건
-- - 신규 데이터부터 적용 (기존 데이터 없음)
-- ========================================

ALTER TABLE upload_session
    ADD COLUMN checksum VARCHAR(500) NULL COMMENT '파일 체크섬 (SHA-256 등 무결성 검증용)' AFTER file_size,
    ADD COLUMN uploader_id VARCHAR(100) NOT NULL COMMENT '업로더 사용자 ID (tenantId와 별도 관리)' AFTER tenant_id,
    ADD INDEX idx_uploader_id (uploader_id);
