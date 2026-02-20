package com.ryuqq.fileflow.adapter.in.rest.monitoring.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.adapter.in.rest.monitoring.MonitoringApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.monitoring.dto.response.OutboxStatusApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.monitoring.mapper.OutboxMonitoringQueryApiMapper;
import com.ryuqq.fileflow.application.monitoring.dto.query.OutboxStatusSearchParams;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxStatusResponse;
import com.ryuqq.fileflow.application.monitoring.port.in.query.GetOutboxStatusUseCase;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;

/**
 * OutboxMonitoringQueryController REST Docs 테스트.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("OutboxMonitoringQueryController REST Docs 테스트")
@WebMvcTest(OutboxMonitoringQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class OutboxMonitoringQueryControllerRestDocsTest extends RestDocsTestSupport {

    @MockBean private GetOutboxStatusUseCase getOutboxStatusUseCase;

    @MockBean private OutboxMonitoringQueryApiMapper queryMapper;

    @Nested
    @DisplayName("Outbox 상태 조회 API")
    class GetOutboxStatusTest {

        @Test
        @DisplayName("GET /api/v1/monitoring/outbox-status - 기간 지정 조회 성공")
        void getOutboxStatus_withDateRange_success() throws Exception {
            OutboxStatusApiResponse apiResponse = MonitoringApiFixtures.outboxStatusApiResponse();

            given(queryMapper.toSearchParams(any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(MonitoringApiFixtures.outboxStatusSearchParams());
            given(getOutboxStatusUseCase.execute(any(OutboxStatusSearchParams.class)))
                    .willReturn(MonitoringApiFixtures.outboxStatusResponse());
            given(queryMapper.toResponse(any(OutboxStatusResponse.class))).willReturn(apiResponse);

            mockMvc.perform(
                            get("/api/v1/monitoring/outbox-status")
                                    .param("startDate", "2026-02-19")
                                    .param("endDate", "2026-02-20"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.download.pending")
                                    .value(apiResponse.download().pending()))
                    .andExpect(
                            jsonPath("$.data.download.sent").value(apiResponse.download().sent()))
                    .andExpect(
                            jsonPath("$.data.download.failed")
                                    .value(apiResponse.download().failed()))
                    .andExpect(
                            jsonPath("$.data.transform.pending")
                                    .value(apiResponse.transform().pending()))
                    .andExpect(
                            jsonPath("$.data.transform.sent").value(apiResponse.transform().sent()))
                    .andExpect(
                            jsonPath("$.data.transform.failed")
                                    .value(apiResponse.transform().failed()))
                    .andExpect(jsonPath("$.data.checkedAt").value(apiResponse.checkedAt()))
                    .andDo(
                            document.document(
                                    queryParameters(
                                            parameterWithName("startDate")
                                                    .description(
                                                            "조회 시작일 (yyyy-MM-dd, 미지정 시"
                                                                    + " 기본 최근 1일)")
                                                    .optional(),
                                            parameterWithName("endDate")
                                                    .description(
                                                            "조회 종료일 (yyyy-MM-dd, 미지정 시" + " 기본 오늘)")
                                                    .optional()),
                                    responseFields(
                                            fieldWithPath("data.download.pending")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("다운로드 PENDING 상태 건수"),
                                            fieldWithPath("data.download.sent")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("다운로드 SENT 상태 건수 (지정 기간" + " 내)"),
                                            fieldWithPath("data.download.failed")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("다운로드 FAILED 상태 건수"),
                                            fieldWithPath("data.transform.pending")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("변환 PENDING 상태 건수"),
                                            fieldWithPath("data.transform.sent")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("변환 SENT 상태 건수 (지정 기간" + " 내)"),
                                            fieldWithPath("data.transform.failed")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("변환 FAILED 상태 건수"),
                                            fieldWithPath("data.checkedAt")
                                                    .type(JsonFieldType.STRING)
                                                    .description("조회 시각 (ISO 8601)"),
                                            fieldWithPath("timestamp")
                                                    .type(JsonFieldType.STRING)
                                                    .description("응답 시각")
                                                    .optional(),
                                            fieldWithPath("requestId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("요청 ID")
                                                    .optional())));
        }

        @Test
        @DisplayName("GET /api/v1/monitoring/outbox-status - 기간 미지정 시 기본값 사용")
        void getOutboxStatus_withoutDateRange_usesDefaults() throws Exception {
            OutboxStatusApiResponse apiResponse = MonitoringApiFixtures.outboxStatusApiResponse();

            given(queryMapper.toSearchParams(null, null))
                    .willReturn(OutboxStatusSearchParams.defaultParams());
            given(getOutboxStatusUseCase.execute(any(OutboxStatusSearchParams.class)))
                    .willReturn(MonitoringApiFixtures.outboxStatusResponse());
            given(queryMapper.toResponse(any(OutboxStatusResponse.class))).willReturn(apiResponse);

            mockMvc.perform(get("/api/v1/monitoring/outbox-status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.download").exists())
                    .andExpect(jsonPath("$.data.transform").exists())
                    .andExpect(jsonPath("$.data.checkedAt").exists());
        }
    }
}
