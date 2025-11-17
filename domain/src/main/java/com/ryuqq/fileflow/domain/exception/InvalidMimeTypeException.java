package com.ryuqq.fileflow.domain.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/**
 * MIME 타입이 유효하지 않을 때 발생하는 예외
 * <p>
 * MIME 타입 검증 실패 시 발생합니다.
 * </p>
 *
 * <p>
 * <strong>발생 시나리오</strong>:
 * <ul>
 *   <li>지원하지 않는 MIME 타입</li>
 *   <li>비어있는 MIME 타입</li>
 *   <li>잘못된 MIME 타입 형식</li>
 * </ul>
 * </p>
 *
 * @author development-team
 * @since 1.0.0
 */
public class InvalidMimeTypeException extends DomainException {

    /**
     * ErrorCode 기반 생성자
     *
     * @param errorCode MIME 타입 에러 코드
     */
    public InvalidMimeTypeException(InvalidMimeTypeErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * ErrorCode + Cause 기반 생성자
     *
     * @param errorCode MIME 타입 에러 코드
     * @param cause 원인 예외
     */
    public InvalidMimeTypeException(InvalidMimeTypeErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
