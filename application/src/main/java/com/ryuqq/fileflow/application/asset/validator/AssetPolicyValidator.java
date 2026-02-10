package com.ryuqq.fileflow.application.asset.validator;

import com.ryuqq.fileflow.application.asset.manager.query.AssetReadManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.exception.AssetAccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AssetPolicyValidator {

    private final AssetReadManager assetReadManager;

    public AssetPolicyValidator(AssetReadManager assetReadManager) {
        this.assetReadManager = assetReadManager;
    }

    public Asset validateCanDelete(String assetId, String requestSource) {
        Asset asset = assetReadManager.getAsset(assetId);

        if (!asset.source().equals(requestSource)) {
            throw new AssetAccessDeniedException(assetId, requestSource);
        }

        return asset;
    }
}
