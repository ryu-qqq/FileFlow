package com.ryuqq.fileflow.application.download.manager.command;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.port.out.command.CallbackOutboxPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.id.CallbackOutboxId;
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
@DisplayName("CallbackOutboxCommandManager 단위 테스트")
class CallbackOutboxCommandManagerTest {

    @InjectMocks private CallbackOutboxCommandManager sut;
    @Mock private CallbackOutboxPersistencePort callbackOutboxPersistencePort;

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("CallbackOutbox를 영속화 포트에 위임한다")
        void persist_CallbackOutbox_DelegatesToPort() {
            // given
            CallbackOutbox callbackOutbox =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-001"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            Instant.parse("2026-01-01T00:00:00Z"));

            // when
            sut.persist(callbackOutbox);

            // then
            then(callbackOutboxPersistencePort).should().persist(callbackOutbox);
        }
    }
}
