package com.ryuqq.fileflow.application.transform.manager.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.port.out.command.TransformCallbackOutboxPersistencePort;
import com.ryuqq.fileflow.application.transform.port.out.query.TransformCallbackOutboxQueryPort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import com.ryuqq.fileflow.domain.transform.id.TransformCallbackOutboxId;
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
@DisplayName("TransformCallbackOutboxCommandManager 단위 테스트")
class TransformCallbackOutboxCommandManagerTest {

    @InjectMocks private TransformCallbackOutboxCommandManager sut;
    @Mock private TransformCallbackOutboxPersistencePort transformCallbackOutboxPersistencePort;
    @Mock private TransformCallbackOutboxQueryPort transformCallbackOutboxQueryPort;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("TransformCallbackOutbox를 영속화 포트에 위임한다")
        void persist_TransformCallbackOutbox_DelegatesToPort() {
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/transform-done",
                            "COMPLETED",
                            NOW);

            sut.persist(outbox);

            then(transformCallbackOutboxPersistencePort).should().persist(outbox);
        }
    }

    @Nested
    @DisplayName("claimPendingMessages 메서드")
    class ClaimPendingMessagesTest {

        @Test
        @DisplayName("QueryPort에 위임하여 PENDING 메시지를 선점한다")
        void claimPendingMessages_DelegatesToQueryPort() {
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com",
                            "COMPLETED",
                            NOW);
            given(transformCallbackOutboxQueryPort.claimPendingMessages(10))
                    .willReturn(List.of(outbox));

            List<TransformCallbackOutbox> result = sut.claimPendingMessages(10);

            assertThat(result).hasSize(1);
            then(transformCallbackOutboxQueryPort).should().claimPendingMessages(10);
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

            then(transformCallbackOutboxPersistencePort).should().bulkMarkSent(ids, NOW);
        }
    }

    @Nested
    @DisplayName("bulkMarkFailed 메서드")
    class BulkMarkFailedTest {

        @Test
        @DisplayName("PersistencePort에 위임하여 벌크 FAILED 처리한다")
        void bulkMarkFailed_DelegatesToPersistencePort() {
            List<String> ids = List.of("outbox-003");

            sut.bulkMarkFailed(ids, NOW, "Callback failed");

            then(transformCallbackOutboxPersistencePort).should().bulkMarkFailed(ids, NOW, "Callback failed");
        }
    }
}
