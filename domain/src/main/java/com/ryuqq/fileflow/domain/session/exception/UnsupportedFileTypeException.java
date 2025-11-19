package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/**
 * 지원하지 않는 파일 타입 예외.
 */
public class UnsupportedFileTypeException extends DomainException {

    public UnsupportedFileTypeException(String mimeType) {
        super(
            SessionErrorCode.UNSUPPORTED_FILE_TYPE,
            String.format(
                "지원하지 않는 파일 타입입니다. (요청: %s, 허용: image/*, text/html)",
                mimeType
            )
        );
    }
}

