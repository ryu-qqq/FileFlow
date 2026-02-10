package com.ryuqq.fileflow.application.download.assembler;

import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import org.springframework.stereotype.Component;

@Component
public class DownloadAssembler {

    public DownloadTaskResponse toResponse(DownloadTask downloadTask) {
        return new DownloadTaskResponse(
                downloadTask.idValue(),
                downloadTask.sourceUrlValue(),
                downloadTask.s3Key(),
                downloadTask.bucket(),
                downloadTask.accessType(),
                downloadTask.purpose(),
                downloadTask.source(),
                downloadTask.status().name(),
                downloadTask.retryCount(),
                downloadTask.maxRetries(),
                downloadTask.callbackUrl(),
                downloadTask.lastError(),
                downloadTask.createdAt(),
                downloadTask.startedAt(),
                downloadTask.completedAt());
    }
}
