package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("WebhookOutboxStatus 단위 테스트")
class WebhookOutboxStatusTest {

    @Nested
    @DisplayName("재시도 가능 여부 테스트")
    class CanRetryTest {

        @Test
        @DisplayName("PENDING 상태는 재시도 가능하다")
        void pending_ShouldBeRetryable() {
            // given
            WebhookOutboxStatus status = WebhookOutboxStatus.PENDING;

            // when & then
            assertThat(status.canRetry()).isTrue();
        }

        @Test
        @DisplayName("SENT 상태는 재시도 불가능하다")
        void sent_ShouldNotBeRetryable() {
            // given
            WebhookOutboxStatus status = WebhookOutboxStatus.SENT;

            // when & then
            assertThat(status.canRetry()).isFalse();
        }

        @Test
        @DisplayName("FAILED 상태는 재시도 불가능하다")
        void failed_ShouldNotBeRetryable() {
            // given
            WebhookOutboxStatus status = WebhookOutboxStatus.FAILED;

            // when & then
            assertThat(status.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("종료 상태 여부 테스트")
    class IsTerminalTest {

        @Test
        @DisplayName("PENDING 상태는 종료 상태가 아니다")
        void pending_ShouldNotBeTerminal() {
            // given
            WebhookOutboxStatus status = WebhookOutboxStatus.PENDING;

            // when & then
            assertThat(status.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("SENT 상태는 종료 상태이다")
        void sent_ShouldBeTerminal() {
            // given
            WebhookOutboxStatus status = WebhookOutboxStatus.SENT;

            // when & then
            assertThat(status.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("FAILED 상태는 종료 상태이다")
        void failed_ShouldBeTerminal() {
            // given
            WebhookOutboxStatus status = WebhookOutboxStatus.FAILED;

            // when & then
            assertThat(status.isTerminal()).isTrue();
        }
    }
}
