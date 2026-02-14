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
        String source) {}
