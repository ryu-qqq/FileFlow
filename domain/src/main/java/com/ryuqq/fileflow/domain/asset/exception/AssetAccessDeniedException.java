package com.ryuqq.fileflow.domain.asset.exception;

import java.util.Map;

public class AssetAccessDeniedException extends AssetException {

    public AssetAccessDeniedException(String assetId, String requestSource) {
        super(
                AssetErrorCode.ASSET_ACCESS_DENIED,
                "해당 파일에 대한 권한이 없습니다. assetId: " + assetId + ", requestSource: " + requestSource,
                Map.of(
                        "assetId", assetId != null ? assetId : "null",
                        "requestSource", requestSource != null ? requestSource : "null"));
    }
}
