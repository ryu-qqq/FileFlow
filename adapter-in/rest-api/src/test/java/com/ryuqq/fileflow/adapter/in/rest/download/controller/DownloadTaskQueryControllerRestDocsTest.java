package com.ryuqq.fileflow.adapter.in.rest.download.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.adapter.in.rest.download.DownloadTaskApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.DownloadTaskApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.download.mapper.DownloadTaskQueryApiMapper;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.application.download.port.in.query.GetDownloadTaskUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;

/**
 * DownloadTaskQueryController REST Docs 테스트.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("DownloadTaskQueryController REST Docs 테스트")
@WebMvcTest(DownloadTaskQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class DownloadTaskQueryControllerRestDocsTest extends RestDocsTestSupport {

    @MockBean private GetDownloadTaskUseCase getUseCase;

    @MockBean private DownloadTaskQueryApiMapper queryMapper;

    @Nested
    @DisplayName("다운로드 작업 상세 조회 API")
    class GetDownloadTaskTest {

        @Test
        @DisplayName("GET /api/v1/download-tasks/{downloadTaskId} - 다운로드 작업 조회 성공")
        void getDownloadTask_success() throws Exception {
            // given
            String downloadTaskId = DownloadTaskApiFixtures.DOWNLOAD_TASK_ID;
            DownloadTaskApiResponse apiResponse = DownloadTaskApiFixtures.downloadTaskApiResponse();

            given(getUseCase.execute(any(String.class)))
                    .willReturn(DownloadTaskApiFixtures.downloadTaskResponse());
            given(queryMapper.toResponse(any(DownloadTaskResponse.class))).willReturn(apiResponse);

            // when & then
            mockMvc.perform(get("/api/v1/download-tasks/{downloadTaskId}", downloadTaskId))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.downloadTaskId").value(apiResponse.downloadTaskId()))
                    .andExpect(jsonPath("$.data.sourceUrl").value(apiResponse.sourceUrl()))
                    .andExpect(jsonPath("$.data.s3Key").value(apiResponse.s3Key()))
                    .andExpect(jsonPath("$.data.bucket").value(apiResponse.bucket()))
                    .andExpect(jsonPath("$.data.status").value(apiResponse.status()))
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("downloadTaskId")
                                                    .description("다운로드 작업 ID")),
                                    responseFields(
                                            fieldWithPath("data.downloadTaskId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("다운로드 작업 ID"),
                                            fieldWithPath("data.sourceUrl")
                                                    .type(JsonFieldType.STRING)
                                                    .description("다운로드 소스 URL"),
                                            fieldWithPath("data.s3Key")
                                                    .type(JsonFieldType.STRING)
                                                    .description("S3 객체 키"),
                                            fieldWithPath("data.bucket")
                                                    .type(JsonFieldType.STRING)
                                                    .description("S3 버킷명"),
                                            fieldWithPath("data.accessType")
                                                    .type(JsonFieldType.STRING)
                                                    .description("접근 유형"),
                                            fieldWithPath("data.purpose")
                                                    .type(JsonFieldType.STRING)
                                                    .description("파일 용도"),
                                            fieldWithPath("data.source")
                                                    .type(JsonFieldType.STRING)
                                                    .description("요청 서비스명"),
                                            fieldWithPath("data.status")
                                                    .type(JsonFieldType.STRING)
                                                    .description("작업 상태"),
                                            fieldWithPath("data.retryCount")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("재시도 횟수"),
                                            fieldWithPath("data.maxRetries")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("최대 재시도 횟수"),
                                            fieldWithPath("data.callbackUrl")
                                                    .type(JsonFieldType.STRING)
                                                    .description("완료 콜백 URL")
                                                    .optional(),
                                            fieldWithPath("data.lastError")
                                                    .type(JsonFieldType.STRING)
                                                    .description("마지막 에러 메시지")
                                                    .optional(),
                                            fieldWithPath("data.createdAt")
                                                    .type(JsonFieldType.STRING)
                                                    .description("생성 시각 (ISO 8601)"),
                                            fieldWithPath("data.startedAt")
                                                    .type(JsonFieldType.STRING)
                                                    .description("시작 시각 (ISO 8601)")
                                                    .optional(),
                                            fieldWithPath("data.completedAt")
                                                    .type(JsonFieldType.STRING)
                                                    .description("완료 시각 (ISO 8601)")
                                                    .optional(),
                                            fieldWithPath("timestamp")
                                                    .type(JsonFieldType.STRING)
                                                    .description("응답 시각")
                                                    .optional(),
                                            fieldWithPath("requestId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("요청 ID")
                                                    .optional())));
        }
    }
}
