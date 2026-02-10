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
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MultipartUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.SessionQueryApiMapper;
import com.ryuqq.fileflow.application.session.port.in.query.GetMultipartUploadSessionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;

/**
 * MultipartUploadSessionQueryController REST Docs 테스트.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("MultipartUploadSessionQueryController REST Docs 테스트")
@WebMvcTest(MultipartUploadSessionQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class MultipartUploadSessionQueryControllerRestDocsTest extends RestDocsTestSupport {

    @MockBean private GetMultipartUploadSessionUseCase getUseCase;

    @MockBean private SessionQueryApiMapper queryMapper;

    @Nested
    @DisplayName("멀티파트 업로드 세션 상세 조회 API")
    class GetMultipartUploadSessionTest {

        @Test
        @DisplayName("GET /api/v1/sessions/multipart/{sessionId} - 멀티파트 업로드 세션 조회 성공")
        void getMultipartUploadSession_success() throws Exception {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;
            MultipartUploadSessionApiResponse apiResponse =
                    SessionApiFixtures.multipartUploadSessionApiResponse();

            given(getUseCase.execute(any(String.class)))
                    .willReturn(SessionApiFixtures.multipartUploadSessionResponse());
            given(
                            queryMapper.toResponse(
                                    any(
                                            com.ryuqq.fileflow.application.session.dto.response
                                                    .MultipartUploadSessionResponse.class)))
                    .willReturn(apiResponse);

            // when & then
            mockMvc.perform(get("/api/v1/sessions/multipart/{sessionId}", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.sessionId").value(apiResponse.sessionId()))
                    .andExpect(jsonPath("$.data.uploadId").value(apiResponse.uploadId()))
                    .andExpect(jsonPath("$.data.s3Key").value(apiResponse.s3Key()))
                    .andExpect(jsonPath("$.data.partSize").value(apiResponse.partSize()))
                    .andExpect(
                            jsonPath("$.data.completedPartCount")
                                    .value(apiResponse.completedPartCount()))
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("sessionId").description("세션 ID")),
                                    responseFields(
                                            fieldWithPath("data.sessionId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("세션 ID"),
                                            fieldWithPath("data.uploadId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("S3 멀티파트 업로드 ID"),
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
                                            fieldWithPath("data.partSize")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("파트 크기 (bytes)"),
                                            fieldWithPath("data.status")
                                                    .type(JsonFieldType.STRING)
                                                    .description("세션 상태"),
                                            fieldWithPath("data.completedPartCount")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("완료된 파트 수"),
                                            fieldWithPath("data.completedParts")
                                                    .type(JsonFieldType.ARRAY)
                                                    .description("완료된 파트 목록"),
                                            fieldWithPath("data.completedParts[].partNumber")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("파트 번호"),
                                            fieldWithPath("data.completedParts[].etag")
                                                    .type(JsonFieldType.STRING)
                                                    .description("파트 ETag"),
                                            fieldWithPath("data.completedParts[].size")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("파트 크기 (bytes)"),
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
