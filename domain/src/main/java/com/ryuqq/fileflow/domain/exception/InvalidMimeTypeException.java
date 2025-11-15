package com.ryuqq.fileflow.domain.exception;

import com.ryuqq.fileflow.domain.common.DomainException;

/**
 * MIME 타입이 유효하지 않을 때 발생하는 예외
 */
public class InvalidMimeTypeException extends DomainException {

    private static final String CODE = "INVALID_MIME_TYPE";

    public InvalidMimeTypeException(String message) {
        super(CODE, message);
    }
}
