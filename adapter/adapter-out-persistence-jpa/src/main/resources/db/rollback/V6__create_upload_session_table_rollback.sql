-- ========================================
-- V6 Rollback: Drop upload_session table
-- ========================================
-- upload_session 테이블을 삭제합니다
-- 외래키 제약조건이 있으므로 테이블 삭제만 진행
-- ========================================

DROP TABLE IF EXISTS upload_session;
