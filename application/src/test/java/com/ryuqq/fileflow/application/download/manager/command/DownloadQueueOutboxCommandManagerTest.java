package com.ryuqq.fileflow.application.download.manager.command;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.port.out.command.DownloadQueueOutboxPersistencePort;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import com.ryuqq.fileflow.domain.download.id.DownloadQueueOutboxId;
import java.time.Instant;
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
    @Mock private DownloadQueueOutboxPersistencePort port;

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

            then(port).should().persist(outbox);
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

            then(port).should().persist(outbox);
        }
    }
}
