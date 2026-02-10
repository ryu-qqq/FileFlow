package com.ryuqq.fileflow.domain.asset.aggregate;

import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.asset.id.AssetMetadataId;
import java.time.Instant;

public class AssetMetadataFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static AssetMetadata anImageMetadata() {
        return AssetMetadata.forNew(
                AssetMetadataId.of("meta-img-001"),
                AssetId.of("asset-001"),
                1920,
                1080,
                null,
                DEFAULT_NOW);
    }

    public static AssetMetadata aTransformedImageMetadata() {
        return AssetMetadata.forNew(
                AssetMetadataId.of("meta-transformed-001"),
                AssetId.of("asset-002"),
                800,
                600,
                "RESIZE",
                DEFAULT_NOW);
    }

    public static AssetMetadata aThumbnailMetadata() {
        return AssetMetadata.forNew(
                AssetMetadataId.of("meta-thumb-001"),
                AssetId.of("asset-003"),
                200,
                200,
                "THUMBNAIL",
                DEFAULT_NOW);
    }

    public static AssetMetadata aReconstitutedImageMetadata() {
        return AssetMetadata.reconstitute(
                AssetMetadataId.of("meta-recon-001"),
                AssetId.of("asset-001"),
                1920,
                1080,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW);
    }
}
