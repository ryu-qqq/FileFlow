package com.ryuqq.fileflow.application.transform.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.port.out.query.TransformQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
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
@DisplayName("TransformQueueOutboxReadManager 단위 테스트")
class TransformQueueOutboxReadManagerTest {

    @InjectMocks private TransformQueueOutboxReadManager sut;
    @Mock private TransformQueueOutboxQueryPort queryPort;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("findPendingMessages 메서드")
    class FindPendingMessagesTest {

        @Test
        @DisplayName("PENDING 메시지 조회를 쿼리 포트에 위임한다")
        void findPendingMessages_DelegatesToQueryPort() {
            TransformQueueOutbox outbox =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);
            given(queryPort.findPendingMessages(10)).willReturn(List.of(outbox));

            List<TransformQueueOutbox> result = sut.findPendingMessages(10);

            assertThat(result).hasSize(1);
            then(queryPort).should().findPendingMessages(10);
        }

        @Test
        @DisplayName("PENDING 메시지가 없으면 빈 리스트를 반환한다")
        void findPendingMessages_NoPending_ReturnsEmpty() {
            given(queryPort.findPendingMessages(10)).willReturn(Collections.emptyList());

            List<TransformQueueOutbox> result = sut.findPendingMessages(10);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countGroupByStatus 메서드")
    class CountGroupByStatusTest {

        @Test
        @DisplayName("상태별 카운트 조회를 쿼리 포트에 위임한다")
        void countGroupByStatus_DelegatesToQueryPort() {
            DateRange dateRange = DateRange.lastDays(1);
            OutboxStatusCount expected = new OutboxStatusCount(3L, 80L, 1L);
            given(queryPort.countGroupByStatus(dateRange)).willReturn(expected);

            OutboxStatusCount result = sut.countGroupByStatus(dateRange);

            assertThat(result).isEqualTo(expected);
            then(queryPort).should().countGroupByStatus(dateRange);
        }
    }
}
