package com.ryuqq.fileflow.application.asset;

import com.ryuqq.fileflow.application.asset.dto.command.DeleteAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetMetadataCommand;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.AccessType;

/**
 * Asset Application Command 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class AssetCommandFixtures {

    private AssetCommandFixtures() {}

    // ===== RegisterAssetCommand Fixtures =====

    public static RegisterAssetCommand registerAssetCommand() {
        return new RegisterAssetCommand(
                "public/2026/01/asset-001.jpg",
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
                "commerce-service");
    }

    public static RegisterAssetCommand registerAssetCommand(String s3Key, String fileName) {
        return new RegisterAssetCommand(
                s3Key,
                "fileflow-bucket",
                AccessType.PUBLIC,
                fileName,
                1024L,
                "image/jpeg",
                "etag-123",
                "jpg",
                AssetOrigin.SINGLE_UPLOAD,
                "origin-001",
                "product-image",
                "commerce-service");
    }

    // ===== DeleteAssetCommand Fixtures =====

    public static DeleteAssetCommand deleteAssetCommand() {
        return deleteAssetCommand("asset-001");
    }

    public static DeleteAssetCommand deleteAssetCommand(String assetId) {
        return new DeleteAssetCommand(assetId, "commerce-service");
    }

    public static DeleteAssetCommand deleteAssetCommand(String assetId, String source) {
        return new DeleteAssetCommand(assetId, source);
    }

    // ===== RegisterAssetMetadataCommand Fixtures =====

    public static RegisterAssetMetadataCommand registerAssetMetadataCommand() {
        return registerAssetMetadataCommand("asset-001");
    }

    public static RegisterAssetMetadataCommand registerAssetMetadataCommand(String assetId) {
        return new RegisterAssetMetadataCommand(assetId, 1920, 1080, "RESIZE");
    }

    public static RegisterAssetMetadataCommand registerAssetMetadataCommand(
            String assetId, int width, int height, String transformType) {
        return new RegisterAssetMetadataCommand(assetId, width, height, transformType);
    }
}
