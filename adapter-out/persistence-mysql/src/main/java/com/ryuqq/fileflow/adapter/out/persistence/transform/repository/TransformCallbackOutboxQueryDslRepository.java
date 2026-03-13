package com.ryuqq.fileflow.adapter.out.persistence.transform.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.transform.entity.QTransformCallbackOutboxJpaEntity.transformCallbackOutboxJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.transform.condition.TransformCallbackOutboxConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformCallbackOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TransformCallbackOutboxQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final TransformCallbackOutboxConditionBuilder conditionBuilder;
    private final EntityManager entityManager;

    public TransformCallbackOutboxQueryDslRepository(
            JPAQueryFactory queryFactory,
            TransformCallbackOutboxConditionBuilder conditionBuilder,
            EntityManager entityManager) {
        this.queryFactory = queryFactory;
        this.conditionBuilder = conditionBuilder;
        this.entityManager = entityManager;
    }

    public List<TransformCallbackOutboxJpaEntity> findPendingOrderByCreatedAtAsc(int limit) {
        return queryFactory
                .selectFrom(transformCallbackOutboxJpaEntity)
                .where(conditionBuilder.outboxStatusEq(OutboxStatus.PENDING))
                .orderBy(transformCallbackOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    public int claimPending(int limit, Instant now) {
        return entityManager
                .createNativeQuery(
                        "UPDATE transform_callback_outbox SET outbox_status = 'PROCESSING',"
                                + " processed_at = :now WHERE outbox_status = 'PENDING'"
                                + " ORDER BY created_at ASC LIMIT :limit")
                .setParameter("now", now)
                .setParameter("limit", limit)
                .executeUpdate();
    }

    public List<TransformCallbackOutboxJpaEntity> findByStatusWithLock(OutboxStatus status) {
        return queryFactory
                .selectFrom(transformCallbackOutboxJpaEntity)
                .where(conditionBuilder.outboxStatusEq(status))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetch();
    }
}
