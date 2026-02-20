package com.ryuqq.fileflow.application.transform.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.transform.manager.client.TransformQueueManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformQueueOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformQueueOutboxReadManager;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import com.ryuqq.fileflow.domain.transform.id.TransformQueueOutboxId;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessTransformQueueOutboxService 단위 테스트")
class ProcessTransformQueueOutboxServiceTest {

    @InjectMocks private ProcessTransformQueueOutboxService sut;
    @Mock private TransformQueueOutboxReadManager outboxReadManager;
    @Mock private TransformQueueOutboxCommandManager outboxCommandManager;
    @Mock private TransformQueueManager transformQueueManager;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("PENDING 메시지가 없으면 empty 결과를 반환한다")
        void execute_NoPending_ReturnsEmpty() {
            given(outboxReadManager.findPendingMessages(100)).willReturn(Collections.emptyList());

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
        }

        @Test
        @DisplayName("PENDING 메시지를 성공적으로 발행하면 SENT로 마킹한다")
        void execute_SuccessfulEnqueue_MarksSent() {
            TransformQueueOutbox outbox =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);
            given(outboxReadManager.findPendingMessages(100)).willReturn(List.of(outbox));

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            assertThat(outbox.status()).isEqualTo(OutboxStatus.SENT);
            then(transformQueueManager).should().enqueue("transform-001");
            then(outboxCommandManager).should().persist(outbox);
        }

        @Test
        @DisplayName("발행 실패 시 FAILED로 마킹한다")
        void execute_FailedEnqueue_MarksFailed() {
            TransformQueueOutbox outbox =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);
            given(outboxReadManager.findPendingMessages(100)).willReturn(List.of(outbox));
            willThrow(new RuntimeException("SQS error"))
                    .given(transformQueueManager)
                    .enqueue("transform-001");

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(1);
            assertThat(outbox.status()).isEqualTo(OutboxStatus.FAILED);
            then(outboxCommandManager).should().persist(outbox);
        }
    }
}
