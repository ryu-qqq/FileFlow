package com.ryuqq.fileflow.domain.asset.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/** FileAsset을 찾을 수 없을 때 발생하는 예외. */
public class FileAssetNotFoundException extends DomainException {

    public FileAssetNotFoundException(String fileAssetId) {
        super(
                FileAssetErrorCode.FILE_ASSET_NOT_FOUND.getCode(),
                "FileAsset not found: " + fileAssetId);
    }
}
