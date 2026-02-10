package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SessionErrorCode 단위 테스트")
class SessionErrorCodeTest {

    @Test
    @DisplayName("SessionErrorCode는 ErrorCode 인터페이스를 구현한다")
    void implementsErrorCode() {
        assertThat(SessionErrorCode.SESSION_NOT_FOUND).isInstanceOf(ErrorCode.class);
    }

    @Test
    @DisplayName("SESSION_NOT_FOUND는 코드 SESSION-001, HTTP 404를 가진다")
    void sessionNotFound() {
        assertThat(SessionErrorCode.SESSION_NOT_FOUND.getCode()).isEqualTo("SESSION-001");
        assertThat(SessionErrorCode.SESSION_NOT_FOUND.getHttpStatus()).isEqualTo(404);
        assertThat(SessionErrorCode.SESSION_NOT_FOUND.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("SESSION_ALREADY_COMPLETED는 코드 SESSION-002, HTTP 409를 가진다")
    void sessionAlreadyCompleted() {
        assertThat(SessionErrorCode.SESSION_ALREADY_COMPLETED.getCode()).isEqualTo("SESSION-002");
        assertThat(SessionErrorCode.SESSION_ALREADY_COMPLETED.getHttpStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("SESSION_EXPIRED는 코드 SESSION-003, HTTP 410을 가진다")
    void sessionExpired() {
        assertThat(SessionErrorCode.SESSION_EXPIRED.getCode()).isEqualTo("SESSION-003");
        assertThat(SessionErrorCode.SESSION_EXPIRED.getHttpStatus()).isEqualTo(410);
    }

    @Test
    @DisplayName("SESSION_ALREADY_ABORTED는 코드 SESSION-004, HTTP 409를 가진다")
    void sessionAlreadyAborted() {
        assertThat(SessionErrorCode.SESSION_ALREADY_ABORTED.getCode()).isEqualTo("SESSION-004");
        assertThat(SessionErrorCode.SESSION_ALREADY_ABORTED.getHttpStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("INVALID_SESSION_STATUS는 코드 SESSION-005, HTTP 400을 가진다")
    void invalidSessionStatus() {
        assertThat(SessionErrorCode.INVALID_SESSION_STATUS.getCode()).isEqualTo("SESSION-005");
        assertThat(SessionErrorCode.INVALID_SESSION_STATUS.getHttpStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("PART_NUMBER_DUPLICATE는 코드 SESSION-006, HTTP 409를 가진다")
    void partNumberDuplicate() {
        assertThat(SessionErrorCode.PART_NUMBER_DUPLICATE.getCode()).isEqualTo("SESSION-006");
        assertThat(SessionErrorCode.PART_NUMBER_DUPLICATE.getHttpStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("PART_NUMBER_INVALID는 코드 SESSION-007, HTTP 400을 가진다")
    void partNumberInvalid() {
        assertThat(SessionErrorCode.PART_NUMBER_INVALID.getCode()).isEqualTo("SESSION-007");
        assertThat(SessionErrorCode.PART_NUMBER_INVALID.getHttpStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("SessionErrorCode는 7개의 값을 가진다")
    void hasSevenValues() {
        assertThat(SessionErrorCode.values()).hasSize(7);
    }
}
