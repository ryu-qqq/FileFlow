package com.ryuqq.fileflow.domain.transform.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.transform.id.TransformCallbackOutboxId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TransformCallbackOutboxTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("forNew - 새 아웃박스 생성")
    class ForNew {

        @Test
        @DisplayName("PENDING 상태로 초기화된다")
        void forNew_creates_pending_outbox() {
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);

            assertThat(outbox.idValue()).isEqualTo("outbox-001");
            assertThat(outbox.transformRequestId()).isEqualTo("transform-001");
            assertThat(outbox.callbackUrl()).isEqualTo("https://callback.example.com/done");
            assertThat(outbox.taskStatus()).isEqualTo("COMPLETED");
            assertThat(outbox.outboxStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(outbox.retryCount()).isZero();
            assertThat(outbox.maxRetries()).isEqualTo(5);
            assertThat(outbox.lastError()).isNull();
            assertThat(outbox.createdAt()).isEqualTo(NOW);
            assertThat(outbox.processedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("markSent - 전송 성공")
    class MarkSent {

        @Test
        @DisplayName("SENT 상태로 전이된다")
        void markSent_transitions_to_sent() {
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);
            Instant sentAt = NOW.plusSeconds(5);

            outbox.markSent(sentAt);

            assertThat(outbox.outboxStatus()).isEqualTo(OutboxStatus.SENT);
            assertThat(outbox.processedAt()).isEqualTo(sentAt);
        }
    }

    @Nested
    @DisplayName("markFailed - 재시도 가능한 실패")
    class MarkFailed {

        @Test
        @DisplayName("retryCount가 maxRetries 미만이면 PENDING을 유지한다")
        void markFailed_under_max_retries_keeps_pending() {
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);

            outbox.markFailed("Connection refused", NOW.plusSeconds(5));

            assertThat(outbox.outboxStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(outbox.retryCount()).isEqualTo(1);
            assertThat(outbox.lastError()).isEqualTo("Connection refused");
        }

        @Test
        @DisplayName("retryCount가 maxRetries에 도달하면 FAILED로 전이된다")
        void markFailed_at_max_retries_transitions_to_failed() {
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.reconstitute(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            OutboxStatus.PENDING,
                            4,
                            5,
                            "Previous error",
                            NOW,
                            null);

            outbox.markFailed("Final failure", NOW.plusSeconds(30));

            assertThat(outbox.outboxStatus()).isEqualTo(OutboxStatus.FAILED);
            assertThat(outbox.retryCount()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("markFailedPermanently - 영구 실패")
    class MarkFailedPermanently {

        @Test
        @DisplayName("즉시 FAILED 상태로 전이된다")
        void markFailedPermanently_transitions_to_failed() {
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);

            outbox.markFailedPermanently("404 Not Found", NOW.plusSeconds(5));

            assertThat(outbox.outboxStatus()).isEqualTo(OutboxStatus.FAILED);
            assertThat(outbox.lastError()).isEqualTo("404 Not Found");
            assertThat(outbox.processedAt()).isEqualTo(NOW.plusSeconds(5));
        }
    }

    @Nested
    @DisplayName("equals/hashCode - ID 기반 동등성")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 ID를 가진 아웃박스는 동등하다")
        void same_id_are_equal() {
            TransformCallbackOutbox outbox1 =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback1.com",
                            "COMPLETED",
                            NOW);
            TransformCallbackOutbox outbox2 =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-002",
                            "https://callback2.com",
                            "FAILED",
                            NOW);

            assertThat(outbox1).isEqualTo(outbox2);
            assertThat(outbox1.hashCode()).isEqualTo(outbox2.hashCode());
        }
    }
}
