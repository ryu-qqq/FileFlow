package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.QUploadSessionJpaEntity.uploadSessionJpaEntity;

/**
 * UploadSessionQueryDslRepository - UploadSession QueryDSL 전용 Repository
 *
 * <p>QueryDSL JPAQueryFactory를 사용하여 UploadSession 조회 쿼리를 실행합니다.
 * 동적 쿼리 조건을 활용하여 성능 최적화된 조회를 제공합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>QueryDSL을 사용한 UploadSession 조회 쿼리 실행</li>
 *   <li>동적 쿼리 조건 생성 및 적용</li>
 *   <li>성능 최적화 (Index 활용, LIMIT 절)</li>
 *   <li>OOM 방지 (Unbounded Query 방지)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ 구현체만 존재 (인터페이스 추상화 불필요)</li>
 *   <li>✅ JPAQueryFactory를 통한 QueryDSL 쿼리 실행</li>
 *   <li>✅ EntityManager를 생성자로 받아서 JPAQueryFactory 생성</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Helper 메서드를 통한 동적 쿼리 조건 생성</li>
 *   <li>✅ CQRS Query Side 전용 (조회만)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionQueryDslRepository {

    private static final int SAFE_BATCH_SIZE = 1000;

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    /**
     * 생성자
     *
     * <p>JPAQueryFactory와 EntityManager를 받습니다.</p>
     * <p>EntityManager는 Pessimistic Lock 쿼리 실행 시 사용됩니다.</p>
     *
     * @param queryFactory JPA Query Factory
     * @param entityManager JPA Entity Manager
     */
    public UploadSessionQueryDslRepository(
        JPAQueryFactory queryFactory,
        EntityManager entityManager
    ) {
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
    }

    /**
     * ID로 Upload Session 조회
     *
     * <p><strong>성능:</strong> Primary Key 조회 (O(1))</p>
     *
     * @param id Upload Session ID
     * @return Upload Session Entity (Optional)
     */
    public Optional<UploadSessionJpaEntity> findById(Long id) {
        UploadSessionJpaEntity entity = queryFactory
            .selectFrom(uploadSessionJpaEntity)
            .where(uploadSessionJpaEntity.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * Session Key로 Upload Session 조회
     *
     * <p><strong>성능:</strong> Unique Index 조회 (O(log n))</p>
     * <p><strong>Index:</strong> idx_session_key (session_key)</p>
     *
     * @param sessionKey Session Key
     * @return Upload Session Entity (Optional)
     */
    public Optional<UploadSessionJpaEntity> findBySessionKey(String sessionKey) {
        UploadSessionJpaEntity entity = queryFactory
            .selectFrom(uploadSessionJpaEntity)
            .where(uploadSessionJpaEntity.sessionKey.eq(sessionKey))
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * 상태와 생성 시간 기준으로 Upload Session 목록 조회
     *
     * <p><strong>사용 시나리오:</strong> 만료된 PENDING 세션 Batch 정리</p>
     *
     * <p><strong>성능:</strong></p>
     * <ul>
     *   <li>복합 Index 활용: idx_status_created_at (status, created_at)</li>
     *   <li>⚠️ 대량 데이터 반환 가능 → Batch 처리 시 LIMIT 사용 권장</li>
     *   <li>기본 LIMIT: 1000 (OOM 방지)</li>
     * </ul>
     *
     * @param status 세션 상태
     * @param createdBefore 이 시간 이전에 생성된 세션
     * @return Upload Session Entity 목록 (최대 1000개)
     */
    public List<UploadSessionJpaEntity> findByStatusAndCreatedBefore(
        SessionStatus status,
        LocalDateTime createdBefore
    ) {
        // CRITICAL: Unbounded Query 방지 - 안전한 기본값 설정
        // 대량 데이터 조회 시 OOM 방지를 위해 LIMIT 절 필수
        // TODO: 향후 Pageable 파라미터로 전환 고려
        return queryFactory
            .selectFrom(uploadSessionJpaEntity)
            .where(
                uploadSessionJpaEntity.status.eq(status),
                uploadSessionJpaEntity.createdAt.before(createdBefore)
            )
            .orderBy(uploadSessionJpaEntity.createdAt.asc())
            .limit(SAFE_BATCH_SIZE)
            .fetch();
    }

    /**
     * 여러 상태 중 하나에 해당하고 생성 시간 기준으로 Upload Session 목록 조회
     *
     * <p><strong>사용 시나리오:</strong> PENDING 또는 IN_PROGRESS 세션 중 오래된 것 조회</p>
     *
     * <p><strong>성능:</strong></p>
     * <ul>
     *   <li>⚠️ IN 절 사용 → Index 활용 제한적</li>
     *   <li>가능하면 findByStatusAndCreatedBefore 사용 권장</li>
     *   <li>기본 LIMIT: 1000 (OOM 방지)</li>
     * </ul>
     *
     * @param statuses 세션 상태 목록 (Not Null, Not Empty)
     * @param createdBefore 이 시간 이전에 생성된 세션
     * @return Upload Session Entity 목록 (최대 1000개)
     * @throws IllegalArgumentException statuses가 null 또는 빈 리스트인 경우
     */
    public List<UploadSessionJpaEntity> findByStatusInAndCreatedBefore(
        List<SessionStatus> statuses,
        LocalDateTime createdBefore
    ) {
        validateStatusesNotEmpty(statuses);

        // CRITICAL: Unbounded Query 방지 - 안전한 기본값 설정
        // IN 절 사용 시 예상보다 많은 데이터 반환 가능
        return queryFactory
            .selectFrom(uploadSessionJpaEntity)
            .where(
                uploadSessionJpaEntity.status.in(statuses),
                uploadSessionJpaEntity.createdAt.before(createdBefore)
            )
            .orderBy(uploadSessionJpaEntity.createdAt.asc())
            .limit(SAFE_BATCH_SIZE)
            .fetch();
    }

    /**
     * Tenant ID와 상태로 Upload Session 개수 조회
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>Tenant별 활성 업로드 세션 모니터링</li>
     *   <li>Rate Limiting 구현 (Tenant당 동시 업로드 제한)</li>
     * </ul>
     *
     * <p><strong>성능:</strong></p>
     * <ul>
     *   <li>복합 Index 활용: idx_tenant_status (tenant_id, status)</li>
     *   <li>COUNT(*) 쿼리 → Index만 사용 (테이블 스캔 없음)</li>
     * </ul>
     *
     * @param tenantId Tenant ID (Not Null)
     * @param status 세션 상태 (Not Null)
     * @return 세션 개수 (0 이상)
     * @throws IllegalArgumentException tenantId 또는 status가 null인 경우
     */
    public long countByTenantIdAndStatus(Long tenantId, SessionStatus status) {
        validateTenantIdNotNull(tenantId);
        validateStatusNotNull(status);

        Long count = queryFactory
            .select(uploadSessionJpaEntity.count())
            .from(uploadSessionJpaEntity)
            .where(
                uploadSessionJpaEntity.tenantId.eq(tenantId),
                uploadSessionJpaEntity.status.eq(status)
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * statuses 유효성 검증 Helper
     *
     * @param statuses 검증할 상태 목록
     * @throws IllegalArgumentException statuses가 null 또는 빈 리스트인 경우
     */
    private void validateStatusesNotEmpty(List<SessionStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            throw new IllegalArgumentException("statuses must not be null or empty");
        }
    }

    /**
     * tenantId 유효성 검증 Helper
     *
     * @param tenantId 검증할 Tenant ID
     * @throws IllegalArgumentException tenantId가 null인 경우
     */
    private void validateTenantIdNotNull(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
    }

    /**
     * status 유효성 검증 Helper
     *
     * @param status 검증할 세션 상태
     * @throws IllegalArgumentException status가 null인 경우
     */
    private void validateStatusNotNull(SessionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
    }

    /**
     * 상태와 생성시간 기준으로 만료된 세션 조회 (Pessimistic Lock + Limit)
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>CleanupExpiredSessionsScheduler: 만료된 세션 정리</li>
     *   <li>동시 실행되는 스케줄러 인스턴스 간 경합 방지</li>
     * </ul>
     *
     * <p><strong>Pessimistic Lock (FOR UPDATE SKIP LOCKED):</strong></p>
     * <ul>
     *   <li>잠금 획득된 Row만 반환 (잠금 실패 시 Skip)</li>
     *   <li>동시 실행 스케줄러가 같은 세션을 처리하지 않도록 보장</li>
     *   <li>Deadlock 방지: SKIP LOCKED 사용</li>
     * </ul>
     *
     * <p><strong>성능:</strong></p>
     * <ul>
     *   <li>Index 활용: {@code idx_status_created_at (status, created_at)}</li>
     *   <li>Limit으로 대량 조회 방지 (권장: 100~1000)</li>
     *   <li>트랜잭션 종료 시 Lock 자동 해제</li>
     * </ul>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>반드시 트랜잭션 내에서 호출되어야 함</li>
     *   <li>Lock Timeout 설정: SKIP LOCKED</li>
     *   <li>트랜잭션은 짧게 유지 (외부 API 호출 금지)</li>
     * </ul>
     *
     * <p><strong>SQL 예시 (MySQL):</strong></p>
     * <pre>{@code
     * SELECT *
     * FROM upload_session
     * WHERE status = ?
     *   AND created_at < ?
     * ORDER BY created_at ASC
     * LIMIT ?
     * FOR UPDATE SKIP LOCKED;
     * }</pre>
     *
     * @param status 세션 상태 (PENDING, IN_PROGRESS 등, Not Null)
     * @param threshold 이 시간 이전에 생성된 세션만 조회 (Not Null)
     * @param limit 최대 조회 건수 (1 이상, 권장: 100~1000)
     * @return 만료된 세션 목록 (잠금 획득된 세션만, 최대 limit 건)
     * @throws IllegalArgumentException status, threshold가 null이거나 limit < 1인 경우
     */
    public List<UploadSessionJpaEntity> findByStatusAndCreatedBeforeWithLock(
        SessionStatus status,
        LocalDateTime threshold,
        int limit
    ) {
        validateStatusNotNull(status);
        validateThresholdNotNull(threshold);
        validateLimitPositive(limit);

        // JPQL을 사용한 Pessimistic Lock 쿼리
        // FOR UPDATE SKIP LOCKED는 setLockMode + setHint로 구현
        return entityManager.createQuery(
                "SELECT u FROM UploadSessionJpaEntity u " +
                "WHERE u.status = :status " +
                "AND u.createdAt < :threshold " +
                "ORDER BY u.createdAt ASC",
                UploadSessionJpaEntity.class
            )
            .setParameter("status", status)
            .setParameter("threshold", threshold)
            .setMaxResults(limit)
            .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
            .setHint("jakarta.persistence.lock.timeout", -2) // -2 = SKIP LOCKED
            .getResultList();
    }

    /**
     * 여러 상태와 생성시간 기준으로 만료된 세션 조회 (Pessimistic Lock + Limit)
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>CleanupExpiredSessionsScheduler: 복수 상태 일괄 정리</li>
     *   <li>PENDING과 IN_PROGRESS를 한 번에 조회하여 처리</li>
     * </ul>
     *
     * <p><strong>Pessimistic Lock (FOR UPDATE SKIP LOCKED):</strong></p>
     * <ul>
     *   <li>{@link #findByStatusAndCreatedBeforeWithLock}와 동일한 Lock 전략</li>
     *   <li>여러 상태에 대해 동일한 Lock 메커니즘 적용</li>
     * </ul>
     *
     * <p><strong>성능:</strong></p>
     * <ul>
     *   <li>⚠️ IN 절 사용으로 Index 효율 저하 가능</li>
     *   <li>가급적 단일 상태 조회 메서드 사용 권장</li>
     *   <li>Index: {@code idx_status_created_at (status, created_at)}</li>
     * </ul>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>statuses가 비어있으면 빈 리스트 반환</li>
     *   <li>반드시 트랜잭션 내에서 호출</li>
     *   <li>트랜잭션은 짧게 유지</li>
     * </ul>
     *
     * <p><strong>SQL 예시 (MySQL):</strong></p>
     * <pre>{@code
     * SELECT *
     * FROM upload_session
     * WHERE status IN (?, ?)
     *   AND created_at < ?
     * ORDER BY created_at ASC
     * LIMIT ?
     * FOR UPDATE SKIP LOCKED;
     * }</pre>
     *
     * @param statuses 세션 상태 목록 (Not Null, Not Empty)
     * @param threshold 이 시간 이전에 생성된 세션만 조회 (Not Null)
     * @param limit 최대 조회 건수 (1 이상, 권장: 100~1000)
     * @return 만료된 세션 목록 (잠금 획득된 세션만, 최대 limit 건)
     * @throws IllegalArgumentException statuses, threshold가 null이거나 limit < 1인 경우
     */
    public List<UploadSessionJpaEntity> findByStatusInAndCreatedBeforeWithLock(
        List<SessionStatus> statuses,
        LocalDateTime threshold,
        int limit
    ) {
        validateStatusesNotEmpty(statuses);
        validateThresholdNotNull(threshold);
        validateLimitPositive(limit);

        // JPQL을 사용한 Pessimistic Lock 쿼리 (IN 절)
        return entityManager.createQuery(
                "SELECT u FROM UploadSessionJpaEntity u " +
                "WHERE u.status IN :statuses " +
                "AND u.createdAt < :threshold " +
                "ORDER BY u.createdAt ASC",
                UploadSessionJpaEntity.class
            )
            .setParameter("statuses", statuses)
            .setParameter("threshold", threshold)
            .setMaxResults(limit)
            .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
            .setHint("jakarta.persistence.lock.timeout", -2) // -2 = SKIP LOCKED
            .getResultList();
    }

    /**
     * threshold 유효성 검증 Helper
     *
     * @param threshold 검증할 시간
     * @throws IllegalArgumentException threshold가 null인 경우
     */
    private void validateThresholdNotNull(LocalDateTime threshold) {
        if (threshold == null) {
            throw new IllegalArgumentException("threshold must not be null");
        }
    }

    /**
     * limit 유효성 검증 Helper
     *
     * @param limit 검증할 limit 값
     * @throws IllegalArgumentException limit이 1 미만인 경우
     */
    private void validateLimitPositive(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1");
        }
    }
}
