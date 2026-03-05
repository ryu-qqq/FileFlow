package com.ryuqq.fileflow.application.transform.manager.command;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.port.out.command.TransformCallbackOutboxPersistencePort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import com.ryuqq.fileflow.domain.transform.id.TransformCallbackOutboxId;
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
@DisplayName("TransformCallbackOutboxCommandManager 단위 테스트")
class TransformCallbackOutboxCommandManagerTest {

    @InjectMocks private TransformCallbackOutboxCommandManager sut;
    @Mock private TransformCallbackOutboxPersistencePort transformCallbackOutboxPersistencePort;

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("TransformCallbackOutbox를 영속화 포트에 위임한다")
        void persist_TransformCallbackOutbox_DelegatesToPort() {
            // given
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/transform-done",
                            "COMPLETED",
                            Instant.parse("2026-01-01T00:00:00Z"));

            // when
            sut.persist(outbox);

            // then
            then(transformCallbackOutboxPersistencePort).should().persist(outbox);
        }
    }
}
