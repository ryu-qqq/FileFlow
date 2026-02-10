package com.ryuqq.fileflow.domain.asset.exception;

import java.util.Map;

public class AssetMetadataNotFoundException extends AssetException {

    public AssetMetadataNotFoundException(String assetId) {
        super(
                AssetErrorCode.ASSET_METADATA_NOT_FOUND,
                "파일 메타데이터를 찾을 수 없습니다. assetId: " + assetId,
                Map.of("assetId", assetId != null ? assetId : "null"));
    }
}
