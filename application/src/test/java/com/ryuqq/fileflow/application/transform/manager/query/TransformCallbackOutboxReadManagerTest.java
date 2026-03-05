package com.ryuqq.fileflow.application.transform.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
@DisplayName("TransformCallbackOutboxReadManager 단위 테스트")
class TransformCallbackOutboxReadManagerTest {

    @InjectMocks private TransformCallbackOutboxReadManager sut;
    @Mock private TransformCallbackOutboxQueryPort transformCallbackOutboxQueryPort;

    @Nested
    @DisplayName("findPendingMessages 메서드")
    class FindPendingMessagesTest {

        @Test
        @DisplayName("PENDING 상태의 아웃박스 목록을 쿼리 포트에서 조회한다")
        void findPendingMessages_DelegatesToPort() {
            // given
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/transform-done",
                            "COMPLETED",
                            Instant.parse("2026-01-01T00:00:00Z"));
            given(transformCallbackOutboxQueryPort.findPendingMessages(10))
                    .willReturn(List.of(outbox));

            // when
            List<TransformCallbackOutbox> result = sut.findPendingMessages(10);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).idValue()).isEqualTo("outbox-001");
        }
    }
}
