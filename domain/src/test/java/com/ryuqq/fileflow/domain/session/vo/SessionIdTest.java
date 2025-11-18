package com.ryuqq.fileflow.domain.session.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SessionId Value Object 테스트
 */
class SessionIdTest {

    @Test
    @DisplayName("UUID v7 형식의 SessionId를 생성해야 한다")
    void shouldGenerateSessionIdWithUUIDv7() {
        // when
        SessionId sessionId = SessionId.generate();

        // then
        assertThat(sessionId).isNotNull();
        assertThat(sessionId.value()).isNotNull();
        assertThat(sessionId.value()).hasSize(36); // UUID 표준 길이
    }

    @Test
    @DisplayName("유효한 UUID 문자열로 SessionId를 생성해야 한다")
    void shouldCreateValidSessionIdFromString() {
        // given
        String validUuid = "01234567-89ab-7def-0123-456789abcdef";

        // when
        SessionId sessionId = SessionId.of(validUuid);

        // then
        assertThat(sessionId).isNotNull();
        assertThat(sessionId.value()).isEqualTo(validUuid);
    }

    @Test
    @DisplayName("null 또는 빈 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNullOrEmpty() {
        // when & then
        assertThatThrownBy(() -> SessionId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SessionId는 null일 수 없습니다 (forNew() 사용)");

        assertThatThrownBy(() -> SessionId.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SessionId는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("유효하지 않은 UUID 형식은 예외가 발생해야 한다")
    void shouldThrowExceptionWhenInvalidUuidFormat() {
        // given
        String invalidUuid = "not-a-valid-uuid";

        // when & then
        assertThatThrownBy(() -> SessionId.of(invalidUuid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 UUID 형식입니다");
    }

    @Test
    @DisplayName("같은 값을 가진 SessionId는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String uuid = "01234567-89ab-7def-0123-456789abcdef";
        SessionId sessionId1 = SessionId.of(uuid);
        SessionId sessionId2 = SessionId.of(uuid);

        // when & then
        assertThat(sessionId1).isEqualTo(sessionId2);
    }

    @Test
    @DisplayName("생성된 SessionId는 매번 다른 값이어야 한다")
    void shouldGenerateDifferentSessionIds() {
        // when
        SessionId sessionId1 = SessionId.generate();
        SessionId sessionId2 = SessionId.generate();

        // then
        assertThat(sessionId1.value()).isNotEqualTo(sessionId2.value());
    }
}
