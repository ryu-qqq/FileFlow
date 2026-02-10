package com.ryuqq.fileflow.sdk.model.session;

public record CreateSingleUploadSessionRequest(
        String fileName, String contentType, String accessType, String purpose, String source) {}
