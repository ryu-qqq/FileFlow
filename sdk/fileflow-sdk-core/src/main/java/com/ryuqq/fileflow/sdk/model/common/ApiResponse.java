package com.ryuqq.fileflow.sdk.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiResponse<T>(T data, String timestamp, String requestId) {}
