package com.ryuqq.fileflow.application.asset.dto.response;

import java.time.Instant;

public record AssetMetadataResponse(
        String metadataId,
        String assetId,
        int width,
        int height,
        String transformType,
        Instant createdAt) {}
