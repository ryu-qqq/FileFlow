-- ========================================
-- V13: Add version column for Optimistic Locking
-- ========================================
-- Issue: KAN-137 - Optimistic Locking 구현으로 동시성 제어 강화
--
-- 목적:
-- 1. JPA Optimistic Locking을 위한 version 컬럼 추가
-- 2. Race Condition 방지 (중복 S3 이벤트, 동시 업데이트 시나리오)
-- 3. 멱등성 보장 및 데이터 일관성 유지
--
-- 동작 방식:
-- - UPDATE 시마다 version이 1씩 자동 증가
-- - UPDATE WHERE 절에 version 조건이 자동 추가됨
-- - 다른 트랜잭션이 먼저 UPDATE 했다면 OptimisticLockException 발생
--
-- Breaking Changes: 없음
-- - 기존 데이터에 version=0 기본값 설정 (하위 호환성 유지)
-- - 신규 데이터부터 자동으로 version 관리됨
-- ========================================

ALTER TABLE upload_session
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0
    COMMENT 'JPA Optimistic Locking 버전 (동시성 제어용, Issue #46 KAN-137)'
    AFTER id;
