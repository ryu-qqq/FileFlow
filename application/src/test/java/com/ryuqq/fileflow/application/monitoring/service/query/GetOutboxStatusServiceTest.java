package com.ryuqq.fileflow.application.monitoring.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.manager.query.DownloadQueueOutboxReadManager;
import com.ryuqq.fileflow.application.monitoring.assembler.OutboxStatusAssembler;
import com.ryuqq.fileflow.application.monitoring.dto.query.OutboxStatusSearchParams;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxQueueStatusResponse;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxStatusResponse;
import com.ryuqq.fileflow.application.transform.manager.query.TransformQueueOutboxReadManager;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import java.time.Instant;
import java.time.LocalDate;
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
@DisplayName("GetOutboxStatusService 단위 테스트")
class GetOutboxStatusServiceTest {

    @InjectMocks private GetOutboxStatusService sut;
    @Mock private DownloadQueueOutboxReadManager downloadReadManager;
    @Mock private TransformQueueOutboxReadManager transformReadManager;
    @Mock private OutboxStatusAssembler outboxStatusAssembler;

    private static final Instant NOW = Instant.parse("2026-02-20T10:00:00Z");

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("DateRange로 Download/Transform 카운트를 조회하고 Assembler로 응답을 생성한다")
        void execute_QueriesBothAndAssembles() {
            DateRange dateRange =
                    DateRange.of(LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20));
            OutboxStatusSearchParams params = OutboxStatusSearchParams.of(dateRange);

            OutboxStatusCount downloadCount = new OutboxStatusCount(5L, 100L, 0L);
            OutboxStatusCount transformCount = new OutboxStatusCount(3L, 0L, 1L);

            given(downloadReadManager.countGroupByStatus(dateRange)).willReturn(downloadCount);
            given(transformReadManager.countGroupByStatus(dateRange)).willReturn(transformCount);

            OutboxStatusResponse expectedResponse =
                    new OutboxStatusResponse(
                            new OutboxQueueStatusResponse(5, 100, 0),
                            new OutboxQueueStatusResponse(3, 0, 1),
                            NOW);
            given(outboxStatusAssembler.toResponse(downloadCount, transformCount))
                    .willReturn(expectedResponse);

            OutboxStatusResponse result = sut.execute(params);

            assertThat(result).isEqualTo(expectedResponse);
            then(downloadReadManager).should().countGroupByStatus(dateRange);
            then(transformReadManager).should().countGroupByStatus(dateRange);
            then(outboxStatusAssembler).should().toResponse(downloadCount, transformCount);
        }
    }
}
