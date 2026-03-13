package com.ryuqq.fileflow.adapter.out.persistence.transform.condition;

import static com.ryuqq.fileflow.adapter.out.persistence.transform.entity.QTransformQueueOutboxJpaEntity.transformQueueOutboxJpaEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class TransformQueueOutboxConditionBuilder {

    public BooleanExpression outboxStatusEq(OutboxStatus status) {
        if (status == null) {
            return null;
        }
        return transformQueueOutboxJpaEntity.outboxStatus.eq(status);
    }

    public BooleanExpression outboxStatusIn(OutboxStatus... statuses) {
        if (statuses == null || statuses.length == 0) {
            return null;
        }
        return transformQueueOutboxJpaEntity.outboxStatus.in(statuses);
    }

    public BooleanExpression createdAtGoe(Instant instant) {
        if (instant == null) {
            return null;
        }
        return transformQueueOutboxJpaEntity.createdAt.goe(instant);
    }

    public BooleanExpression createdAtLoe(Instant instant) {
        if (instant == null) {
            return null;
        }
        return transformQueueOutboxJpaEntity.createdAt.loe(instant);
    }
}
