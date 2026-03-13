package com.ryuqq.fileflow.application.transform.manager.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.port.out.command.TransformQueueOutboxPersistencePort;
import com.ryuqq.fileflow.application.transform.port.out.query.TransformQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import com.ryuqq.fileflow.domain.transform.id.TransformQueueOutboxId;
import java.time.Instant;
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
@DisplayName("TransformQueueOutboxCommandManager 단위 테스트")
class TransformQueueOutboxCommandManagerTest {

    @InjectMocks private TransformQueueOutboxCommandManager sut;
    @Mock private TransformQueueOutboxPersistencePort transformQueueOutboxPersistencePort;
    @Mock private TransformQueueOutboxQueryPort transformQueueOutboxQueryPort;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("TransformQueueOutbox를 영속화 포트에 위임한다")
        void persist_DelegatesToPort() {
            TransformQueueOutbox outbox =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);

            sut.persist(outbox);

            then(transformQueueOutboxPersistencePort).should().persist(outbox);
        }
    }

    @Nested
    @DisplayName("persist 메서드 — 업데이트 시나리오")
    class PersistUpdateTest {

        @Test
        @DisplayName("상태 변경된 TransformQueueOutbox를 영속화 포트에 위임한다")
        void persist_UpdatedOutbox_DelegatesToPort() {
            TransformQueueOutbox outbox =
                    TransformQueueOutbox.reconstitute(
                            TransformQueueOutboxId.of("outbox-001"),
                            "transform-001",
                            OutboxStatus.SENT,
                            0,
                            null,
                            NOW,
                            NOW.plusSeconds(5));

            sut.persist(outbox);

            then(transformQueueOutboxPersistencePort).should().persist(outbox);
        }
    }

    @Nested
    @DisplayName("claimPendingMessages 메서드")
    class ClaimPendingMessagesTest {

        @Test
        @DisplayName("QueryPort에 위임하여 PENDING 메시지를 선점한다")
        void claimPendingMessages_DelegatesToQueryPort() {
            TransformQueueOutbox outbox =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);
            given(transformQueueOutboxQueryPort.claimPendingMessages(100))
                    .willReturn(List.of(outbox));

            List<TransformQueueOutbox> result = sut.claimPendingMessages(100);

            assertThat(result).hasSize(1);
            then(transformQueueOutboxQueryPort).should().claimPendingMessages(100);
        }
    }

    @Nested
    @DisplayName("bulkMarkSent 메서드")
    class BulkMarkSentTest {

        @Test
        @DisplayName("PersistencePort에 위임하여 벌크 SENT 처리한다")
        void bulkMarkSent_DelegatesToPersistencePort() {
            List<String> ids = List.of("outbox-001", "outbox-002");

            sut.bulkMarkSent(ids, NOW);

            then(transformQueueOutboxPersistencePort).should().bulkMarkSent(ids, NOW);
        }
    }

    @Nested
    @DisplayName("bulkMarkFailed 메서드")
    class BulkMarkFailedTest {

        @Test
        @DisplayName("PersistencePort에 위임하여 벌크 FAILED 처리한다")
        void bulkMarkFailed_DelegatesToPersistencePort() {
            List<String> ids = List.of("outbox-003");

            sut.bulkMarkFailed(ids, NOW, "SQS error");

            then(transformQueueOutboxPersistencePort)
                    .should()
                    .bulkMarkFailed(ids, NOW, "SQS error");
        }
    }
}
