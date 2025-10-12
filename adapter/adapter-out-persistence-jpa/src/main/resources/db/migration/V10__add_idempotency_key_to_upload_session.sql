-- ========================================
-- V10: Add idempotency_key to upload_session
-- ========================================
-- 멱등성 키를 추가하여 중복 요청 방지 기능 구현
-- ========================================

ALTER TABLE upload_session
    ADD COLUMN idempotency_key VARCHAR(200) NULL COMMENT '멱등성 키 (중복 요청 방지)' AFTER session_id,
    ADD UNIQUE KEY uk_idempotency_key (idempotency_key);
