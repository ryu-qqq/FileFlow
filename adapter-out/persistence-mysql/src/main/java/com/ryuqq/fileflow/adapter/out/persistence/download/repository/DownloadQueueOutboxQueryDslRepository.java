package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.download.entity.QDownloadQueueOutboxJpaEntity.downloadQueueOutboxJpaEntity;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.download.condition.DownloadQueueOutboxConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadQueueOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class DownloadQueueOutboxQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final DownloadQueueOutboxConditionBuilder conditionBuilder;
    private final EntityManager entityManager;

    public DownloadQueueOutboxQueryDslRepository(
            JPAQueryFactory queryFactory,
            DownloadQueueOutboxConditionBuilder conditionBuilder,
            EntityManager entityManager) {
        this.queryFactory = queryFactory;
        this.conditionBuilder = conditionBuilder;
        this.entityManager = entityManager;
    }

    public List<DownloadQueueOutboxJpaEntity> findPendingOrderByCreatedAtAsc(int limit) {
        return queryFactory
                .selectFrom(downloadQueueOutboxJpaEntity)
                .where(conditionBuilder.outboxStatusEq(OutboxStatus.PENDING))
                .orderBy(downloadQueueOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    public OutboxStatusCount countGroupByOutboxStatus(Instant startInstant, Instant endInstant) {
        BooleanExpression pendingCondition = conditionBuilder.outboxStatusEq(OutboxStatus.PENDING);

        BooleanExpression sentOrFailedCondition =
                conditionBuilder
                        .outboxStatusIn(OutboxStatus.SENT, OutboxStatus.FAILED)
                        .and(conditionBuilder.createdAtGoe(startInstant))
                        .and(conditionBuilder.createdAtLoe(endInstant));

        List<StatusCountRow> rows =
                queryFactory
                        .select(
                                Projections.constructor(
                                        StatusCountRow.class,
                                        downloadQueueOutboxJpaEntity.outboxStatus,
                                        downloadQueueOutboxJpaEntity.count()))
                        .from(downloadQueueOutboxJpaEntity)
                        .where(pendingCondition.or(sentOrFailedCondition))
                        .groupBy(downloadQueueOutboxJpaEntity.outboxStatus)
                        .fetch();

        return toOutboxStatusCount(rows);
    }

    public int claimPending(int limit, Instant now) {
        return entityManager
                .createNativeQuery(
                        "UPDATE download_queue_outbox SET outbox_status = 'PROCESSING',"
                                + " processed_at = :now WHERE outbox_status = 'PENDING'"
                                + " ORDER BY created_at ASC LIMIT :limit")
                .setParameter("now", now)
                .setParameter("limit", limit)
                .executeUpdate();
    }

    public List<DownloadQueueOutboxJpaEntity> findByStatusWithLock(OutboxStatus status) {
        return queryFactory
                .selectFrom(downloadQueueOutboxJpaEntity)
                .where(conditionBuilder.outboxStatusEq(status))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetch();
    }

    private OutboxStatusCount toOutboxStatusCount(List<StatusCountRow> rows) {
        long pending = 0;
        long sent = 0;
        long failed = 0;
        for (StatusCountRow row : rows) {
            switch (row.status()) {
                case PENDING -> pending = row.count();
                case SENT -> sent = row.count();
                case FAILED -> failed = row.count();
            }
        }
        return new OutboxStatusCount(pending, sent, failed);
    }

    public record StatusCountRow(OutboxStatus status, long count) {}
}
