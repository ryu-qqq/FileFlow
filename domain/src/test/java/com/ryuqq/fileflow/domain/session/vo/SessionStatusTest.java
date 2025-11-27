package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("SessionStatus 단위 테스트")
class SessionStatusTest {

    @Nested
    @DisplayName("상태 전환 테스트")
    class TransitionTest {

        @Test
        @DisplayName("PREPARING에서 ACTIVE로 전환할 수 있다")
        void canTransitionTo_FromPreparingToActive_ShouldReturnTrue() {
            // given
            SessionStatus preparing = SessionStatus.PREPARING;

            // when
            boolean result = preparing.canTransitionTo(SessionStatus.ACTIVE);

            // then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @CsvSource({
            "PREPARING, COMPLETED, false",
            "PREPARING, EXPIRED, false",
            "PREPARING, FAILED, false",
            "PREPARING, PREPARING, false"
        })
        @DisplayName("PREPARING에서 ACTIVE 이외 상태로는 전환할 수 없다")
        void canTransitionTo_FromPreparingToOther_ShouldReturnFalse(
                SessionStatus from, SessionStatus to, boolean expected) {
            // when
            boolean result = from.canTransitionTo(to);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({"ACTIVE, COMPLETED, true", "ACTIVE, EXPIRED, true", "ACTIVE, FAILED, true"})
        @DisplayName("ACTIVE에서 COMPLETED, EXPIRED, FAILED로 전환할 수 있다")
        void canTransitionTo_FromActiveToTerminal_ShouldReturnTrue(
                SessionStatus from, SessionStatus to, boolean expected) {
            // when
            boolean result = from.canTransitionTo(to);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({"ACTIVE, ACTIVE, false", "ACTIVE, PREPARING, false"})
        @DisplayName("ACTIVE에서 ACTIVE나 PREPARING으로는 전환할 수 없다")
        void canTransitionTo_FromActiveToInvalid_ShouldReturnFalse(
                SessionStatus from, SessionStatus to, boolean expected) {
            // when
            boolean result = from.canTransitionTo(to);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
            "COMPLETED, PREPARING",
            "COMPLETED, ACTIVE",
            "COMPLETED, EXPIRED",
            "COMPLETED, FAILED",
            "COMPLETED, COMPLETED"
        })
        @DisplayName("COMPLETED 상태에서는 어떤 상태로도 전환할 수 없다")
        void canTransitionTo_FromCompleted_ShouldReturnFalse(SessionStatus from, SessionStatus to) {
            // when
            boolean result = from.canTransitionTo(to);

            // then
            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @CsvSource({
            "EXPIRED, PREPARING",
            "EXPIRED, ACTIVE",
            "EXPIRED, COMPLETED",
            "EXPIRED, FAILED",
            "EXPIRED, EXPIRED"
        })
        @DisplayName("EXPIRED 상태에서는 어떤 상태로도 전환할 수 없다")
        void canTransitionTo_FromExpired_ShouldReturnFalse(SessionStatus from, SessionStatus to) {
            // when
            boolean result = from.canTransitionTo(to);

            // then
            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @CsvSource({
            "FAILED, PREPARING",
            "FAILED, ACTIVE",
            "FAILED, COMPLETED",
            "FAILED, EXPIRED",
            "FAILED, FAILED"
        })
        @DisplayName("FAILED 상태에서는 어떤 상태로도 전환할 수 없다")
        void canTransitionTo_FromFailed_ShouldReturnFalse(SessionStatus from, SessionStatus to) {
            // when
            boolean result = from.canTransitionTo(to);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Enum 기본 테스트")
    class EnumBasicTest {

        @Test
        @DisplayName("모든 상태 값이 정의되어 있다")
        void values_ShouldContainAllStatuses() {
            // when
            SessionStatus[] values = SessionStatus.values();

            // then
            assertThat(values).hasSize(5);
            assertThat(values)
                    .containsExactly(
                            SessionStatus.PREPARING,
                            SessionStatus.ACTIVE,
                            SessionStatus.COMPLETED,
                            SessionStatus.EXPIRED,
                            SessionStatus.FAILED);
        }

        @Test
        @DisplayName("문자열로 상태를 찾을 수 있다")
        void valueOf_WithValidName_ShouldReturnStatus() {
            // when & then
            assertThat(SessionStatus.valueOf("PREPARING")).isEqualTo(SessionStatus.PREPARING);
            assertThat(SessionStatus.valueOf("ACTIVE")).isEqualTo(SessionStatus.ACTIVE);
            assertThat(SessionStatus.valueOf("COMPLETED")).isEqualTo(SessionStatus.COMPLETED);
            assertThat(SessionStatus.valueOf("EXPIRED")).isEqualTo(SessionStatus.EXPIRED);
            assertThat(SessionStatus.valueOf("FAILED")).isEqualTo(SessionStatus.FAILED);
        }
    }
}
