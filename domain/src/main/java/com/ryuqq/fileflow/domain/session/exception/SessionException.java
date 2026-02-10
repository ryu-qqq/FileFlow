package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.util.Map;

public class SessionException extends DomainException {

    public SessionException(SessionErrorCode errorCode) {
        super(errorCode);
    }

    public SessionException(SessionErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public SessionException(SessionErrorCode errorCode, String detail, Map<String, Object> args) {
        super(errorCode, detail, args);
    }
}
