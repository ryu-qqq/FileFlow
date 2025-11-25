package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.session.vo.ETag;

/**
 * ETag가 일치하지 않는 경우 발생하는 예외.
 *
 * <p>클라이언트가 제공한 ETag와 S3에 실제 업로드된 파일의 ETag가 다른 경우 발생합니다.
 */
public class ETagMismatchException extends DomainException {

    /**
     * ETag 불일치 예외를 생성합니다.
     *
     * @param expectedETag 예상된 ETag (S3에서 반환)
     * @param actualETag 실제 제공된 ETag (클라이언트 제공)
     */
    public ETagMismatchException(ETag expectedETag, ETag actualETag) {
        super(
                SessionErrorCode.ETAG_MISMATCH,
                String.format(
                        "업로드된 파일의 ETag가 일치하지 않습니다. 예상: %s, 실제: %s",
                        expectedETag.value(), actualETag.value()));
    }

    /**
     * ETag 불일치 예외를 생성합니다 (문자열 버전).
     *
     * @param expectedETag 예상된 ETag
     * @param actualETag 실제 제공된 ETag
     */
    public ETagMismatchException(String expectedETag, String actualETag) {
        super(
                SessionErrorCode.ETAG_MISMATCH,
                String.format(
                        "업로드된 파일의 ETag가 일치하지 않습니다. 예상: %s, 실제: %s", expectedETag, actualETag));
    }
}
