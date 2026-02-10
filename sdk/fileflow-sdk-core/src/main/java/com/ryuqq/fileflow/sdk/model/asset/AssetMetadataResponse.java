package com.ryuqq.fileflow.sdk.model.asset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssetMetadataResponse(
        String metadataId,
        String assetId,
        int width,
        int height,
        String transformType,
        String createdAt) {}
