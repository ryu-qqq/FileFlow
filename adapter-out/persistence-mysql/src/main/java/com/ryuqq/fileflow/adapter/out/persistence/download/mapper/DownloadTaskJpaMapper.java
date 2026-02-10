package com.ryuqq.fileflow.adapter.out.persistence.download.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.StorageInfo;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.CallbackInfo;
import com.ryuqq.fileflow.domain.download.vo.RetryPolicy;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import org.springframework.stereotype.Component;

@Component
public class DownloadTaskJpaMapper {

    public DownloadTaskJpaEntity toEntity(DownloadTask domain) {
        return DownloadTaskJpaEntity.create(
                domain.idValue(),
                domain.sourceUrlValue(),
                domain.bucket(),
                domain.s3Key(),
                domain.accessType(),
                domain.purpose(),
                domain.source(),
                domain.status(),
                domain.retryCount(),
                domain.maxRetries(),
                domain.callbackUrl(),
                domain.lastError(),
                domain.createdAt(),
                domain.updatedAt(),
                domain.startedAt(),
                domain.completedAt());
    }

    public DownloadTask toDomain(DownloadTaskJpaEntity entity) {
        return DownloadTask.reconstitute(
                DownloadTaskId.of(entity.getId()),
                SourceUrl.of(entity.getSourceUrl()),
                StorageInfo.of(entity.getBucket(), entity.getS3Key(), entity.getAccessType()),
                entity.getPurpose(),
                entity.getSource(),
                entity.getStatus(),
                RetryPolicy.of(entity.getRetryCount(), entity.getMaxRetries()),
                CallbackInfo.of(entity.getCallbackUrl()),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getStartedAt(),
                entity.getCompletedAt());
    }
}
