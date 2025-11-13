-- ============================================================================
-- Migration: V2__add_upload_session_composite_indexes.sql
-- Author: Sangwon Ryu
-- Date: 2025-11-05
-- Description: Upload Session 테이블에 성능 최적화를 위한 복합 Index 추가
-- ============================================================================

-- ============================================================================
-- 1. 복합 Index: (status, created_at)
-- ============================================================================
-- 목적: findByStatusAndCreatedBefore() 쿼리 최적화
-- 사용 시나리오:
--   - 만료된 PENDING 세션 Batch 정리
--   - 특정 상태의 오래된 세션 모니터링
--   - 세션 생명주기 관리 (Expired, Abandoned)
--
-- 예시 쿼리:
--   SELECT * FROM upload_session
--   WHERE status = 'PENDING'
--     AND created_at < '2025-11-05 10:00:00'
--   ORDER BY created_at ASC
--   LIMIT 1000;
--
-- 성능 효과:
--   - Index Range Scan (O(log n + m)) - m은 결과 건수
--   - Table Full Scan 방지
--   - ORDER BY created_at 정렬 비용 제거 (Index 순서 활용)
-- ============================================================================

CREATE INDEX idx_upload_session_status_created_at
    ON upload_session (status, created_at);

-- ============================================================================
-- 2. 복합 Index: (tenant_id, status)
-- ============================================================================
-- 목적: countByTenantIdAndStatus() 쿼리 최적화
-- 사용 시나리오:
--   - Tenant별 활성 업로드 세션 모니터링
--   - Rate Limiting 구현 (Tenant당 동시 업로드 제한)
--   - Tenant별 리소스 사용량 추적
--
-- 예시 쿼리:
--   SELECT COUNT(*)
--   FROM upload_session
--   WHERE tenant_id = 1001
--     AND status = 'IN_PROGRESS';
--
-- 성능 효과:
--   - Covering Index (Index만으로 COUNT 계산 가능)
--   - Table Access 불필요 (Index Scan만 수행)
--   - O(log n) 성능 (n = tenant_id의 전체 세션 수)
-- ============================================================================

CREATE INDEX idx_upload_session_tenant_status
    ON upload_session (tenant_id, status);

-- ============================================================================
-- 3. Index 통계 및 검증 쿼리 (참고용 - 실행 불필요)
-- ============================================================================
-- Index 효율성 검증:
--
-- EXPLAIN SELECT * FROM upload_session
-- WHERE status = 'PENDING'
--   AND created_at < NOW() - INTERVAL 30 MINUTE;
-- → Expected: type=range, key=idx_upload_session_status_created_at
--
-- EXPLAIN SELECT COUNT(*) FROM upload_session
-- WHERE tenant_id = 1001 AND status = 'IN_PROGRESS';
-- → Expected: type=ref, key=idx_upload_session_tenant_status, Extra=Using index
--
-- ============================================================================

-- ============================================================================
-- 4. 기존 Index 확인 (참고용)
-- ============================================================================
-- 기존에 이미 존재하는 단일 Index:
--   - PRIMARY KEY (id)
--   - UNIQUE KEY idx_session_key (session_key)
--   - INDEX idx_tenant_id (tenant_id)
--   - INDEX idx_status (status)
--
-- 새로 추가된 복합 Index:
--   - INDEX idx_upload_session_status_created_at (status, created_at)
--   - INDEX idx_upload_session_tenant_status (tenant_id, status)
--
-- ⚠️ 주의: 복합 Index는 단일 Index를 대체할 수 없음
-- - idx_status는 status만 조회 시 여전히 사용됨
-- - idx_tenant_id는 tenant_id만 조회 시 여전히 사용됨
-- ============================================================================

-- ============================================================================
-- 5. Index 사이즈 모니터링 (참고용)
-- ============================================================================
-- Index 사이즈 확인 쿼리:
--
-- SELECT
--     TABLE_NAME,
--     INDEX_NAME,
--     ROUND(STAT_VALUE * @@innodb_page_size / 1024 / 1024, 2) AS size_mb
-- FROM mysql.innodb_index_stats
-- WHERE TABLE_NAME = 'upload_session'
--   AND DATABASE_NAME = DATABASE()
--   AND STAT_NAME = 'size'
-- ORDER BY size_mb DESC;
--
-- ============================================================================
