package com.ryuqq.fileflow.adapter.in.rest.download.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.adapter.in.rest.download.DownloadTaskApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.command.CreateDownloadTaskApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.DownloadTaskApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.download.mapper.DownloadTaskCommandApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.download.mapper.DownloadTaskQueryApiMapper;
import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.application.download.port.in.command.CreateDownloadTaskUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

/**
 * DownloadTaskCommandController REST Docs 테스트.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("DownloadTaskCommandController REST Docs 테스트")
@WebMvcTest(DownloadTaskCommandController.class)
@AutoConfigureMockMvc(addFilters = false)
class DownloadTaskCommandControllerRestDocsTest extends RestDocsTestSupport {

    @MockBean private CreateDownloadTaskUseCase createUseCase;

    @MockBean private DownloadTaskCommandApiMapper commandMapper;

    @MockBean private DownloadTaskQueryApiMapper queryMapper;

    @Nested
    @DisplayName("다운로드 작업 생성 API")
    class CreateDownloadTaskTest {

        @Test
        @DisplayName("POST /api/v1/download-tasks - 다운로드 작업 생성 성공")
        void createDownloadTask_success() throws Exception {
            // given
            CreateDownloadTaskApiRequest request =
                    DownloadTaskApiFixtures.createDownloadTaskRequest();
            DownloadTaskApiResponse apiResponse = DownloadTaskApiFixtures.downloadTaskApiResponse();

            given(commandMapper.toCommand(any(CreateDownloadTaskApiRequest.class)))
                    .willReturn(
                            new CreateDownloadTaskCommand(
                                    request.sourceUrl(),
                                    request.s3Key(),
                                    request.bucket(),
                                    request.accessType(),
                                    request.purpose(),
                                    request.source(),
                                    request.callbackUrl()));
            given(createUseCase.execute(any(CreateDownloadTaskCommand.class)))
                    .willReturn(DownloadTaskApiFixtures.downloadTaskResponse());
            given(queryMapper.toResponse(any(DownloadTaskResponse.class))).willReturn(apiResponse);

            // when & then
            mockMvc.perform(
                            post("/api/v1/download-tasks")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(
                            jsonPath("$.data.downloadTaskId").value(apiResponse.downloadTaskId()))
                    .andExpect(jsonPath("$.data.sourceUrl").value(apiResponse.sourceUrl()))
                    .andExpect(jsonPath("$.data.s3Key").value(apiResponse.s3Key()))
                    .andExpect(jsonPath("$.data.bucket").value(apiResponse.bucket()))
                    .andExpect(jsonPath("$.data.status").value(apiResponse.status()))
                    .andDo(
                            document.document(
                                    requestFields(
                                            fieldWithPath("sourceUrl")
                                                    .type(JsonFieldType.STRING)
                                                    .description("다운로드 소스 URL"),
                                            fieldWithPath("s3Key")
                                                    .type(JsonFieldType.STRING)
                                                    .description("저장할 S3 객체 키"),
                                            fieldWithPath("bucket")
                                                    .type(JsonFieldType.STRING)
                                                    .description("S3 버킷명"),
                                            fieldWithPath("accessType")
                                                    .type(JsonFieldType.STRING)
                                                    .description(
                                                            "접근 유형 (PUBLIC: 공개, INTERNAL: 내부)"),
                                            fieldWithPath("purpose")
                                                    .type(JsonFieldType.STRING)
                                                    .description("파일 용도"),
                                            fieldWithPath("source")
                                                    .type(JsonFieldType.STRING)
                                                    .description("요청 서비스명"),
                                            fieldWithPath("callbackUrl")
                                                    .type(JsonFieldType.STRING)
                                                    .description("완료 콜백 URL")
                                                    .optional()),
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
