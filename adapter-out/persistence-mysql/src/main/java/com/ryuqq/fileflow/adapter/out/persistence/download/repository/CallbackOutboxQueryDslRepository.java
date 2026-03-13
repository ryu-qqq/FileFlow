package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.download.entity.QCallbackOutboxJpaEntity.callbackOutboxJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.download.condition.CallbackOutboxConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.CallbackOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CallbackOutboxQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final CallbackOutboxConditionBuilder conditionBuilder;
    private final EntityManager entityManager;

    public CallbackOutboxQueryDslRepository(
            JPAQueryFactory queryFactory,
            CallbackOutboxConditionBuilder conditionBuilder,
            EntityManager entityManager) {
        this.queryFactory = queryFactory;
        this.conditionBuilder = conditionBuilder;
        this.entityManager = entityManager;
    }

    public List<CallbackOutboxJpaEntity> findPendingOrderByCreatedAtAsc(int limit) {
        return queryFactory
                .selectFrom(callbackOutboxJpaEntity)
                .where(conditionBuilder.outboxStatusEq(OutboxStatus.PENDING))
                .orderBy(callbackOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    public int claimPending(int limit, Instant now) {
        return entityManager
                .createNativeQuery(
                        "UPDATE callback_outbox SET outbox_status = 'PROCESSING',"
                                + " processed_at = :now WHERE outbox_status = 'PENDING'"
                                + " ORDER BY created_at ASC LIMIT :limit")
                .setParameter("now", now)
                .setParameter("limit", limit)
                .executeUpdate();
    }

    public List<CallbackOutboxJpaEntity> findByStatusWithLock(OutboxStatus status) {
        return queryFactory
                .selectFrom(callbackOutboxJpaEntity)
                .where(conditionBuilder.outboxStatusEq(status))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetch();
    }
}
