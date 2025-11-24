package com.ryuqq.crawlinghub.domain.common.exception;

import com.ryuqq.authhub.domain.common.exception.fixture.DomainExceptionFixture;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DomainException 테스트")
class DomainExceptionTest {

    @Test
    @DisplayName("코드와 메시지로 DomainException 생성 성공")
    void shouldCreateDomainExceptionWithCodeAndMessage() {
        // Given
        String code = "USER-001";
        String message = "User not found";

        // When
        DomainException exception = DomainExceptionFixture.aDomainException(code, message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.code()).isEqualTo(code);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.args()).isEmpty();
    }

    @Test
    @DisplayName("코드, 메시지, 인자로 DomainException 생성 성공")
    void shouldCreateDomainExceptionWithCodeMessageAndArgs() {
        // Given
        String code = "USER-002";
        String message = "User with id {userId} not found";
        Map<String, Object> args = Map.of("userId", 123L);

        // When
        DomainException exception =
                DomainExceptionFixture.aDomainExceptionWithArgs(code, message, args);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.code()).isEqualTo(code);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.args()).containsEntry("userId", 123L);
    }

    @Test
    @DisplayName("null args로 DomainException 생성 시 빈 Map 반환")
    void shouldCreateDomainExceptionWithNullArgs() {
        // Given
        String code = "USER-003";
        String message = "Invalid user";
        Map<String, Object> nullArgs = null;

        // When
        DomainException exception =
                DomainExceptionFixture.aDomainExceptionWithArgs(code, message, nullArgs);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.args()).isEmpty();
    }

    @Test
    @DisplayName("DomainException은 RuntimeException을 상속")
    void shouldExtendRuntimeException() {
        // When
        DomainException exception = DomainExceptionFixture.aDomainException();

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("args Map은 불변")
    void shouldReturnImmutableArgs() {
        // Given
        Map<String, Object> mutableArgs = Map.of("key", "value");

        // When
        DomainException exception =
                DomainExceptionFixture.aDomainExceptionWithArgs("TEST-002", "Test", mutableArgs);

        // Then
        assertThat(exception.args()).isUnmodifiable();
    }
}
