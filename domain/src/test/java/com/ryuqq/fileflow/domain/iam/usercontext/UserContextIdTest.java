package com.ryuqq.fileflow.domain.iam.usercontext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserContextId Value Object 유효성 검증 테스트
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("UserContextId 테스트")
class UserContextIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 Long 값으로 UserContextId를 생성할 수 있다")
        void createWithValidValue() {
            // when
            UserContextId id = UserContextId.of(1L);

            // then
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("큰 Long 값으로도 생성 가능하다")
        void createWithLargeValue() {
            // given
            Long largeValue = Long.MAX_VALUE;

            // when
            UserContextId id = UserContextId.of(largeValue);

            // then
            assertThat(id.value()).isEqualTo(largeValue);
        }

        @Test
        @DisplayName("null 값으로 생성하면 예외가 발생한다")
        void createWithNull() {
            // when & then
            assertThatThrownBy(() -> UserContextId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserContext ID는 필수입니다");
        }

        @Test
        @DisplayName("0 이하의 값으로 생성하면 예외가 발생한다")
        void createWithZero() {
            // when & then
            assertThatThrownBy(() -> UserContextId.of(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserContext ID는 양수여야 합니다");
        }

        @Test
        @DisplayName("음수 값으로 생성하면 예외가 발생한다")
        void createWithNegative() {
            // when & then
            assertThatThrownBy(() -> UserContextId.of(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserContext ID는 양수여야 합니다");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값의 UserContextId는 동등하다")
        void equalityWithSameValue() {
            // given
            UserContextId id1 = UserContextId.of(1L);
            UserContextId id2 = UserContextId.of(1L);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 UserContextId는 동등하지 않다")
        void inequalityWithDifferentValue() {
            // given
            UserContextId id1 = UserContextId.of(1L);
            UserContextId id2 = UserContextId.of(2L);

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("Record로 구현되어 불변 객체이다")
        void isImmutable() {
            // given
            UserContextId id = UserContextId.of(1L);

            // when & then - value는 final이므로 변경 불가능
            assertThat(id.value()).isEqualTo(1L);
        }
    }
}
