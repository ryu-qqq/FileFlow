package com.ryuqq.fileflow.sdk.model.transform;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateTransformRequestRequest(
        String sourceAssetId,
        String transformType,
        Integer width,
        Integer height,
        Integer quality,
        String targetFormat) {}
