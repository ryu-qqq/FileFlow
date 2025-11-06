package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.adapter;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.QExternalDownloadOutboxJpaEntity.externalDownloadOutboxJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.mapper.ExternalDownloadOutboxEntityMapper;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.common.OutboxStatus;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * External Download Outbox Query Adapter (CQRS - Query Side)
 *
 * <p>Application Layer의 {@link ExternalDownloadOutboxQueryPort}를 구현하는 Query Adapter입니다.</p>
 * <p>QueryDSL을 사용하여 복잡한 조회 쿼리를 타입 안전하게 처리합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>ExternalDownloadOutbox의 조회 (Query 전용)</li>
 *   <li>QueryDSL을 통한 복잡한 검색 조건 처리</li>
 *   <li>읽기 최적화 (페이징, 정렬, 필터링)</li>
 * </ul>
 *
 * <p><strong>CQRS 설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Query (읽기) 전용 - 쓰기 메서드 없음</li>
 *   <li>✅ QueryDSL 사용 - 타입 안전한 쿼리</li>
 *   <li>✅ @Transactional(readOnly = true) - 읽기 최적화</li>
 *   <li>✅ 복잡한 조회 조건 지원 - 동적 쿼리</li>
 *   <li>❌ 쓰기 메서드 금지 - Command Adapter로 분리</li>
 * </ul>
 *
 * <p><strong>QueryDSL 장점:</strong></p>
 * <ul>
 *   <li>컴파일 타임 타입 체크</li>
 *   <li>IDE 자동 완성 지원</li>
 *   <li>리팩토링 안전성</li>
 *   <li>동적 쿼리 작성 용이</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see ExternalDownloadOutboxCommandAdapter
 */
@Component
@Transactional(readOnly = true)
public class ExternalDownloadOutboxQueryAdapter implements ExternalDownloadOutboxQueryPort {

    private final JPAQueryFactory queryFactory;

    /**
     * 생성자
     *
     * @param queryFactory JPAQueryFactory (QueryDSL)
     */
    public ExternalDownloadOutboxQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Outbox ID로 조회 (QueryDSL)
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
     */
    @Override
    public Optional<ExternalDownloadOutbox> findById(Long outboxId) {
        if (outboxId == null) {
            return Optional.empty();
        }

        ExternalDownloadOutboxJpaEntity entity = queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.id.eq(outboxId))
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(ExternalDownloadOutboxEntityMapper::toDomain);
    }

    /**
     * Download ID로 Outbox 조회 (QueryDSL)
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
     */
    @Override
    public Optional<ExternalDownloadOutbox> findByDownloadId(Long downloadId) {
        if (downloadId == null) {
            return Optional.empty();
        }

        ExternalDownloadOutboxJpaEntity entity = queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.downloadId.eq(downloadId))
            .orderBy(externalDownloadOutboxJpaEntity.createdAt.desc())
            .fetchFirst();

        return Optional.ofNullable(entity)
            .map(ExternalDownloadOutboxEntityMapper::toDomain);
    }

    /**
     * 멱등성 키로 Outbox 조회 (QueryDSL)
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
     */
    @Override
    public Optional<ExternalDownloadOutbox> findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }

        ExternalDownloadOutboxJpaEntity entity = queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.idempotencyKey.eq(idempotencyKey))
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(ExternalDownloadOutboxEntityMapper::toDomain);
    }

    /**
     * 상태별 Outbox 메시지 조회 (QueryDSL)
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
     */
    @Override
    public List<ExternalDownloadOutbox> findByStatus(OutboxStatus status, int limit) {
        if (status == null || limit <= 0) {
            return List.of();
        }

        List<ExternalDownloadOutboxJpaEntity> entities = queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.status.eq(status))
            .orderBy(externalDownloadOutboxJpaEntity.createdAt.asc())
            .limit(limit)
            .fetch();

        return entities.stream()
            .map(ExternalDownloadOutboxEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 오래된 PROCESSING 메시지 조회 (QueryDSL)
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
     */
    @Override
    public List<ExternalDownloadOutbox> findStaleProcessingMessages(
        LocalDateTime threshold,
        int limit
    ) {
        if (threshold == null || limit <= 0) {
            return List.of();
        }

        List<ExternalDownloadOutboxJpaEntity> entities = queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(
                externalDownloadOutboxJpaEntity.status.eq(OutboxStatus.PROCESSING),
                externalDownloadOutboxJpaEntity.createdAt.before(threshold)
            )
            .orderBy(externalDownloadOutboxJpaEntity.createdAt.asc())
            .limit(limit)
            .fetch();

        return entities.stream()
            .map(ExternalDownloadOutboxEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 처리 대기 중인 메시지 개수 조회 (QueryDSL)
     *
     * <p><strong>QueryDSL 쿼리:</strong></p>
     * <pre>
     * SELECT COUNT(o)
     * FROM ExternalDownloadOutboxJpaEntity o
     * WHERE o.status = 'PENDING'
     * </pre>
     *
     * @return PENDING 상태 메시지 개수
     */
    @Override
    public long countPendingMessages() {
        Long count = queryFactory
            .select(externalDownloadOutboxJpaEntity.count())
            .from(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.status.eq(OutboxStatus.PENDING))
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 실패한 메시지 조회 (QueryDSL)
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
     */
    @Override
    public List<ExternalDownloadOutbox> findFailedMessages(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        List<ExternalDownloadOutboxJpaEntity> entities = queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.status.eq(OutboxStatus.FAILED))
            .orderBy(externalDownloadOutboxJpaEntity.createdAt.asc())
            .limit(limit)
            .fetch();

        return entities.stream()
            .map(ExternalDownloadOutboxEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 재시도 가능한 FAILED 메시지 조회 (QueryDSL)
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
     */
    @Override
    public List<ExternalDownloadOutbox> findRetryableFailedMessages(
        int maxRetryCount,
        LocalDateTime retryAfter,
        int limit
    ) {
        if (maxRetryCount < 0 || retryAfter == null || limit <= 0) {
            return List.of();
        }

        List<ExternalDownloadOutboxJpaEntity> entities = queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(
                externalDownloadOutboxJpaEntity.status.eq(OutboxStatus.FAILED),
                externalDownloadOutboxJpaEntity.retryCount.lt(maxRetryCount),
                externalDownloadOutboxJpaEntity.createdAt.before(retryAfter)
            )
            .orderBy(externalDownloadOutboxJpaEntity.createdAt.asc())
            .limit(limit)
            .fetch();

        return entities.stream()
            .map(ExternalDownloadOutboxEntityMapper::toDomain)
            .collect(Collectors.toList());
    }
}
