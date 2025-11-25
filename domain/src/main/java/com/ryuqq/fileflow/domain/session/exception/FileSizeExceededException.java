package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/**
 * 파일 크기 초과 예외.
 *
 * <p>업로드하려는 파일의 크기가 허용된 최대 크기를 초과한 경우 발생합니다.
 *
 * <p><strong>에러 코드</strong>: FILE_SIZE_EXCEEDED
 *
 * <p><strong>HTTP 상태</strong>: 400 Bad Request
 */
public class FileSizeExceededException extends DomainException {

    /**
     * FileSizeExceededException 생성자
     *
     * @param actualSize 실제 파일 크기 (바이트)
     * @param maxSize 최대 허용 크기 (바이트)
     */
    public FileSizeExceededException(long actualSize, long maxSize) {
        super(
                SessionErrorCode.FILE_SIZE_EXCEEDED.getCode(),
                String.format("파일 크기가 최대 허용 크기를 초과했습니다. (실제: %d, 최대: %d)", actualSize, maxSize));
    }
}
