package com.ryuqq.fileflow.domain.asset.exception;

import java.util.Map;

public class AssetExceptionFixture {

    public static AssetException anAssetNotFound() {
        return new AssetException(AssetErrorCode.ASSET_NOT_FOUND);
    }

    public static AssetException anAssetAlreadyDeleted() {
        return new AssetException(AssetErrorCode.ASSET_ALREADY_DELETED);
    }

    public static AssetException anAssetNotFoundWithDetail(String assetId) {
        return new AssetException(
                AssetErrorCode.ASSET_NOT_FOUND,
                "Asset not found: " + assetId,
                Map.of("assetId", assetId));
    }
}
