package com.ryuqq.fileflow.application.transform.dto.command;

public record CreateTransformRequestCommand(
        String sourceAssetId,
        String transformType,
        Integer width,
        Integer height,
        Integer quality,
        String targetFormat) {}
