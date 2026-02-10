package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompletedPartResponse(int partNumber, String etag, long size) {}
