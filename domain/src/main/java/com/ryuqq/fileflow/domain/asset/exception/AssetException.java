package com.ryuqq.fileflow.domain.asset.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

public class AssetException extends DomainException {

    public AssetException(AssetErrorCode errorCode) {
        super(errorCode);
    }

    public AssetException(AssetErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
