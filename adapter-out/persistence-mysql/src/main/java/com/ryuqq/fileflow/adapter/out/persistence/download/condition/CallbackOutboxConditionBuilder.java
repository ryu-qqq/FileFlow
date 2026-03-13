package com.ryuqq.fileflow.adapter.out.persistence.download.condition;

import static com.ryuqq.fileflow.adapter.out.persistence.download.entity.QCallbackOutboxJpaEntity.callbackOutboxJpaEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class CallbackOutboxConditionBuilder {

    public BooleanExpression outboxStatusEq(OutboxStatus status) {
        if (status == null) {
            return null;
        }
        return callbackOutboxJpaEntity.outboxStatus.eq(status);
    }

    public BooleanExpression createdAtGoe(Instant instant) {
        if (instant == null) {
            return null;
        }
        return callbackOutboxJpaEntity.createdAt.goe(instant);
    }

    public BooleanExpression createdAtLoe(Instant instant) {
        if (instant == null) {
            return null;
        }
        return callbackOutboxJpaEntity.createdAt.loe(instant);
    }
}
