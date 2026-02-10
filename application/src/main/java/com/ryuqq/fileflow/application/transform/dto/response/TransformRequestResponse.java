package com.ryuqq.fileflow.application.transform.dto.response;

import java.time.Instant;

public record TransformRequestResponse(
        String transformRequestId,
        String sourceAssetId,
        String sourceContentType,
        String transformType,
        Integer width,
        Integer height,
        Integer quality,
        String targetFormat,
        String status,
        String resultAssetId,
        String lastError,
        Instant createdAt,
        Instant completedAt) {}
