package com.ryuqq.fileflow.application.asset;

import com.ryuqq.fileflow.application.asset.dto.response.AssetMetadataResponse;
import com.ryuqq.fileflow.application.asset.dto.response.AssetResponse;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;

/**
 * Asset Application Response 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class AssetResponseFixtures {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private AssetResponseFixtures() {}

    // ===== AssetResponse Fixtures =====

    public static AssetResponse assetResponse() {
        return assetResponse("asset-001");
    }

    public static AssetResponse assetResponse(String assetId) {
        return new AssetResponse(
                assetId,
                "public/2026/01/" + assetId + ".jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                1024L,
                "image/jpeg",
                "etag-123",
                "jpg",
                AssetOrigin.SINGLE_UPLOAD,
                "origin-001",
                "product-image",
                "commerce-service",
                NOW);
    }

    // ===== AssetMetadataResponse Fixtures =====

    public static AssetMetadataResponse assetMetadataResponse() {
        return assetMetadataResponse("meta-001", "asset-001");
    }

    public static AssetMetadataResponse assetMetadataResponse(String metadataId, String assetId) {
        return new AssetMetadataResponse(metadataId, assetId, 1920, 1080, "RESIZE", NOW);
    }
}
