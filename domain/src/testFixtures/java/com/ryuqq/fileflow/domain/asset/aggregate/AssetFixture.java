package com.ryuqq.fileflow.domain.asset.aggregate;

import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.asset.vo.FileInfo;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.common.vo.StorageInfo;
import java.time.Instant;

public class AssetFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant DELETE_TIME = Instant.parse("2026-01-02T00:00:00Z");

    public static Asset anAsset() {
        return Asset.forNew(
                AssetId.of("asset-001"),
                StorageInfo.of("test-bucket", "public/2026/02/test.jpg", AccessType.PUBLIC),
                FileInfo.of("test.jpg", 1024L, "image/jpeg", "etag-123", "jpg"),
                AssetOrigin.SINGLE_UPLOAD,
                "origin-001",
                "product-image",
                "commerce-service",
                DEFAULT_NOW);
    }

    public static Asset anAssetWithId(String assetId) {
        return Asset.forNew(
                AssetId.of(assetId),
                StorageInfo.of("test-bucket", "public/2026/02/test.jpg", AccessType.PUBLIC),
                FileInfo.of("test.jpg", 1024L, "image/jpeg", "etag-123", "jpg"),
                AssetOrigin.SINGLE_UPLOAD,
                "origin-001",
                "product-image",
                "commerce-service",
                DEFAULT_NOW);
    }

    public static Asset aPdfAsset() {
        return Asset.forNew(
                AssetId.of("asset-pdf-001"),
                StorageInfo.of("test-bucket", "internal/2026/02/report.pdf", AccessType.INTERNAL),
                FileInfo.of("report.pdf", 2048L, "application/pdf", "etag-pdf", "pdf"),
                AssetOrigin.EXTERNAL_DOWNLOAD,
                "origin-pdf-001",
                "report",
                "admin-service",
                DEFAULT_NOW);
    }

    public static Asset aMultipartAsset() {
        return Asset.forNew(
                AssetId.of("asset-mp-001"),
                StorageInfo.of("test-bucket", "public/2026/02/large-video.mp4", AccessType.PUBLIC),
                FileInfo.of("large-video.mp4", 104857600L, "video/mp4", "etag-mp", "mp4"),
                AssetOrigin.MULTIPART_UPLOAD,
                "origin-mp-001",
                "product-video",
                "commerce-service",
                DEFAULT_NOW);
    }

    public static Asset aDeletedAsset() {
        Asset asset = anAsset();
        asset.delete(DELETE_TIME);
        return asset;
    }

    public static Asset aReconstitutedAsset() {
        return Asset.reconstitute(
                AssetId.of("asset-recon-001"),
                StorageInfo.of("test-bucket", "public/2026/02/test.jpg", AccessType.PUBLIC),
                FileInfo.of("test.jpg", 1024L, "image/jpeg", "etag-123", "jpg"),
                AssetOrigin.SINGLE_UPLOAD,
                "origin-001",
                "product-image",
                "commerce-service",
                DEFAULT_NOW,
                DEFAULT_NOW,
                null);
    }

    public static Asset aReconstitutedDeletedAsset() {
        return Asset.reconstitute(
                AssetId.of("asset-recon-del-001"),
                StorageInfo.of("test-bucket", "public/2026/02/test.jpg", AccessType.PUBLIC),
                FileInfo.of("test.jpg", 1024L, "image/jpeg", "etag-123", "jpg"),
                AssetOrigin.SINGLE_UPLOAD,
                "origin-001",
                "product-image",
                "commerce-service",
                DEFAULT_NOW,
                DELETE_TIME,
                DELETE_TIME);
    }
}
