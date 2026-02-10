package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SessionExpiration Value Object 단위 테스트")
class SessionExpirationTest {

    @Nested
    @DisplayName("of - 생성")
    class Of {

        @Test
        @DisplayName("유효한 값으로 SessionExpiration을 생성할 수 있다")
        void createsWithValidValues() {
            SessionExpiration expiration =
                    SessionExpiration.of("session-001", "SINGLE", Duration.ofHours(1));

            assertThat(expiration.sessionId()).isEqualTo("session-001");
            assertThat(expiration.sessionType()).isEqualTo("SINGLE");
            assertThat(expiration.ttl()).isEqualTo(Duration.ofHours(1));
        }

        @Test
        @DisplayName("MULTIPART 타입으로 생성할 수 있다")
        void createsWithMultipartType() {
            SessionExpiration expiration =
                    SessionExpiration.of("multipart-001", "MULTIPART", Duration.ofMinutes(30));

            assertThat(expiration.sessionType()).isEqualTo("MULTIPART");
            assertThat(expiration.ttl()).isEqualTo(Duration.ofMinutes(30));
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class Validation {

        @Test
        @DisplayName("sessionId가 null이면 NullPointerException이 발생한다")
        void throwsWhenSessionIdIsNull() {
            assertThatThrownBy(() -> SessionExpiration.of(null, "SINGLE", Duration.ofHours(1)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("sessionId must not be null");
        }

        @Test
        @DisplayName("sessionType이 null이면 NullPointerException이 발생한다")
        void throwsWhenSessionTypeIsNull() {
            assertThatThrownBy(() -> SessionExpiration.of("session-001", null, Duration.ofHours(1)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("sessionType must not be null");
        }

        @Test
        @DisplayName("ttl이 null이면 NullPointerException이 발생한다")
        void throwsWhenTtlIsNull() {
            assertThatThrownBy(() -> SessionExpiration.of("session-001", "SINGLE", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("ttl must not be null");
        }

        @Test
        @DisplayName("ttl이 0이면 IllegalArgumentException이 발생한다")
        void throwsWhenTtlIsZero() {
            assertThatThrownBy(() -> SessionExpiration.of("session-001", "SINGLE", Duration.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ttl must be positive");
        }

        @Test
        @DisplayName("ttl이 음수이면 IllegalArgumentException이 발생한다")
        void throwsWhenTtlIsNegative() {
            assertThatThrownBy(
                            () ->
                                    SessionExpiration.of(
                                            "session-001", "SINGLE", Duration.ofHours(-1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ttl must be positive");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class Equality {

        @Test
        @DisplayName("같은 값의 SessionExpiration은 동등하다")
        void sameValuesAreEqual() {
            SessionExpiration exp1 =
                    SessionExpiration.of("session-001", "SINGLE", Duration.ofHours(1));
            SessionExpiration exp2 =
                    SessionExpiration.of("session-001", "SINGLE", Duration.ofHours(1));

            assertThat(exp1).isEqualTo(exp2);
            assertThat(exp1.hashCode()).isEqualTo(exp2.hashCode());
        }

        @Test
        @DisplayName("다른 sessionId를 가진 SessionExpiration은 동등하지 않다")
        void differentSessionIdAreNotEqual() {
            SessionExpiration exp1 =
                    SessionExpiration.of("session-001", "SINGLE", Duration.ofHours(1));
            SessionExpiration exp2 =
                    SessionExpiration.of("session-002", "SINGLE", Duration.ofHours(1));

            assertThat(exp1).isNotEqualTo(exp2);
        }
    }
}
