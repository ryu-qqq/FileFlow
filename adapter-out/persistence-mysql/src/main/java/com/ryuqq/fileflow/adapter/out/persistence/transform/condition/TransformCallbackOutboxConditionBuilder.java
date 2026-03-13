package com.ryuqq.fileflow.adapter.out.persistence.transform.condition;

import static com.ryuqq.fileflow.adapter.out.persistence.transform.entity.QTransformCallbackOutboxJpaEntity.transformCallbackOutboxJpaEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class TransformCallbackOutboxConditionBuilder {

    public BooleanExpression outboxStatusEq(OutboxStatus status) {
        if (status == null) {
            return null;
        }
        return transformCallbackOutboxJpaEntity.outboxStatus.eq(status);
    }

    public BooleanExpression createdAtGoe(Instant instant) {
        if (instant == null) {
            return null;
        }
        return transformCallbackOutboxJpaEntity.createdAt.goe(instant);
    }

    public BooleanExpression createdAtLoe(Instant instant) {
        if (instant == null) {
            return null;
        }
        return transformCallbackOutboxJpaEntity.createdAt.loe(instant);
    }
}
