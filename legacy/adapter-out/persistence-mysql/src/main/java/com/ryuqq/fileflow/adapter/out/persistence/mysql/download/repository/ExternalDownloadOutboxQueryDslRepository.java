package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.QExternalDownloadOutboxJpaEntity.externalDownloadOutboxJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.OutboxStatus;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ExternalDownloadOutboxQueryDslRepository - ExternalDownloadOutbox QueryDSL 전용 Repository
 *
 * <p>QueryDSL을 사용한 동적 쿼리 구현입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>QueryDSL 기반 동적 쿼리 실행</li>
 *   <li>복잡한 조회 조건 처리</li>
 *   <li>타입 안전한 쿼리 작성</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ 구현체만 존재 (인터페이스 추상화 불필요)</li>
 *   <li>✅ JPAQueryFactory를 통한 QueryDSL 쿼리 실행</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ CQRS Query Side 전용 (조회만)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExternalDownloadOutboxQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * Constructor - JPAQueryFactory 주입
     *
     * @param queryFactory JPA queryFactory
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public ExternalDownloadOutboxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Outbox ID로 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT o
     * FROM ExternalDownloadOutboxJpaEntity o
     * WHERE o.id = :outboxId
     * </pre>
     *
     * @param outboxId Outbox ID
     * @return Outbox 메시지 (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<ExternalDownloadOutboxJpaEntity> findById(Long outboxId) {
        if (outboxId == null) {
            return Optional.empty();
        }

        ExternalDownloadOutboxJpaEntity entity = queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.id.eq(outboxId))
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * Download ID로 Outbox 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT o
     * FROM ExternalDownloadOutboxJpaEntity o
     * WHERE o.downloadId = :downloadId
     * ORDER BY o.createdAt DESC
     * LIMIT 1
     * </pre>
     *
     * @param downloadId Download ID
     * @return Outbox 메시지 (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<ExternalDownloadOutboxJpaEntity> findByDownloadId(Long downloadId) {
        if (downloadId == null) {
            return Optional.empty();
        }

        ExternalDownloadOutboxJpaEntity entity = queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.downloadId.eq(downloadId))
            .orderBy(externalDownloadOutboxJpaEntity.createdAt.desc())
            .fetchFirst();

        return Optional.ofNullable(entity);
    }

    /**
     * 멱등성 키로 Outbox 조회
     *
     * <p>중복 이벤트 발행을 방지하기 위해 사용합니다.</p>
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT o
     * FROM ExternalDownloadOutboxJpaEntity o
     * WHERE o.idempotencyKey = :idempotencyKey
     * </pre>
     *
     * @param idempotencyKey 멱등성 키
     * @return Outbox 메시지 (Optional)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<ExternalDownloadOutboxJpaEntity> findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }

        ExternalDownloadOutboxJpaEntity entity = queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.idempotencyKey.eq(idempotencyKey))
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * 상태별 Outbox 메시지 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT o
     * FROM ExternalDownloadOutboxJpaEntity o
     * WHERE o.status = :status
     * ORDER BY o.createdAt ASC
     * LIMIT :limit
     * </pre>
     *
     * @param status 조회할 상태
     * @param limit  최대 조회 개수
     * @return Outbox 메시지 리스트
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<ExternalDownloadOutboxJpaEntity> findByStatus(OutboxStatus status, int limit) {
        if (status == null || limit <= 0) {
            return List.of();
        }

        return queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.status.eq(status))
            .orderBy(externalDownloadOutboxJpaEntity.createdAt.asc())
            .limit(limit)
            .fetch();
    }

    /**
     * 오래된 PROCESSING 메시지 조회
     *
     * <p>장애로 인해 오래 걸린 PROCESSING 상태 메시지를 찾아 복구합니다.</p>
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT o
     * FROM ExternalDownloadOutboxJpaEntity o
     * WHERE o.status = 'PROCESSING'
     *   AND o.createdAt < :threshold
     * ORDER BY o.createdAt ASC
     * LIMIT :limit
     * </pre>
     *
     * @param threshold 임계 시간
     * @param limit     최대 조회 개수
     * @return 오래된 PROCESSING 메시지 리스트
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<ExternalDownloadOutboxJpaEntity> findStaleProcessingMessages(
        LocalDateTime threshold,
        int limit
    ) {
        if (threshold == null || limit <= 0) {
            return List.of();
        }

        return queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(
                externalDownloadOutboxJpaEntity.status.eq(OutboxStatus.PROCESSING),
                externalDownloadOutboxJpaEntity.createdAt.before(threshold)
            )
            .orderBy(externalDownloadOutboxJpaEntity.createdAt.asc())
            .limit(limit)
            .fetch();
    }

    /**
     * 처리 대기 중인 메시지 개수 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT COUNT(o)
     * FROM ExternalDownloadOutboxJpaEntity o
     * WHERE o.status = 'PENDING'
     * </pre>
     *
     * @return PENDING 상태 메시지 개수
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public long countPendingMessages() {
        Long count = queryFactory
            .select(externalDownloadOutboxJpaEntity.count())
            .from(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.status.eq(OutboxStatus.PENDING))
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 실패한 메시지 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT o
     * FROM ExternalDownloadOutboxJpaEntity o
     * WHERE o.status = 'FAILED'
     * ORDER BY o.createdAt ASC
     * LIMIT :limit
     * </pre>
     *
     * @param limit 최대 조회 개수
     * @return 실패한 메시지 리스트
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<ExternalDownloadOutboxJpaEntity> findFailedMessages(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        return queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.status.eq(OutboxStatus.FAILED))
            .orderBy(externalDownloadOutboxJpaEntity.createdAt.asc())
            .limit(limit)
            .fetch();
    }

    /**
     * 재시도 가능한 FAILED 메시지 조회
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT o
     * FROM ExternalDownloadOutboxJpaEntity o
     * WHERE o.status = 'FAILED'
     *   AND o.retryCount < :maxRetryCount
     *   AND o.createdAt < :retryAfter
     * ORDER BY o.createdAt ASC
     * LIMIT :limit
     * </pre>
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param retryAfter    이 시간 이후에 재시도 가능
     * @param limit         최대 조회 개수
     * @return 재시도 가능한 FAILED 메시지 리스트
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<ExternalDownloadOutboxJpaEntity> findRetryableFailedMessages(
        int maxRetryCount,
        LocalDateTime retryAfter,
        int limit
    ) {
        if (maxRetryCount < 0 || retryAfter == null || limit <= 0) {
            return List.of();
        }

        return queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(
                externalDownloadOutboxJpaEntity.status.eq(OutboxStatus.FAILED),
                externalDownloadOutboxJpaEntity.retryCount.lt(maxRetryCount),
                externalDownloadOutboxJpaEntity.createdAt.before(retryAfter)
            )
            .orderBy(externalDownloadOutboxJpaEntity.createdAt.asc())
            .limit(limit)
            .fetch();
    }
}
