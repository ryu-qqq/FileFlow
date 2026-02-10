package com.ryuqq.fileflow.adapter.out.persistence.asset;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;

public class AssetJpaEntityFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant DELETE_TIME = Instant.parse("2026-01-02T00:00:00Z");

    public static AssetJpaEntity anAssetEntity() {
        return AssetJpaEntity.create(
                "asset-001",
                "test-bucket",
                "public/2026/02/test.jpg",
                AccessType.PUBLIC,
                "test.jpg",
                1024L,
                "image/jpeg",
                "etag-123",
                "jpg",
                AssetOrigin.SINGLE_UPLOAD,
                "origin-001",
                "product-image",
                "commerce-service",
                DEFAULT_NOW,
                DEFAULT_NOW,
                null);
    }

    public static AssetJpaEntity aDeletedAssetEntity() {
        return AssetJpaEntity.create(
                "asset-del-001",
                "test-bucket",
                "public/2026/02/deleted.jpg",
                AccessType.PUBLIC,
                "deleted.jpg",
                1024L,
                "image/jpeg",
                "etag-del",
                "jpg",
                AssetOrigin.SINGLE_UPLOAD,
                "origin-del-001",
                "product-image",
                "commerce-service",
                DEFAULT_NOW,
                DELETE_TIME,
                DELETE_TIME);
    }

    public static AssetJpaEntity anAssetEntityWithId(String id) {
        return AssetJpaEntity.create(
                id,
                "test-bucket",
                "public/2026/02/" + id + ".jpg",
                AccessType.PUBLIC,
                "test.jpg",
                1024L,
                "image/jpeg",
                "etag-" + id,
                "jpg",
                AssetOrigin.SINGLE_UPLOAD,
                "origin-001",
                "product-image",
                "commerce-service",
                DEFAULT_NOW,
                DEFAULT_NOW,
                null);
    }

    public static Instant defaultNow() {
        return DEFAULT_NOW;
    }
}
