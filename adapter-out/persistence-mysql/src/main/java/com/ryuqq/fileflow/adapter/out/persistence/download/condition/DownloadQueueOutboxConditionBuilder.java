package com.ryuqq.fileflow.adapter.out.persistence.download.condition;

import static com.ryuqq.fileflow.adapter.out.persistence.download.entity.QDownloadQueueOutboxJpaEntity.downloadQueueOutboxJpaEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class DownloadQueueOutboxConditionBuilder {

    public BooleanExpression outboxStatusEq(OutboxStatus status) {
        if (status == null) {
            return null;
        }
        return downloadQueueOutboxJpaEntity.outboxStatus.eq(status);
    }

    public BooleanExpression outboxStatusIn(OutboxStatus... statuses) {
        if (statuses == null || statuses.length == 0) {
            return null;
        }
        return downloadQueueOutboxJpaEntity.outboxStatus.in(statuses);
    }

    public BooleanExpression createdAtGoe(Instant instant) {
        if (instant == null) {
            return null;
        }
        return downloadQueueOutboxJpaEntity.createdAt.goe(instant);
    }

    public BooleanExpression createdAtLoe(Instant instant) {
        if (instant == null) {
            return null;
        }
        return downloadQueueOutboxJpaEntity.createdAt.loe(instant);
    }
}
