-- ========================================
-- V2: Add version column for optimistic locking
-- ========================================

-- Single Upload Session 테이블에 version 컬럼 추가
ALTER TABLE single_upload_session
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Multipart Upload Session 테이블에 version 컬럼 추가
ALTER TABLE multipart_upload_session
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
