package com.ryuqq.fileflow.sdk.model.transform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
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
        String createdAt,
        String completedAt) {}
