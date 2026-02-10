package com.ryuqq.fileflow.adapter.out.persistence.download.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.CallbackOutboxJpaEntity;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.id.CallbackOutboxId;
import org.springframework.stereotype.Component;

@Component
public class CallbackOutboxJpaMapper {

    public CallbackOutboxJpaEntity toEntity(CallbackOutbox domain) {
        return CallbackOutboxJpaEntity.create(
                domain.idValue(),
                domain.downloadTaskId(),
                domain.callbackUrl(),
                domain.taskStatus(),
                domain.outboxStatus(),
                domain.retryCount(),
                domain.lastError(),
                domain.createdAt(),
                domain.processedAt());
    }

    public CallbackOutbox toDomain(CallbackOutboxJpaEntity entity) {
        return CallbackOutbox.reconstitute(
                CallbackOutboxId.of(entity.getId()),
                entity.getDownloadTaskId(),
                entity.getCallbackUrl(),
                entity.getTaskStatus(),
                entity.getOutboxStatus(),
                entity.getRetryCount(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getProcessedAt());
    }
}
