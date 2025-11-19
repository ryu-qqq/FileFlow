package com.ryuqq.fileflow.domain.session.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SessionExpiredException Tests")
class SessionExpiredExceptionTest {

    @Test
    @DisplayName("expiresAt으로 예외를 생성할 수 있어야 한다")
    void shouldCreateExceptionWithCorrectMessage() {
        // given
        LocalDateTime expiresAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        // when
        SessionExpiredException exception = new SessionExpiredException(expiresAt);

        // then
        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getMessage()).contains("세션이 만료되었습니다");
        assertThat(exception.getMessage()).contains("만료 시각: 2024-01-01T12:00:00");
        assertThat(exception.errorCode()).isEqualTo(SessionErrorCode.SESSION_EXPIRED);
    }

    @Test
    @DisplayName("HTTP 상태 코드는 410을 반환해야 한다")
    void shouldReturnHttpStatus410() {
        // given
        LocalDateTime expiresAt = LocalDateTime.now();

        // when
        SessionExpiredException exception = new SessionExpiredException(expiresAt);

        // then
        assertThat(exception.httpStatus()).isEqualTo(410);
        assertThat(exception.code()).isEqualTo("SESSION-EXPIRED");
    }
}

