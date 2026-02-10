package com.ryuqq.fileflow.sdk.model.asset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssetResponse(
        String assetId,
        String s3Key,
        String bucket,
        String accessType,
        String fileName,
        long fileSize,
        String contentType,
        String etag,
        String extension,
        String origin,
        String originId,
        String purpose,
        String source,
        String createdAt) {}
