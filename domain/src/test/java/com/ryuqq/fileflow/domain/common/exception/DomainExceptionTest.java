package com.ryuqq.fileflow.domain.common.exception;

import com.ryuqq.fileflow.domain.exception.InvalidFileSizeErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DomainException 테스트
 */
@DisplayName("DomainException Tests")
class DomainExceptionTest {

    @Test
    @DisplayName("ErrorCode 기반으로 예외를 생성할 수 있어야 한다")
    void shouldCreateExceptionWithErrorCode() {
        // given
        ErrorCode errorCode = InvalidFileSizeErrorCode.NEGATIVE_FILE_SIZE;

        // when
        DomainException exception = new DomainException(errorCode);

        // then
        assertThat(exception.errorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
        assertThat(exception.code()).isEqualTo("SIZE-001");
        assertThat(exception.httpStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("ErrorCode와 Cause 기반으로 예외를 생성할 수 있어야 한다")
    void shouldCreateExceptionWithErrorCodeAndCause() {
        // given
        ErrorCode errorCode = InvalidFileSizeErrorCode.FILE_SIZE_LIMIT_EXCEEDED;
        Throwable cause = new IllegalArgumentException("원인 예외");

        // when
        DomainException exception = new DomainException(errorCode, cause);

        // then
        assertThat(exception.errorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.code()).isEqualTo("SIZE-002");
        assertThat(exception.httpStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("code() 메서드는 ErrorCode의 코드를 반환해야 한다")
    void shouldReturnCodeFromErrorCode() {
        // given
        ErrorCode errorCode = InvalidFileSizeErrorCode.EMPTY_FILE_SIZE;
        DomainException exception = new DomainException(errorCode);

        // when
        String code = exception.code();

        // then
        assertThat(code).isEqualTo("SIZE-003");
    }

    @Test
    @DisplayName("httpStatus() 메서드는 ErrorCode의 HTTP 상태 코드를 반환해야 한다")
    void shouldReturnHttpStatusFromErrorCode() {
        // given
        ErrorCode errorCode = InvalidFileSizeErrorCode.NEGATIVE_FILE_SIZE;
        DomainException exception = new DomainException(errorCode);

        // when
        int httpStatus = exception.httpStatus();

        // then
        assertThat(httpStatus).isEqualTo(400);
    }

    @Test
    @DisplayName("errorCode() 메서드는 생성 시 전달받은 ErrorCode를 반환해야 한다")
    void shouldReturnOriginalErrorCode() {
        // given
        ErrorCode errorCode = InvalidFileSizeErrorCode.FILE_SIZE_LIMIT_EXCEEDED;
        DomainException exception = new DomainException(errorCode);

        // when
        ErrorCode returnedErrorCode = exception.errorCode();

        // then
        assertThat(returnedErrorCode).isEqualTo(errorCode);
        assertThat(returnedErrorCode.getCode()).isEqualTo("SIZE-002");
        assertThat(returnedErrorCode.getMessage()).isEqualTo("파일 크기 제한을 초과했습니다");
        assertThat(returnedErrorCode.getHttpStatus()).isEqualTo(400);
    }
}
