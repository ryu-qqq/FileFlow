package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/**
 * 지원하지 않는 파일 타입 예외.
 *
 * <p>업로드하려는 파일의 MIME 타입이 허용되지 않은 경우 발생합니다. 허용되는 MIME 타입: image/*, text/html
 *
 * <p><strong>에러 코드</strong>: UNSUPPORTED_FILE_TYPE
 *
 * <p><strong>HTTP 상태</strong>: 400 Bad Request
 */
public class UnsupportedFileTypeException extends DomainException {

    /**
     * UnsupportedFileTypeException 생성자
     *
     * @param mimeType 지원하지 않는 MIME 타입
     */
    public UnsupportedFileTypeException(String mimeType) {
        super(
                SessionErrorCode.UNSUPPORTED_FILE_TYPE.getCode(),
                String.format("지원하지 않는 파일 타입입니다. (요청: %s, 허용: image/*, text/html)", mimeType));
    }
}
