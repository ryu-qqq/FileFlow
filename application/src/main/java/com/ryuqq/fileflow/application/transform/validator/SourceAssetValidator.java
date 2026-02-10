package com.ryuqq.fileflow.application.transform.validator;

import com.ryuqq.fileflow.application.asset.manager.query.AssetReadManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import org.springframework.stereotype.Component;

@Component
public class SourceAssetValidator {

    private final AssetReadManager assetReadManager;

    public SourceAssetValidator(AssetReadManager assetReadManager) {
        this.assetReadManager = assetReadManager;
    }

    /** 소스 에셋 존재 여부를 검증하고 contentType을 반환합니다. 없으면 AssetNotFoundException을 던집니다. */
    public String validateAndGetContentType(String sourceAssetId) {
        Asset asset = assetReadManager.getAsset(sourceAssetId);
        return asset.contentType();
    }
}
