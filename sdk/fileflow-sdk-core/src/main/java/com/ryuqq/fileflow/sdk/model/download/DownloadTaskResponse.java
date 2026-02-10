package com.ryuqq.fileflow.sdk.model.download;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DownloadTaskResponse(
        String downloadTaskId,
        String sourceUrl,
        String s3Key,
        String bucket,
        String accessType,
        String purpose,
        String source,
        String status,
        int retryCount,
        int maxRetries,
        String callbackUrl,
        String lastError,
        String createdAt,
        String startedAt,
        String completedAt) {}
