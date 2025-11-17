package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RetryCount Value Object 테스트
 */
class RetryCountTest {

    @Test
    @DisplayName("File용 RetryCount를 생성해야 한다 (최대 3회)")
    void shouldCreateRetryCountForFile() {
        // when
        RetryCount retryCount = RetryCount.forFile();

        // then
        assertThat(retryCount).isNotNull();
        assertThat(retryCount.current()).isEqualTo(0);
        assertThat(retryCount.max()).isEqualTo(3);
        assertThat(retryCount.canRetry()).isTrue();
    }

    @Test
    @DisplayName("Job용 RetryCount를 생성해야 한다 (최대 2회)")
    void shouldCreateRetryCountForJob() {
        // when
        RetryCount retryCount = RetryCount.forJob();

        // then
        assertThat(retryCount).isNotNull();
        assertThat(retryCount.current()).isEqualTo(0);
        assertThat(retryCount.max()).isEqualTo(2);
        assertThat(retryCount.canRetry()).isTrue();
    }

    @Test
    @DisplayName("Outbox용 RetryCount를 생성해야 한다 (최대 3회)")
    void shouldCreateRetryCountForOutbox() {
        // when
        RetryCount retryCount = RetryCount.forOutbox();

        // then
        assertThat(retryCount).isNotNull();
        assertThat(retryCount.current()).isEqualTo(0);
        assertThat(retryCount.max()).isEqualTo(3);
        assertThat(retryCount.canRetry()).isTrue();
    }

    @Test
    @DisplayName("재시도 가능 여부를 확인할 수 있어야 한다")
    void shouldCheckCanRetry() {
        // given
        RetryCount retryCount = RetryCount.forJob(); // max=2

        // when & then
        assertThat(retryCount.canRetry()).isTrue(); // 0 < 2

        RetryCount incremented1 = retryCount.increment();
        assertThat(incremented1.canRetry()).isTrue(); // 1 < 2

        RetryCount incremented2 = incremented1.increment();
        assertThat(incremented2.canRetry()).isFalse(); // 2 < 2 (false)
    }

    @Test
    @DisplayName("재시도 횟수를 증가시키면 새로운 인스턴스를 반환해야 한다")
    void shouldIncrementRetryCount() {
        // given
        RetryCount retryCount = RetryCount.forFile(); // current=0, max=3

        // when
        RetryCount incremented = retryCount.increment();

        // then
        assertThat(retryCount.current()).isEqualTo(0); // 원본 불변
        assertThat(incremented.current()).isEqualTo(1); // 새 인스턴스
        assertThat(incremented.max()).isEqualTo(3);
    }

    @Test
    @DisplayName("최대 재시도 횟수를 초과하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenExceedingMaxRetry() {
        // given
        RetryCount retryCount = RetryCount.forJob(); // max=2
        RetryCount maxReached = retryCount.increment().increment(); // current=2

        // when & then
        assertThatThrownBy(maxReached::increment)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최대 재시도 횟수를 초과했습니다");
    }

    @Test
    @DisplayName("같은 값을 가진 RetryCount는 동등해야 한다")
    void shouldBeEqualWhenValuesAreSame() {
        // given
        RetryCount retryCount1 = RetryCount.forFile();
        RetryCount retryCount2 = RetryCount.forFile();

        // when & then
        assertThat(retryCount1).isEqualTo(retryCount2);
    }

    @Test
    @DisplayName("재시도 잔여 횟수를 확인할 수 있어야 한다")
    void shouldGetRemainingRetries() {
        // given
        RetryCount retryCount = RetryCount.forFile(); // max=3

        // when & then
        assertThat(retryCount.remaining()).isEqualTo(3); // 3 - 0

        RetryCount incremented = retryCount.increment();
        assertThat(incremented.remaining()).isEqualTo(2); // 3 - 1
    }
}
