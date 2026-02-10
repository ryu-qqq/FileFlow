package com.ryuqq.fileflow.domain.asset.id;

public class AssetIdFixture {

    public static AssetId anAssetId() {
        return AssetId.of("asset-001");
    }

    public static AssetId anAssetId(String value) {
        return AssetId.of(value);
    }
}
