package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SingleUploadSessionResponse(
        String sessionId,
        String presignedUrl,
        String s3Key,
        String bucket,
        String accessType,
        String fileName,
        String contentType,
        String status,
        String expiresAt,
        String createdAt) {}
