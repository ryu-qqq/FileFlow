package com.ryuqq.fileflow.sdk.model.download;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateDownloadTaskRequest(
        String sourceUrl, String accessType, String purpose, String source, String callbackUrl) {}
