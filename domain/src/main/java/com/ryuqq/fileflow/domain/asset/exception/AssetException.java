package com.ryuqq.fileflow.domain.asset.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.util.Map;

public class AssetException extends DomainException {

    public AssetException(AssetErrorCode errorCode) {
        super(errorCode);
    }

    public AssetException(AssetErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public AssetException(AssetErrorCode errorCode, String detail, Map<String, Object> args) {
        super(errorCode, detail, args);
    }
}
