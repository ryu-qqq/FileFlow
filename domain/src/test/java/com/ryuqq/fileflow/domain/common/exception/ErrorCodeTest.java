package com.ryuqq.crawlinghub.domain.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorCode 인터페이스 테스트")
class ErrorCodeTest {

    /** 테스트용 ErrorCode 구현체 */
    enum TestErrorCode implements ErrorCode {
        INVALID_INPUT("TEST-001", 400, "Invalid input provided"),
        NOT_FOUND("TEST-002", 404, "Resource not found"),
        INTERNAL_ERROR("TEST-003", 500, "Internal server error");

        private final String code;
        private final int httpStatus;
        private final String message;

        TestErrorCode(String code, int httpStatus, String message) {
            this.code = code;
            this.httpStatus = httpStatus;
            this.message = message;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public int getHttpStatus() {
            return httpStatus;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    @Test
    @DisplayName("ErrorCode enum 구현 - INVALID_INPUT")
    void shouldImplementErrorCodeForInvalidInput() {
        // Given
        ErrorCode errorCode = TestErrorCode.INVALID_INPUT;

        // Then
        assertThat(errorCode.getCode()).isEqualTo("TEST-001");
        assertThat(errorCode.getHttpStatus()).isEqualTo(400);
        assertThat(errorCode.getMessage()).isEqualTo("Invalid input provided");
    }

    @Test
    @DisplayName("ErrorCode enum 구현 - NOT_FOUND")
    void shouldImplementErrorCodeForNotFound() {
        // Given
        ErrorCode errorCode = TestErrorCode.NOT_FOUND;

        // Then
        assertThat(errorCode.getCode()).isEqualTo("TEST-002");
        assertThat(errorCode.getHttpStatus()).isEqualTo(404);
        assertThat(errorCode.getMessage()).isEqualTo("Resource not found");
    }

    @Test
    @DisplayName("ErrorCode enum 구현 - INTERNAL_ERROR")
    void shouldImplementErrorCodeForInternalError() {
        // Given
        ErrorCode errorCode = TestErrorCode.INTERNAL_ERROR;

        // Then
        assertThat(errorCode.getCode()).isEqualTo("TEST-003");
        assertThat(errorCode.getHttpStatus()).isEqualTo(500);
        assertThat(errorCode.getMessage()).isEqualTo("Internal server error");
    }

    @Test
    @DisplayName("ErrorCode와 DomainException 통합 테스트")
    void shouldIntegrateErrorCodeWithDomainException() {
        // Given
        ErrorCode errorCode = TestErrorCode.NOT_FOUND;

        // When
        DomainException exception =
                new DomainException(errorCode.getCode(), errorCode.getMessage());

        // Then
        assertThat(exception.code()).isEqualTo("TEST-002");
        assertThat(exception.getMessage()).isEqualTo("Resource not found");
    }
}
