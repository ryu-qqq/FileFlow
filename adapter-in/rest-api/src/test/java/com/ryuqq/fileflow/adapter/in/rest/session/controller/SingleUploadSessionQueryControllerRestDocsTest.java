package com.ryuqq.fileflow.adapter.in.rest.session.controller;

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
import com.ryuqq.fileflow.adapter.in.rest.session.SessionApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.SingleUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.SessionQueryApiMapper;
import com.ryuqq.fileflow.application.session.port.in.query.GetSingleUploadSessionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;

/**
 * SingleUploadSessionQueryController REST Docs 테스트.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("SingleUploadSessionQueryController REST Docs 테스트")
@WebMvcTest(SingleUploadSessionQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class SingleUploadSessionQueryControllerRestDocsTest extends RestDocsTestSupport {

    @MockBean private GetSingleUploadSessionUseCase getUseCase;

    @MockBean private SessionQueryApiMapper queryMapper;

    @Nested
    @DisplayName("단건 업로드 세션 상세 조회 API")
    class GetSingleUploadSessionTest {

        @Test
        @DisplayName("GET /api/v1/sessions/single/{sessionId} - 단건 업로드 세션 조회 성공")
        void getSingleUploadSession_success() throws Exception {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;
            SingleUploadSessionApiResponse apiResponse =
                    SessionApiFixtures.singleUploadSessionApiResponse();

            given(getUseCase.execute(any(String.class)))
                    .willReturn(SessionApiFixtures.singleUploadSessionResponse());
            given(
                            queryMapper.toResponse(
                                    any(
                                            com.ryuqq.fileflow.application.session.dto.response
                                                    .SingleUploadSessionResponse.class)))
                    .willReturn(apiResponse);

            // when & then
            mockMvc.perform(get("/api/v1/sessions/single/{sessionId}", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.sessionId").value(apiResponse.sessionId()))
                    .andExpect(jsonPath("$.data.presignedUrl").value(apiResponse.presignedUrl()))
                    .andExpect(jsonPath("$.data.s3Key").value(apiResponse.s3Key()))
                    .andExpect(jsonPath("$.data.bucket").value(apiResponse.bucket()))
                    .andExpect(jsonPath("$.data.status").value(apiResponse.status()))
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("sessionId").description("세션 ID")),
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
}
