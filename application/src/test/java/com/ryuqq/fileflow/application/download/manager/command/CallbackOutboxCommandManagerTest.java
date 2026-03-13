package com.ryuqq.fileflow.application.download.manager.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.port.out.command.CallbackOutboxPersistencePort;
import com.ryuqq.fileflow.application.download.port.out.query.CallbackOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.id.CallbackOutboxId;
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
@DisplayName("CallbackOutboxCommandManager 단위 테스트")
class CallbackOutboxCommandManagerTest {

    @InjectMocks private CallbackOutboxCommandManager sut;
    @Mock private CallbackOutboxPersistencePort callbackOutboxPersistencePort;
    @Mock private CallbackOutboxQueryPort callbackOutboxQueryPort;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("CallbackOutbox를 영속화 포트에 위임한다")
        void persist_CallbackOutbox_DelegatesToPort() {
            CallbackOutbox callbackOutbox =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-001"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);

            sut.persist(callbackOutbox);

            then(callbackOutboxPersistencePort).should().persist(callbackOutbox);
        }
    }

    @Nested
    @DisplayName("claimPendingMessages 메서드")
    class ClaimPendingMessagesTest {

        @Test
        @DisplayName("PENDING 메시지 선점을 쿼리 포트에 위임한다")
        void claimPendingMessages_DelegatesToQueryPort() {
            CallbackOutbox outbox =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-001"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);
            given(callbackOutboxQueryPort.claimPendingMessages(10)).willReturn(List.of(outbox));

            List<CallbackOutbox> result = sut.claimPendingMessages(10);

            assertThat(result).hasSize(1);
            then(callbackOutboxQueryPort).should().claimPendingMessages(10);
        }
    }

    @Nested
    @DisplayName("bulkMarkSent 메서드")
    class BulkMarkSentTest {

        @Test
        @DisplayName("ID 목록의 일괄 전송 완료 처리를 영속화 포트에 위임한다")
        void bulkMarkSent_DelegatesToPersistencePort() {
            List<String> ids = List.of("outbox-001", "outbox-002");

            sut.bulkMarkSent(ids, NOW);

            then(callbackOutboxPersistencePort).should().bulkMarkSent(ids, NOW);
        }
    }

    @Nested
    @DisplayName("bulkMarkFailed 메서드")
    class BulkMarkFailedTest {

        @Test
        @DisplayName("ID 목록의 일괄 실패 처리를 영속화 포트에 위임한다")
        void bulkMarkFailed_DelegatesToPersistencePort() {
            List<String> ids = List.of("outbox-003", "outbox-004");

            sut.bulkMarkFailed(ids, NOW, "Callback failed");

            then(callbackOutboxPersistencePort)
                    .should()
                    .bulkMarkFailed(ids, NOW, "Callback failed");
        }
    }
}
