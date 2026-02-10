package com.ryuqq.fileflow.domain.asset.id;

public class AssetMetadataIdFixture {

    public static AssetMetadataId anAssetMetadataId() {
        return AssetMetadataId.of("meta-001");
    }

    public static AssetMetadataId anAssetMetadataId(String value) {
        return AssetMetadataId.of(value);
    }
}
