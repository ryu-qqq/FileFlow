package com.ryuqq.fileflow.adapter.out.persistence.transform.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformCallbackOutboxJpaEntity;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import com.ryuqq.fileflow.domain.transform.id.TransformCallbackOutboxId;
import org.springframework.stereotype.Component;

@Component
public class TransformCallbackOutboxJpaMapper {

    public TransformCallbackOutboxJpaEntity toEntity(TransformCallbackOutbox domain) {
        return TransformCallbackOutboxJpaEntity.create(
                domain.idValue(),
                domain.transformRequestId(),
                domain.callbackUrl(),
                domain.taskStatus(),
                domain.outboxStatus(),
                domain.retryCount(),
                domain.maxRetries(),
                domain.lastError(),
                domain.createdAt(),
                domain.processedAt());
    }

    public TransformCallbackOutbox toDomain(TransformCallbackOutboxJpaEntity entity) {
        return TransformCallbackOutbox.reconstitute(
                TransformCallbackOutboxId.of(entity.getId()),
                entity.getTransformRequestId(),
                entity.getCallbackUrl(),
                entity.getTaskStatus(),
                entity.getOutboxStatus(),
                entity.getRetryCount(),
                entity.getMaxRetries(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getProcessedAt());
    }
}
