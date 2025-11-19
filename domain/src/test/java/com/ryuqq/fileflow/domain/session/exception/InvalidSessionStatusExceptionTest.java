package com.ryuqq.fileflow.domain.session.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvalidSessionStatusException Tests")
class InvalidSessionStatusExceptionTest {

    @Test
    @DisplayName("currentStatus와 requestedStatus로 예외를 생성할 수 있어야 한다")
    void shouldCreateExceptionWithCorrectMessage() {
        // given
        SessionStatus currentStatus = SessionStatus.PREPARING;
        SessionStatus requestedStatus = SessionStatus.COMPLETED;

        // when
        InvalidSessionStatusException exception = new InvalidSessionStatusException(currentStatus, requestedStatus);

        // then
        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getMessage()).contains("세션 상태 전환이 불가능합니다");
        assertThat(exception.getMessage()).contains("현재: PREPARING");
        assertThat(exception.getMessage()).contains("요청: COMPLETED");
        assertThat(exception.errorCode()).isEqualTo(SessionErrorCode.INVALID_SESSION_STATUS);
    }

    @Test
    @DisplayName("HTTP 상태 코드는 409를 반환해야 한다")
    void shouldReturnHttpStatus409() {
        // given
        SessionStatus currentStatus = SessionStatus.ACTIVE;
        SessionStatus requestedStatus = SessionStatus.PREPARING;

        // when
        InvalidSessionStatusException exception = new InvalidSessionStatusException(currentStatus, requestedStatus);

        // then
        assertThat(exception.httpStatus()).isEqualTo(409);
        assertThat(exception.code()).isEqualTo("INVALID-SESSION-STATUS");
    }
}

