package com.ryuqq.fileflow.adapter.out.persistence.asset;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetMetadataJpaEntity;
import java.time.Instant;

public class AssetMetadataJpaEntityFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static AssetMetadataJpaEntity anImageMetadataEntity() {
        return AssetMetadataJpaEntity.create(
                "meta-img-001", "asset-001", 1920, 1080, null, DEFAULT_NOW, DEFAULT_NOW);
    }

    public static AssetMetadataJpaEntity aTransformedMetadataEntity() {
        return AssetMetadataJpaEntity.create(
                "meta-transformed-001", "asset-002", 800, 600, "RESIZE", DEFAULT_NOW, DEFAULT_NOW);
    }

    public static AssetMetadataJpaEntity aMetadataEntityWithAssetId(String assetId) {
        return AssetMetadataJpaEntity.create(
                "meta-" + assetId, assetId, 1920, 1080, null, DEFAULT_NOW, DEFAULT_NOW);
    }

    public static Instant defaultNow() {
        return DEFAULT_NOW;
    }
}
