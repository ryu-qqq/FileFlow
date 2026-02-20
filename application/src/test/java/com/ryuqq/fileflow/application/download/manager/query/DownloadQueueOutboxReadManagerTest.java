package com.ryuqq.fileflow.application.download.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.port.out.query.DownloadQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import com.ryuqq.fileflow.domain.download.id.DownloadQueueOutboxId;
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
@DisplayName("DownloadQueueOutboxReadManager 단위 테스트")
class DownloadQueueOutboxReadManagerTest {

    @InjectMocks private DownloadQueueOutboxReadManager sut;
    @Mock private DownloadQueueOutboxQueryPort queryPort;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("findPendingMessages 메서드")
    class FindPendingMessagesTest {

        @Test
        @DisplayName("PENDING 메시지 조회를 쿼리 포트에 위임한다")
        void findPendingMessages_DelegatesToQueryPort() {
            DownloadQueueOutbox outbox =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-001"), "download-001", NOW);
            given(queryPort.findPendingMessages(10)).willReturn(List.of(outbox));

            List<DownloadQueueOutbox> result = sut.findPendingMessages(10);

            assertThat(result).hasSize(1);
            then(queryPort).should().findPendingMessages(10);
        }

        @Test
        @DisplayName("PENDING 메시지가 없으면 빈 리스트를 반환한다")
        void findPendingMessages_NoPending_ReturnsEmpty() {
            given(queryPort.findPendingMessages(10)).willReturn(Collections.emptyList());

            List<DownloadQueueOutbox> result = sut.findPendingMessages(10);

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
            OutboxStatusCount expected = new OutboxStatusCount(5L, 100L, 2L);
            given(queryPort.countGroupByStatus(dateRange)).willReturn(expected);

            OutboxStatusCount result = sut.countGroupByStatus(dateRange);

            assertThat(result).isEqualTo(expected);
            then(queryPort).should().countGroupByStatus(dateRange);
        }
    }
}
