package com.ryuqq.fileflow.domain.asset.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/** FileAsset을 찾을 수 없을 때 발생하는 예외. */
public class AssetNotFoundException extends DomainException {

    public AssetNotFoundException(String assetId) {
        super(AssetErrorCode.ASSET_NOT_FOUND.getCode(), "Asset ID: " + assetId);
    }
}
