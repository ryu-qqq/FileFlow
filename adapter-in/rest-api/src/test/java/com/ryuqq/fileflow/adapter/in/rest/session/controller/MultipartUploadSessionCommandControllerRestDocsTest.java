package com.ryuqq.fileflow.adapter.in.rest.session.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
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
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.AddCompletedPartApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteMultipartUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CreateMultipartUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MultipartUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.PresignedPartUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.SessionCommandApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.SessionQueryApiMapper;
import com.ryuqq.fileflow.application.session.dto.command.AbortMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.AddCompletedPartCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.GeneratePresignedPartUrlCommand;
import com.ryuqq.fileflow.application.session.port.in.command.AbortMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.AddCompletedPartUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CreateMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.GeneratePresignedPartUrlUseCase;
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
 * MultipartUploadSessionCommandController REST Docs 테스트.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("MultipartUploadSessionCommandController REST Docs 테스트")
@WebMvcTest(MultipartUploadSessionCommandController.class)
@AutoConfigureMockMvc(addFilters = false)
class MultipartUploadSessionCommandControllerRestDocsTest extends RestDocsTestSupport {

    @MockBean private CreateMultipartUploadSessionUseCase createUseCase;

    @MockBean private GeneratePresignedPartUrlUseCase generatePresignedPartUrlUseCase;

    @MockBean private AddCompletedPartUseCase addCompletedPartUseCase;

    @MockBean private CompleteMultipartUploadSessionUseCase completeUseCase;

    @MockBean private AbortMultipartUploadSessionUseCase abortUseCase;

    @MockBean private SessionCommandApiMapper commandMapper;

    @MockBean private SessionQueryApiMapper queryMapper;

    @Nested
    @DisplayName("멀티파트 업로드 세션 생성 API")
    class CreateMultipartUploadSessionTest {

