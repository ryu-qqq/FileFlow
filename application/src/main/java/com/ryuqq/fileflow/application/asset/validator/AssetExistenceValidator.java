package com.ryuqq.fileflow.application.asset.validator;

import com.ryuqq.fileflow.application.asset.manager.query.AssetReadManager;
import org.springframework.stereotype.Component;

@Component
public class AssetExistenceValidator {

    private final AssetReadManager assetReadManager;

    public AssetExistenceValidator(AssetReadManager assetReadManager) {
        this.assetReadManager = assetReadManager;
    }

    /** Asset 존재 여부를 검증합니다. 없으면 AssetNotFoundException을 던집니다. */
    public void validateExists(String assetId) {
        assetReadManager.getAsset(assetId);
    }
}
