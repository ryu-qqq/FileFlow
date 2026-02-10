package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MultipartUploadSessionResponse(
        String sessionId,
        String uploadId,
        String s3Key,
        String bucket,
        String accessType,
        String fileName,
        String contentType,
        long partSize,
        String status,
        int completedPartCount,
        List<CompletedPartResponse> completedParts,
        String expiresAt,
        String createdAt) {}