        @Test
        @DisplayName("POST /api/v1/sessions/multipart - 멀티파트 업로드 세션 생성 성공")
        void createMultipartUploadSession_success() throws Exception {
            // given
            CreateMultipartUploadSessionApiRequest request =
                    SessionApiFixtures.createMultipartUploadSessionRequest();
            MultipartUploadSessionApiResponse apiResponse =
                    SessionApiFixtures.multipartUploadSessionApiResponse();

            given(commandMapper.toCommand(any(CreateMultipartUploadSessionApiRequest.class)))
                    .willReturn(
                            new CreateMultipartUploadSessionCommand(
                                    request.fileName(),
                                    request.contentType(),
                                    request.accessType(),
                                    request.partSize(),
                                    request.purpose(),
                                    request.source()));
            given(createUseCase.execute(any()))
                    .willReturn(SessionApiFixtures.multipartUploadSessionResponse());
            given(
                            queryMapper.toResponse(
                                    any(
                                            com.ryuqq.fileflow.application.session.dto.response
                                                    .MultipartUploadSessionResponse.class)))
                    .willReturn(apiResponse);

            // when & then
            mockMvc.perform(
                            post("/api/v1/sessions/multipart")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.sessionId").value(apiResponse.sessionId()))
                    .andExpect(jsonPath("$.data.uploadId").value(apiResponse.uploadId()))
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
                                            fieldWithPath("partSize")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("파트 크기 (bytes)"),
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

    @Nested
    @DisplayName("파트별 Presigned URL 발급 API")
    class GeneratePresignedPartUrlTest {

        @Test
        @DisplayName(
                "GET /api/v1/sessions/multipart/{sessionId}/parts/{partNumber}/presigned-url"
                        + " - Presigned URL 발급 성공")
        void generatePresignedPartUrl_success() throws Exception {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;
            int partNumber = 1;
            PresignedPartUrlApiResponse apiResponse =
                    SessionApiFixtures.presignedPartUrlApiResponse();

            given(commandMapper.toCommand(anyString(), anyInt()))
                    .willReturn(new GeneratePresignedPartUrlCommand(sessionId, partNumber));
            given(generatePresignedPartUrlUseCase.execute(any()))
                    .willReturn(SessionApiFixtures.presignedPartUrlResponse());
            given(
                            queryMapper.toResponse(
                                    any(
                                            com.ryuqq.fileflow.application.session.dto.response
                                                    .PresignedPartUrlResponse.class)))
                    .willReturn(apiResponse);

            // when & then
            mockMvc.perform(
                            get(
                                    "/api/v1/sessions/multipart/{sessionId}/parts/{partNumber}/presigned-url",
                                    sessionId,
                                    partNumber))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.presignedUrl").value(apiResponse.presignedUrl()))
                    .andExpect(jsonPath("$.data.partNumber").value(apiResponse.partNumber()))
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("sessionId").description("세션 ID"),
                                            parameterWithName("partNumber").description("파트 번호")),
                                    responseFields(
                                            fieldWithPath("data.presignedUrl")
                                                    .type(JsonFieldType.STRING)
                                                    .description("파트 업로드용 Presigned URL"),
                                            fieldWithPath("data.partNumber")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("파트 번호"),
                                            fieldWithPath("data.expiresInSeconds")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("Presigned URL 만료까지 남은 시간 (초)"),
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
    @DisplayName("파트 업로드 완료 기록 API")
    class AddCompletedPartTest {

        @Test
        @DisplayName("POST /api/v1/sessions/multipart/{sessionId}/parts - 파트 업로드 완료 기록 성공")
        void addCompletedPart_success() throws Exception {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;
            AddCompletedPartApiRequest request = SessionApiFixtures.addCompletedPartRequest();

            given(commandMapper.toCommand(anyString(), any(AddCompletedPartApiRequest.class)))
                    .willReturn(
                            new AddCompletedPartCommand(
                                    sessionId,
                                    request.partNumber(),
                                    request.etag(),
                                    request.size()));
            willDoNothing().given(addCompletedPartUseCase).execute(any());

            // when & then
            mockMvc.perform(
                            post("/api/v1/sessions/multipart/{sessionId}/parts", sessionId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("sessionId").description("세션 ID")),
                                    requestFields(
                                            fieldWithPath("partNumber")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("파트 번호"),
                                            fieldWithPath("etag")
                                                    .type(JsonFieldType.STRING)
                                                    .description("파트 ETag"),
                                            fieldWithPath("size")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("파트 크기 (bytes)")),
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

    @Nested
    @DisplayName("멀티파트 업로드 세션 완료 API")
    class CompleteMultipartUploadSessionTest {

        @Test
        @DisplayName(
                "POST /api/v1/sessions/multipart/{sessionId}/complete" + " - 멀티파트 업로드 세션 완료 성공")
        void completeMultipartUploadSession_success() throws Exception {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;
            CompleteMultipartUploadSessionApiRequest request =
                    SessionApiFixtures.completeMultipartUploadSessionRequest();

            given(
                            commandMapper.toCommand(
                                    anyString(),
                                    any(CompleteMultipartUploadSessionApiRequest.class)))
                    .willReturn(
                            new CompleteMultipartUploadSessionCommand(
                                    sessionId, request.totalFileSize(), request.etag()));
            willDoNothing().given(completeUseCase).execute(any());

            // when & then
            mockMvc.perform(
                            post("/api/v1/sessions/multipart/{sessionId}/complete", sessionId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("sessionId").description("세션 ID")),
                                    requestFields(
                                            fieldWithPath("totalFileSize")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("전체 파일 크기 (bytes)"),
                                            fieldWithPath("etag")
                                                    .type(JsonFieldType.STRING)
                                                    .description(
                                                            "S3 CompleteMultipartUpload ETag")),
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

    @Nested
    @DisplayName("멀티파트 업로드 세션 중단 API")
    class AbortMultipartUploadSessionTest {

        @Test
        @DisplayName("POST /api/v1/sessions/multipart/{sessionId}/abort" + " - 멀티파트 업로드 세션 중단 성공")
        void abortMultipartUploadSession_success() throws Exception {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;

            given(commandMapper.toAbortCommand(anyString()))
                    .willReturn(new AbortMultipartUploadSessionCommand(sessionId));
            willDoNothing().given(abortUseCase).execute(any());

            // when & then
            mockMvc.perform(post("/api/v1/sessions/multipart/{sessionId}/abort", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("sessionId").description("세션 ID")),
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
