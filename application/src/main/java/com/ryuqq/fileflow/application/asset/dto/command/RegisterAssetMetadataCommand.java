package com.ryuqq.fileflow.application.asset.dto.command;

public record RegisterAssetMetadataCommand(
        String assetId, int width, int height, String transformType) {}
