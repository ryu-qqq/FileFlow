package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PresignedPartUrlResponse(
        String presignedUrl, int partNumber, long expiresInSeconds) {}
