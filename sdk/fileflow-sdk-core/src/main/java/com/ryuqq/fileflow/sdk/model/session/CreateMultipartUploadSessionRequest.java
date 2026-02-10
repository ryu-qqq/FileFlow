package com.ryuqq.fileflow.sdk.model.session;

public record CreateMultipartUploadSessionRequest(
        String fileName,
        String contentType,
        String accessType,
        long partSize,
        String purpose,
        String source) {}
