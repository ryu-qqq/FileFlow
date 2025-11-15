package com.ryuqq.fileflow.domain.exception;

import com.ryuqq.fileflow.domain.common.DomainException;

/**
 * 파일 크기가 유효하지 않을 때 발생하는 예외
 */
public class InvalidFileSizeException extends DomainException {

    private static final String CODE = "INVALID_FILE_SIZE";

    public InvalidFileSizeException(String message) {
        super(CODE, message);
    }
}
