package com.ryuqq.fileflow.sdk.model.asset;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RegisterAssetRequest(
        String s3Key,
        String bucket,
        String accessType,
        String fileName,
        String contentType,
        String purpose,
        String source) {

    public RegisterAssetRequest {
        if (s3Key == null || s3Key.isBlank()) {
            throw new IllegalArgumentException("s3Key must not be null or blank");
        }
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("bucket must not be null or blank");
        }
    }
}
