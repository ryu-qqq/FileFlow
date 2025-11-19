package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/**
 * 파일 크기 초과 예외.
 */
public class FileSizeExceededException extends DomainException {

    public FileSizeExceededException(long actualSize, long maxSize) {
        super(
            SessionErrorCode.FILE_SIZE_EXCEEDED,
            String.format(
                "파일 크기가 최대 허용 크기를 초과했습니다. (실제: %d, 최대: %d)",
                actualSize,
                maxSize
            )
        );
    }
}

