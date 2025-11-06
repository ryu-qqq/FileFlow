package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.UploadSessionEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadSessionJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.QUploadSessionJpaEntity.uploadSessionJpaEntity;

/**
 * Upload Session Query Adapter (CQRS - Query Side)
 *
 * <p>Application Layer의 {@link LoadUploadSessionPort}를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession Domain Aggregate의 조회 (읽기 전용)</li>
 *   <li>QueryDSL을 통한 DB 접근 및 성능 최적화</li>
 *   <li>Mapper를 통한 Entity → Domain 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수</li>
 *   <li>✅ @Transactional(readOnly = true) 적용 (조회 최적화)</li>
 *   <li>✅ QueryDSL 사용 (복잡한 쿼리 타입 안전)</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>✅ Long FK Strategy (JPA 관계 없음)</li>
 *   <li>✅ CQRS - Query 전용 (읽기만)</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * <p><strong>성능 최적화:</strong></p>
 * <ul>
 *   <li>QueryDSL로 동적 쿼리 타입 안전하게 구성</li>
 *   <li>복합 Index 활용: (status, created_at), (tenant_id, status)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionQueryAdapter implements LoadUploadSessionPort {

    private final JPAQueryFactory queryFactory;

    /**
     * 생성자
     *
     * @param queryFactory QueryDSL JPAQueryFactory (복잡한 쿼리용)
     */
    public UploadSessionQueryAdapter(
        JPAQueryFactory queryFactory
    ) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 Upload Session 조회
     *
     * <p><strong>성능:</strong> Primary Key 조회 (O(1))</p>
     *
     * @param id Upload Session ID
     * @return Upload Session (Optional)
     */
    @Override
    public Optional<UploadSession> findById(Long id) {
        UploadSessionJpaEntity entity = queryFactory
            .selectFrom(uploadSessionJpaEntity)
            .where(uploadSessionJpaEntity.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(UploadSessionEntityMapper::toDomain);
    }

    /**
     * Session Key로 Upload Session 조회
     *
     * <p><strong>성능:</strong> Unique Index 조회 (O(log n))</p>
     * <p><strong>Index:</strong> idx_session_key (session_key)</p>
     *
     * @param sessionKey Session Key
     * @return Upload Session (Optional)
     */
    @Override
    public Optional<UploadSession> findBySessionKey(SessionKey sessionKey) {
        UploadSessionJpaEntity entity = queryFactory
            .selectFrom(uploadSessionJpaEntity)
            .where(uploadSessionJpaEntity.sessionKey.eq(sessionKey.value()))
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(UploadSessionEntityMapper::toDomain);
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
     * </ul>
     *
     * @param status 세션 상태
     * @param createdBefore 이 시간 이전에 생성된 세션
     * @return Upload Session 목록
     */
    @Override
    public List<UploadSession> findByStatusAndCreatedBefore(
        SessionStatus status,
        LocalDateTime createdBefore
    ) {
        List<UploadSessionJpaEntity> entities = queryFactory
            .selectFrom(uploadSessionJpaEntity)
            .where(
                uploadSessionJpaEntity.status.eq(status),
                uploadSessionJpaEntity.createdAt.before(createdBefore)
            )
            .orderBy(uploadSessionJpaEntity.createdAt.asc())
            .fetch();

        return entities.stream()
            .map(UploadSessionEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 여러 상태 중 하나에 해당하고 생성 시간 기준으로 Upload Session 목록 조회
     *
     * <p><strong>사용 시나리오:</strong> PENDING 또는 IN_PROGRESS 세션 중 오래된 것 조회</p>
     *
     * <p><strong>성능:</strong></p>
     * <ul>
     *   <li>⚠️ IN 절 사용 → Index 활용 제한적</li>
     *   <li>가능하면 {@link #findByStatusAndCreatedBefore} 사용 권장</li>
     * </ul>
     *
     * @param statuses 세션 상태 목록 (Not Null, Not Empty)
     * @param createdBefore 이 시간 이전에 생성된 세션
     * @return Upload Session 목록
     * @throws IllegalArgumentException statuses가 null 또는 빈 리스트인 경우
     */
    @Override
    public List<UploadSession> findByStatusInAndCreatedBefore(
        List<SessionStatus> statuses,
        LocalDateTime createdBefore
    ) {
        if (statuses == null || statuses.isEmpty()) {
            throw new IllegalArgumentException("statuses must not be null or empty");
        }

        List<UploadSessionJpaEntity> entities = queryFactory
            .selectFrom(uploadSessionJpaEntity)
            .where(
                uploadSessionJpaEntity.status.in(statuses),
                uploadSessionJpaEntity.createdAt.before(createdBefore)
            )
            .orderBy(uploadSessionJpaEntity.createdAt.asc())
            .fetch();

        return entities.stream()
            .map(UploadSessionEntityMapper::toDomain)
            .collect(Collectors.toList());
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
     * <p><strong>예시:</strong></p>
     * <pre>{@code
     * // Tenant의 진행 중인 업로드 개수 확인
     * long activeCount = queryAdapter.countByTenantIdAndStatus(
     *     tenantId,
     *     SessionStatus.IN_PROGRESS
     * );
     *
     * if (activeCount >= MAX_CONCURRENT_UPLOADS) {
     *     throw new RateLimitExceededException("Too many concurrent uploads");
     * }
     * }</pre>
     *
     * @param tenantId Tenant ID (Not Null)
     * @param status 세션 상태 (Not Null)
     * @return 세션 개수 (0 이상)
     * @throws IllegalArgumentException tenantId 또는 status가 null인 경우
     */
    @Override
    public long countByTenantIdAndStatus(Long tenantId, SessionStatus status) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }

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
}
