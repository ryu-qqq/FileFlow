package com.ryuqq.fileflow.application.download.manager.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.port.out.command.DownloadQueueOutboxPersistencePort;
import com.ryuqq.fileflow.application.download.port.out.query.DownloadQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import com.ryuqq.fileflow.domain.download.id.DownloadQueueOutboxId;
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
@DisplayName("DownloadQueueOutboxCommandManager 단위 테스트")
class DownloadQueueOutboxCommandManagerTest {

    @InjectMocks private DownloadQueueOutboxCommandManager sut;
    @Mock private DownloadQueueOutboxPersistencePort downloadQueueOutboxPersistencePort;
    @Mock private DownloadQueueOutboxQueryPort downloadQueueOutboxQueryPort;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("DownloadQueueOutbox를 영속화 포트에 위임한다")
        void persist_DelegatesToPort() {
            DownloadQueueOutbox outbox =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-001"), "download-001", NOW);

            sut.persist(outbox);

            then(downloadQueueOutboxPersistencePort).should().persist(outbox);
        }
    }

    @Nested
    @DisplayName("persist 메서드 — 업데이트 시나리오")
    class PersistUpdateTest {

        @Test
        @DisplayName("상태 변경된 DownloadQueueOutbox를 영속화 포트에 위임한다")
        void persist_UpdatedOutbox_DelegatesToPort() {
            DownloadQueueOutbox outbox =
                    DownloadQueueOutbox.reconstitute(
                            DownloadQueueOutboxId.of("outbox-001"),
                            "download-001",
                            OutboxStatus.SENT,
                            0,
                            null,
                            NOW,
                            NOW.plusSeconds(5));

            sut.persist(outbox);

            then(downloadQueueOutboxPersistencePort).should().persist(outbox);
        }
    }

    @Nested
    @DisplayName("claimPendingMessages 메서드")
    class ClaimPendingMessagesTest {

        @Test
        @DisplayName("PENDING 메시지 선점을 쿼리 포트에 위임한다")
        void claimPendingMessages_DelegatesToQueryPort() {
            DownloadQueueOutbox outbox =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-001"), "download-001", NOW);
            given(downloadQueueOutboxQueryPort.claimPendingMessages(10))
                    .willReturn(List.of(outbox));

            List<DownloadQueueOutbox> result = sut.claimPendingMessages(10);

            assertThat(result).hasSize(1);
            then(downloadQueueOutboxQueryPort).should().claimPendingMessages(10);
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

            then(downloadQueueOutboxPersistencePort).should().bulkMarkSent(ids, NOW);
        }
    }

    @Nested
    @DisplayName("bulkMarkFailed 메서드")
    class BulkMarkFailedTest {

        @Test
        @DisplayName("ID 목록의 일괄 실패 처리를 영속화 포트에 위임한다")
        void bulkMarkFailed_DelegatesToPersistencePort() {
            List<String> ids = List.of("outbox-003", "outbox-004");

            sut.bulkMarkFailed(ids, NOW);

            then(downloadQueueOutboxPersistencePort).should().bulkMarkFailed(ids, NOW);
        }
    }
}
