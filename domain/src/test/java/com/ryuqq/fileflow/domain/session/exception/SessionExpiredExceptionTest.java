package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SessionExpiredException 단위 테스트")
class SessionExpiredExceptionTest {

    @Test
    @DisplayName("만료 시간으로 예외를 생성할 수 있다")
    void constructor_WithExpirationTime_ShouldCreateException() {
        // given
        LocalDateTime expirationTime = LocalDateTime.of(2025, 1, 25, 12, 0, 0);

        // when
        SessionExpiredException exception = new SessionExpiredException(expirationTime);

        // then
        assertThat(exception.getMessage())
                .contains("세션이 만료되었습니다")
                .contains("만료 시각: 2025-01-25T12:00");
        assertThat(exception.code()).isEqualTo("SESSION-EXPIRED");
    }

    @Test
    @DisplayName("현재 시간보다 이전 시간으로 예외를 생성할 수 있다")
    void constructor_WithPastTime_ShouldCreateException() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        // when
        SessionExpiredException exception = new SessionExpiredException(pastTime);

        // then
        assertThat(exception.getMessage()).contains("세션이 만료되었습니다");
    }

    @Test
    @DisplayName("DomainException을 상속한다")
    void shouldExtendDomainException() {
        // given
        SessionExpiredException exception = new SessionExpiredException(LocalDateTime.now());

        // when & then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
