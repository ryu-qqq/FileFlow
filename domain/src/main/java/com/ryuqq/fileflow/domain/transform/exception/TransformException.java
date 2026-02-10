package com.ryuqq.fileflow.domain.transform.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.util.Map;

public class TransformException extends DomainException {

    public TransformException(TransformErrorCode errorCode) {
        super(errorCode);
    }

    public TransformException(TransformErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public TransformException(
            TransformErrorCode errorCode, String detail, Map<String, Object> args) {
        super(errorCode, detail, args);
    }
}
