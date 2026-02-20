package com.ryuqq.fileflow.adapter.in.rest.monitoring.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.monitoring.MonitoringApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.monitoring.dto.response.OutboxStatusApiResponse;
import com.ryuqq.fileflow.application.monitoring.dto.query.OutboxStatusSearchParams;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxStatusResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * OutboxMonitoringQueryApiMapper 단위 테스트.
 *
 * <p>API Request → SearchParams, Application Response → API Response 변환 로직을 검증합니다.
 */
@Tag("unit")
@DisplayName("OutboxMonitoringQueryApiMapper 단위 테스트")
class OutboxMonitoringQueryApiMapperTest {

    private OutboxMonitoringQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OutboxMonitoringQueryApiMapper();
    }

    @Nested
    @DisplayName("toSearchParams(LocalDate, LocalDate)")
    class ToSearchParamsTest {

        @Test
        @DisplayName("startDate와 endDate가 주어지면 DateRange를 포함한 SearchParams를 생성한다")
        void toSearchParams_WithDates_CreatesSearchParams() {
            LocalDate startDate = LocalDate.of(2026, 2, 19);
            LocalDate endDate = LocalDate.of(2026, 2, 20);

            OutboxStatusSearchParams params = mapper.toSearchParams(startDate, endDate);

            assertThat(params.dateRange().startDate()).isEqualTo(startDate);
            assertThat(params.dateRange().endDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("둘 다 null이면 기본값(최근 1일)으로 SearchParams를 생성한다")
        void toSearchParams_BothNull_CreatesDefaultParams() {
            OutboxStatusSearchParams params = mapper.toSearchParams(null, null);

            assertThat(params.dateRange()).isNotNull();
            assertThat(params.dateRange().startDate()).isNotNull();
            assertThat(params.dateRange().endDate()).isNotNull();
        }

        @Test
        @DisplayName("startDate만 주어지면 endDate가 null인 DateRange를 생성한다")
        void toSearchParams_OnlyStartDate_CreatesOpenEndRange() {
            LocalDate startDate = LocalDate.of(2026, 2, 19);

            OutboxStatusSearchParams params = mapper.toSearchParams(startDate, null);

            assertThat(params.dateRange().startDate()).isEqualTo(startDate);
            assertThat(params.dateRange().endDate()).isNull();
        }
    }

    @Nested
    @DisplayName("toResponse(OutboxStatusResponse)")
    class ToOutboxStatusApiResponseTest {

        @Test
        @DisplayName("OutboxStatusResponse를 OutboxStatusApiResponse로 변환한다")
        void toResponse_outboxStatus_success() {
            OutboxStatusResponse response = MonitoringApiFixtures.outboxStatusResponse();

            OutboxStatusApiResponse apiResponse = mapper.toResponse(response);

            assertThat(apiResponse.download().pending())
                    .isEqualTo(MonitoringApiFixtures.DOWNLOAD_PENDING);
            assertThat(apiResponse.download().sent())
                    .isEqualTo(MonitoringApiFixtures.DOWNLOAD_SENT);
            assertThat(apiResponse.download().failed())
                    .isEqualTo(MonitoringApiFixtures.DOWNLOAD_FAILED);
            assertThat(apiResponse.transform().pending())
                    .isEqualTo(MonitoringApiFixtures.TRANSFORM_PENDING);
            assertThat(apiResponse.transform().sent())
                    .isEqualTo(MonitoringApiFixtures.TRANSFORM_SENT);
            assertThat(apiResponse.transform().failed())
                    .isEqualTo(MonitoringApiFixtures.TRANSFORM_FAILED);
        }

        @Test
        @DisplayName("Instant 타입의 checkedAt이 ISO 8601 형식의 문자열로 변환된다")
        void toResponse_outboxStatus_dateFormat() {
            OutboxStatusResponse response = MonitoringApiFixtures.outboxStatusResponse();

            OutboxStatusApiResponse apiResponse = mapper.toResponse(response);

            assertThat(apiResponse.checkedAt()).contains("T");
            assertThat(apiResponse.checkedAt()).contains("+");
            assertThat(apiResponse.checkedAt())
                    .isEqualTo(MonitoringApiFixtures.CHECKED_AT_FORMATTED);
        }
    }
}
