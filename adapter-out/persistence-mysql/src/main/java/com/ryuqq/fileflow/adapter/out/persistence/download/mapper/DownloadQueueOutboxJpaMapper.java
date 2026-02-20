package com.ryuqq.fileflow.adapter.out.persistence.download.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadQueueOutboxJpaEntity;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import com.ryuqq.fileflow.domain.download.id.DownloadQueueOutboxId;
import org.springframework.stereotype.Component;

@Component
public class DownloadQueueOutboxJpaMapper {

    public DownloadQueueOutboxJpaEntity toEntity(DownloadQueueOutbox domain) {
        return DownloadQueueOutboxJpaEntity.create(
                domain.idValue(),
                domain.downloadTaskId(),
                domain.status(),
                domain.retryCount(),
                domain.lastError(),
                domain.createdAt(),
                domain.processedAt());
    }

    public DownloadQueueOutbox toDomain(DownloadQueueOutboxJpaEntity entity) {
        return DownloadQueueOutbox.reconstitute(
                DownloadQueueOutboxId.of(entity.getId()),
                entity.getDownloadTaskId(),
                entity.getOutboxStatus(),
                entity.getRetryCount(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getProcessedAt());
    }
}
