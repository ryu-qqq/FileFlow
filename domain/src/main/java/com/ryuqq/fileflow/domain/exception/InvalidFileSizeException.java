package com.ryuqq.fileflow.domain.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/**
 * 파일 크기가 유효하지 않을 때 발생하는 예외
 * <p>
 * 파일 크기 검증 실패 시 발생합니다.
 * </p>
 *
 * <p>
 * <strong>발생 시나리오</strong>:
 * <ul>
 *   <li>음수 파일 크기</li>
 *   <li>파일 크기 제한 초과</li>
 *   <li>파일 크기가 0</li>
 * </ul>
 * </p>
 *
 * @author development-team
 * @since 1.0.0
 */
public class InvalidFileSizeException extends DomainException {

    /**
     * ErrorCode 기반 생성자
     *
     * @param errorCode 파일 크기 에러 코드
     */
    public InvalidFileSizeException(InvalidFileSizeErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * ErrorCode + Cause 기반 생성자
     *
     * @param errorCode 파일 크기 에러 코드
     * @param cause 원인 예외
     */
    public InvalidFileSizeException(InvalidFileSizeErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
