package com.ryuqq.fileflow.adapter.out.persistence.transform.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformQueueOutboxJpaEntity;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import com.ryuqq.fileflow.domain.transform.id.TransformQueueOutboxId;
import org.springframework.stereotype.Component;

@Component
public class TransformQueueOutboxJpaMapper {

    public TransformQueueOutboxJpaEntity toEntity(TransformQueueOutbox domain) {
        return TransformQueueOutboxJpaEntity.create(
                domain.idValue(),
                domain.transformRequestId(),
                domain.status(),
                domain.retryCount(),
                domain.lastError(),
                domain.createdAt(),
                domain.processedAt());
    }

    public TransformQueueOutbox toDomain(TransformQueueOutboxJpaEntity entity) {
        return TransformQueueOutbox.reconstitute(
                TransformQueueOutboxId.of(entity.getId()),
                entity.getTransformRequestId(),
                entity.getOutboxStatus(),
                entity.getRetryCount(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getProcessedAt());
    }
}
