package com.ryuqq.fileflow.domain.transform.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.transform.id.TransformQueueOutboxId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("TransformQueueOutbox 단위 테스트")
class TransformQueueOutboxTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("forNew 메서드")
    class ForNewTest {

        @Test
        @DisplayName("신규 아웃박스 생성 시 PENDING 상태로 초기화된다")
        void forNew_InitializesPendingStatus() {
            TransformQueueOutbox outbox =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);

            assertThat(outbox.idValue()).isEqualTo("outbox-001");
            assertThat(outbox.transformRequestId()).isEqualTo("transform-001");
            assertThat(outbox.status()).isEqualTo(OutboxStatus.PENDING);
            assertThat(outbox.retryCount()).isZero();
            assertThat(outbox.lastError()).isNull();
            assertThat(outbox.createdAt()).isEqualTo(NOW);
            assertThat(outbox.processedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("markSent 메서드")
    class MarkSentTest {

        @Test
        @DisplayName("SENT 상태로 전환된다")
        void markSent_TransitionsToSent() {
            TransformQueueOutbox outbox =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);
            Instant sentAt = NOW.plusSeconds(5);

            outbox.markSent(sentAt);

            assertThat(outbox.status()).isEqualTo(OutboxStatus.SENT);
            assertThat(outbox.processedAt()).isEqualTo(sentAt);
        }
    }

    @Nested
    @DisplayName("markFailed 메서드")
    class MarkFailedTest {

        @Test
        @DisplayName("FAILED 상태로 전환되고 retryCount가 증가한다")
        void markFailed_TransitionsToFailedAndIncrementsRetry() {
            TransformQueueOutbox outbox =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);
            Instant failedAt = NOW.plusSeconds(5);

            outbox.markFailed("SQS connection error", failedAt);

            assertThat(outbox.status()).isEqualTo(OutboxStatus.FAILED);
            assertThat(outbox.retryCount()).isEqualTo(1);
            assertThat(outbox.lastError()).isEqualTo("SQS connection error");
            assertThat(outbox.processedAt()).isEqualTo(failedAt);
        }
    }

    @Nested
    @DisplayName("reconstitute 메서드")
    class ReconstituteTest {

        @Test
        @DisplayName("저장된 데이터로 아웃박스를 복원한다")
        void reconstitute_RestoresFromPersistedData() {
            Instant processedAt = NOW.plusSeconds(10);
            TransformQueueOutbox outbox =
                    TransformQueueOutbox.reconstitute(
                            TransformQueueOutboxId.of("outbox-001"),
                            "transform-001",
                            OutboxStatus.SENT,
                            0,
                            null,
                            NOW,
                            processedAt);

            assertThat(outbox.idValue()).isEqualTo("outbox-001");
            assertThat(outbox.transformRequestId()).isEqualTo("transform-001");
            assertThat(outbox.status()).isEqualTo(OutboxStatus.SENT);
            assertThat(outbox.processedAt()).isEqualTo(processedAt);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 ID의 아웃박스는 동일하다")
        void sameId_AreEqual() {
            TransformQueueOutbox a =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);
            TransformQueueOutbox b =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-002", NOW);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 ID의 아웃박스는 다르다")
        void differentId_AreNotEqual() {
            TransformQueueOutbox a =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);
            TransformQueueOutbox b =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-002"), "transform-001", NOW);

            assertThat(a).isNotEqualTo(b);
        }
    }
}
