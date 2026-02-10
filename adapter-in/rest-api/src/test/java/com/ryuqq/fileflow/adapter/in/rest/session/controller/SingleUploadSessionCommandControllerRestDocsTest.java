package com.ryuqq.fileflow.adapter.in.rest.session.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.adapter.in.rest.session.SessionApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteSingleUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CreateSingleUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.SingleUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.SessionCommandApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.SessionQueryApiMapper;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteSingleUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CreateSingleUploadSessionUseCase;
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
 * SingleUploadSessionCommandController REST Docs 테스트.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("SingleUploadSessionCommandController REST Docs 테스트")
@WebMvcTest(SingleUploadSessionCommandController.class)
@AutoConfigureMockMvc(addFilters = false)
class SingleUploadSessionCommandControllerRestDocsTest extends RestDocsTestSupport {

    @MockBean private CreateSingleUploadSessionUseCase createUseCase;

    @MockBean private CompleteSingleUploadSessionUseCase completeUseCase;

    @MockBean private SessionCommandApiMapper commandMapper;

    @MockBean private SessionQueryApiMapper queryMapper;

    @Nested
    @DisplayName("단건 업로드 세션 생성 API")
    class CreateSingleUploadSessionTest {

        @Test
        @DisplayName("POST /api/v1/sessions/single - 단건 업로드 세션 생성 성공")
        void createSingleUploadSession_success() throws Exception {
            // given
            CreateSingleUploadSessionApiRequest request =
                    SessionApiFixtures.createSingleUploadSessionRequest();
            SingleUploadSessionApiResponse apiResponse =
                    SessionApiFixtures.singleUploadSessionApiResponse();

            given(commandMapper.toCommand(any(CreateSingleUploadSessionApiRequest.class)))
                    .willReturn(
                            SessionApiFixtures.singleUploadSessionResponse() != null
                                    ? new com.ryuqq.fileflow.application.session.dto.command
                                            .CreateSingleUploadSessionCommand(
                                            request.fileName(),
                                            request.contentType(),
                                            request.accessType(),
                                            request.purpose(),
                                            request.source())
                                    : null);
            given(createUseCase.execute(any()))
                    .willReturn(SessionApiFixtures.singleUploadSessionResponse());
            given(
                            queryMapper.toResponse(
                                    any(
                                            com.ryuqq.fileflow.application.session.dto.response
                                                    .SingleUploadSessionResponse.class)))
                    .willReturn(apiResponse);

            // when & then
            mockMvc.perform(
                            post("/api/v1/sessions/single")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.sessionId").value(apiResponse.sessionId()))
                    .andExpect(jsonPath("$.data.presignedUrl").value(apiResponse.presignedUrl()))
                    .andExpect(jsonPath("$.data.s3Key").value(apiResponse.s3Key()))
                    .andDo(
                            document.document(
                                    requestFields(
                                            fieldWithPath("fileName")
                                                    .type(JsonFieldType.STRING)
                                                    .description("원본 파일명"),
                                            fieldWithPath("contentType")
                                                    .type(JsonFieldType.STRING)
                                                    .description("MIME 타입"),
                                            fieldWithPath("accessType")
                                                    .type(JsonFieldType.STRING)
                                                    .description(
                                                            "접근 유형 (PUBLIC: 공개, INTERNAL: 내부)"),
                                            fieldWithPath("purpose")
                                                    .type(JsonFieldType.STRING)
                                                    .description("파일 용도"),
                                            fieldWithPath("source")
                                                    .type(JsonFieldType.STRING)
                                                    .description("요청 서비스명")),
                                    responseFields(
                                            fieldWithPath("data.sessionId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("세션 ID"),
                                            fieldWithPath("data.presignedUrl")
                                                    .type(JsonFieldType.STRING)
                                                    .description("Presigned Upload URL"),
                                            fieldWithPath("data.s3Key")
                                                    .type(JsonFieldType.STRING)
                                                    .description("S3 객체 키"),
                                            fieldWithPath("data.bucket")
                                                    .type(JsonFieldType.STRING)
                                                    .description("S3 버킷명"),
                                            fieldWithPath("data.accessType")
                                                    .type(JsonFieldType.STRING)
                                                    .description("접근 유형"),
                                            fieldWithPath("data.fileName")
                                                    .type(JsonFieldType.STRING)
                                                    .description("원본 파일명"),
                                            fieldWithPath("data.contentType")
                                                    .type(JsonFieldType.STRING)
                                                    .description("MIME 타입"),
                                            fieldWithPath("data.status")
                                                    .type(JsonFieldType.STRING)
                                                    .description("세션 상태"),
                                            fieldWithPath("data.expiresAt")
                                                    .type(JsonFieldType.STRING)
                                                    .description("만료 시각 (ISO 8601)"),
                                            fieldWithPath("data.createdAt")
                                                    .type(JsonFieldType.STRING)
                                                    .description("생성 시각 (ISO 8601)"),
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

    @Nested
    @DisplayName("단건 업로드 세션 완료 API")
    class CompleteSingleUploadSessionTest {

        @Test
        @DisplayName("POST /api/v1/sessions/single/{sessionId}/complete - 단건 업로드 세션 완료 성공")
        void completeSingleUploadSession_success() throws Exception {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;
            CompleteSingleUploadSessionApiRequest request =
                    SessionApiFixtures.completeSingleUploadSessionRequest();

            given(
                            commandMapper.toCommand(
                                    any(String.class),
                                    any(CompleteSingleUploadSessionApiRequest.class)))
                    .willReturn(
                            new com.ryuqq.fileflow.application.session.dto.command
                                    .CompleteSingleUploadSessionCommand(
                                    sessionId, request.fileSize(), request.etag()));
            willDoNothing().given(completeUseCase).execute(any());

            // when & then
            mockMvc.perform(
                            post("/api/v1/sessions/single/{sessionId}/complete", sessionId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("sessionId").description("세션 ID")),
                                    requestFields(
                                            fieldWithPath("fileSize")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("파일 크기 (bytes)"),
                                            fieldWithPath("etag")
                                                    .type(JsonFieldType.STRING)
                                                    .description("S3 ETag")),
                                    responseFields(
                                            fieldWithPath("data")
                                                    .type(JsonFieldType.NULL)
                                                    .description("응답 데이터 (없음)"),
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
