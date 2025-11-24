package com.ryuqq.cralwinghub.domain.fixture;


import com.ryuqq.crawlinghub.domain.common.exception.DomainException;

import java.util.Map;

/** DomainException Test Fixture Object Mother 패턴을 사용한 테스트 데이터 생성 */
public final class DomainExceptionFixture {

    private static final String DEFAULT_ERROR_CODE = "TEST-001";
    private static final String DEFAULT_ERROR_MESSAGE = "Test exception occurred";

    /**
     * 기본 DomainException 생성
     *
     * @return DomainException 인스턴스
     */
    public static DomainException aDomainException() {
        return new DomainException(DEFAULT_ERROR_CODE, DEFAULT_ERROR_MESSAGE);
    }

    /**
     * 특정 코드와 메시지로 DomainException 생성
     *
     * @param code 에러 코드
     * @param message 에러 메시지
     * @return DomainException 인스턴스
     */
    public static DomainException aDomainException(String code, String message) {
        return new DomainException(code, message);
    }

    /**
     * 인자를 포함한 DomainException 생성
     *
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param args 에러 인자
     * @return DomainException 인스턴스
     */
    public static DomainException aDomainExceptionWithArgs(
            String code, String message, Map<String, Object> args) {
        return new DomainException(code, message, args);
    }

    /**
     * NOT_FOUND 에러 DomainException 생성
     *
     * @return DomainException 인스턴스
     */
    public static DomainException aNotFoundException() {
        return new DomainException("NOT_FOUND", "Resource not found");
    }

    /**
     * INVALID_INPUT 에러 DomainException 생성
     *
     * @return DomainException 인스턴스
     */
    public static DomainException anInvalidInputException() {
        return new DomainException("INVALID_INPUT", "Invalid input provided");
    }

    /**
     * USER 도메인 에러 DomainException 생성
     *
     * @param userId 사용자 ID
     * @return DomainException 인스턴스
     */
    public static DomainException aUserNotFoundException(Long userId) {
        return new DomainException("USER-001", "User not found", Map.of("userId", userId));
    }

    private DomainExceptionFixture() {
        // Utility class
    }
}
