package com.ryuqq.fileflow.domain.asset.exception;

import java.util.Map;

public class AssetNotFoundException extends AssetException {

    public AssetNotFoundException(String assetId) {
        super(
                AssetErrorCode.ASSET_NOT_FOUND,
                "파일을 찾을 수 없습니다. assetId: " + assetId,
                Map.of("assetId", assetId != null ? assetId : "null"));
    }
}
