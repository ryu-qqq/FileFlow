package com.ryuqq.fileflow.sdk.model.session;

public record AddCompletedPartRequest(int partNumber, String etag, long size) {}
