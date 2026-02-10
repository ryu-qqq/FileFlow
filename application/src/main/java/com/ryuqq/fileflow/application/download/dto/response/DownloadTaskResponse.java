package com.ryuqq.fileflow.application.download.dto.response;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;

public record DownloadTaskResponse(
        String downloadTaskId,
        String sourceUrl,
        String s3Key,
        String bucket,
        AccessType accessType,
        String purpose,
        String source,
        String status,
        int retryCount,
        int maxRetries,
        String callbackUrl,
        String lastError,
        Instant createdAt,
        Instant startedAt,
        Instant completedAt) {}
