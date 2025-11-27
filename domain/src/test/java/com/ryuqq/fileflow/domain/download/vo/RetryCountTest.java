package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RetryCount 단위 테스트")
class RetryCountTest {

    private static final int MAX_RETRY_COUNT = 2;

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("초기값 0으로 생성할 수 있다")
        void initial_ShouldCreateWithZero() {
            // when
            RetryCount retryCount = RetryCount.initial();

            // then
            assertThat(retryCount.value()).isEqualTo(0);
        }

        @Test
        @DisplayName("유효한 값으로 생성할 수 있다")
        void of_WithValidValue_ShouldCreate() {
            // when
            RetryCount retryCount = RetryCount.of(1);

            // then
            assertThat(retryCount.value()).isEqualTo(1);
        }

        @Test
        @DisplayName("음수 값으로 생성 시 예외가 발생한다")
        void of_WithNegativeValue_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> RetryCount.of(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("음수");
        }
    }

    @Nested
    @DisplayName("재시도 가능 여부 테스트")
    class CanRetryTest {

        @Test
        @DisplayName("retryCount가 0이면 재시도 가능하다")
        void canRetry_WhenZero_ShouldReturnTrue() {
            // given
            RetryCount retryCount = RetryCount.initial();

            // when & then
            assertThat(retryCount.canRetry()).isTrue();
        }

        @Test
        @DisplayName("retryCount가 1이면 재시도 가능하다")
        void canRetry_WhenOne_ShouldReturnTrue() {
            // given
            RetryCount retryCount = RetryCount.of(1);

            // when & then
            assertThat(retryCount.canRetry()).isTrue();
        }

        @Test
        @DisplayName("retryCount가 최대값(2)이면 재시도 불가능하다")
        void canRetry_WhenMax_ShouldReturnFalse() {
            // given
            RetryCount retryCount = RetryCount.of(MAX_RETRY_COUNT);

            // when & then
            assertThat(retryCount.canRetry()).isFalse();
        }

        @Test
        @DisplayName("retryCount가 최대값 초과이면 재시도 불가능하다")
        void canRetry_WhenOverMax_ShouldReturnFalse() {
            // given
            RetryCount retryCount = RetryCount.of(MAX_RETRY_COUNT + 1);

            // when & then
            assertThat(retryCount.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("증가 테스트")
    class IncrementTest {

        @Test
        @DisplayName("increment 호출 시 새로운 RetryCount가 반환된다")
        void increment_ShouldReturnNewRetryCountWithIncrementedValue() {
            // given
            RetryCount retryCount = RetryCount.initial();

            // when
            RetryCount incremented = retryCount.increment();

            // then
            assertThat(incremented.value()).isEqualTo(1);
            assertThat(retryCount.value()).isEqualTo(0); // 불변성 확인
        }

        @Test
        @DisplayName("연속 increment가 올바르게 동작한다")
        void increment_MultipleTimes_ShouldWork() {
            // given
            RetryCount retryCount = RetryCount.initial();

            // when
            RetryCount first = retryCount.increment();
            RetryCount second = first.increment();

            // then
            assertThat(second.value()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 RetryCount는 동등하다")
        void equals_WithSameValue_ShouldBeEqual() {
            // given
            RetryCount count1 = RetryCount.of(1);
            RetryCount count2 = RetryCount.of(1);

            // when & then
            assertThat(count1).isEqualTo(count2);
            assertThat(count1.hashCode()).isEqualTo(count2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 RetryCount는 동등하지 않다")
        void equals_WithDifferentValue_ShouldNotBeEqual() {
            // given
            RetryCount count1 = RetryCount.of(1);
            RetryCount count2 = RetryCount.of(2);

            // when & then
            assertThat(count1).isNotEqualTo(count2);
        }
    }
}
