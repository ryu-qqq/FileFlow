package com.ryuqq.fileflow.sdk.model.session;

public record CompleteMultipartUploadSessionRequest(long totalFileSize, String etag) {}
