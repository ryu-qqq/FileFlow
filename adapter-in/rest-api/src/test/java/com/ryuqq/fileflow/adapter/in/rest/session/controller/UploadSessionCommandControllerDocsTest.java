package com.ryuqq.fileflow.adapter.in.rest.session.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitMultipartUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.MarkPartUploadedApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.UploadSessionApiMapper;
import com.ryuqq.fileflow.application.session.dto.response.*;
import com.ryuqq.fileflow.application.session.port.in.command.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * UploadSessionCommandController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(UploadSessionCommandController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.endpoints.base-v1=/api/v1")
@Import(UploadSessionCommandControllerDocsTest.DocsTestConfig.class)
@DisplayName("UploadSessionCommandController REST Docs")
class UploadSessionCommandControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private InitSingleUploadUseCase initSingleUploadUseCase;
    @MockitoBean private InitMultipartUploadUseCase initMultiPartUploadUseCase;
    @MockitoBean private CompleteSingleUploadUseCase completeSingleUploadUseCase;
    @MockitoBean private CompleteMultipartUploadUseCase completeMultipartUploadUseCase;
    @MockitoBean private MarkPartUploadedUseCase markPartUploadedUseCase;
    @MockitoBean private CancelUploadSessionUseCase cancelUploadSessionUseCase;

    @Test
    @DisplayName("POST /api/v1/upload-sessions/single - 단일 업로드 세션 초기화 API 문서")
    void initSingleUpload() throws Exception {
        // given
        InitSingleUploadApiRequest request =
                new InitSingleUploadApiRequest(
                        "idempotency-key-123",
                        "example.jpg",
                        1024000L,
                        "image/jpeg",
                        1L,
                        1L,
                        null,
                        "user@example.com");

        InitSingleUploadResponse response =
                new InitSingleUploadResponse(
                        "session-123",
                        "https://s3.amazonaws.com/bucket/upload?signature=...",
                        LocalDateTime.now().plusHours(1),
                        "my-bucket",
                        "uploads/example.jpg");

        given(initSingleUploadUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/upload-sessions/single")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(
                        document(
                                "upload-session-init-single",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("idempotencyKey").description("멱등성 키"),
                                        fieldWithPath("fileName").description("파일명"),
                                        fieldWithPath("fileSize").description("파일 크기 (bytes)"),
                                        fieldWithPath("contentType").description("Content-Type"),
                                        fieldWithPath("tenantId").description("테넌트 ID"),
                                        fieldWithPath("organizationId").description("조직 ID"),
                                        fieldWithPath("userId").description("사용자 ID").optional(),
                                        fieldWithPath("userEmail")
                                                .description("사용자 이메일")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.presignedUrl")
                                                .description("Presigned Upload URL"),
                                        fieldWithPath("data.expiresAt").description("URL 만료 시각"),
                                        fieldWithPath("data.bucket").description("S3 버킷명"),
                                        fieldWithPath("data.key").description("S3 객체 키"),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("POST /api/v1/upload-sessions/multipart - Multipart 업로드 세션 초기화 API 문서")
    void initMultipartUpload() throws Exception {
        // given
        InitMultipartUploadApiRequest request =
                new InitMultipartUploadApiRequest(
                        "large-file.mp4",
                        104857600L,
                        "video/mp4",
                        20971520L,
                        1L,
                        1L,
                        null,
                        "user@example.com");

        InitMultipartUploadResponse response =
                new InitMultipartUploadResponse(
                        "session-456",
                        "upload-id-789",
                        2,
                        20971520L,
                        LocalDateTime.now().plusHours(1),
                        "my-bucket",
                        "uploads/large-file.mp4",
                        List.of(
                                new InitMultipartUploadResponse.PartInfo(
                                        1,
                                        "https://s3.amazonaws.com/bucket/upload?partNumber=1&signature=..."),
                                new InitMultipartUploadResponse.PartInfo(
                                        2,
                                        "https://s3.amazonaws.com/bucket/upload?partNumber=2&signature=")));

        given(initMultiPartUploadUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/upload-sessions/multipart")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(
                        document(
                                "upload-session-init-multipart",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("fileName").description("파일명"),
                                        fieldWithPath("fileSize").description("파일 크기 (bytes)"),
                                        fieldWithPath("contentType").description("Content-Type"),
                                        fieldWithPath("partSize").description("각 Part 크기 (bytes)"),
                                        fieldWithPath("tenantId").description("테넌트 ID"),
                                        fieldWithPath("organizationId").description("조직 ID"),
                                        fieldWithPath("userId").description("사용자 ID").optional(),
                                        fieldWithPath("userEmail")
                                                .description("사용자 이메일")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.uploadId").description("S3 Upload ID"),
                                        fieldWithPath("data.totalParts").description("전체 Part 개수"),
                                        fieldWithPath("data.partSize")
                                                .description("각 Part 크기 (bytes)"),
                                        fieldWithPath("data.expiresAt").description("URL 만료 시각"),
                                        fieldWithPath("data.bucket").description("S3 버킷명"),
                                        fieldWithPath("data.key").description("S3 객체 키"),
                                        fieldWithPath("data.parts").description("Part 정보 목록"),
                                        fieldWithPath("data.parts[].partNumber")
                                                .description("Part 번호"),
                                        fieldWithPath("data.parts[].presignedUrl")
                                                .description("Presigned Upload URL"),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("PATCH /api/v1/upload-sessions/{sessionId}/single/complete - 단일 업로드 완료 API 문서")
    void completeSingleUpload() throws Exception {
        // given
        String sessionId = "session-123";
        CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest("etag-abc123");

        CompleteSingleUploadResponse response =
                new CompleteSingleUploadResponse(
                        sessionId,
                        "COMPLETED",
                        "my-bucket",
                        "uploads/example.jpg",
                        "etag-abc123",
                        LocalDateTime.now());

        given(completeSingleUploadUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/upload-sessions/{sessionId}/single/complete", sessionId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "upload-session-complete-single",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("sessionId").description("세션 ID")),
                                requestFields(fieldWithPath("etag").description("S3 ETag")),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.status").description("세션 상태"),
                                        fieldWithPath("data.bucket").description("S3 버킷명"),
                                        fieldWithPath("data.key").description("S3 객체 키"),
                                        fieldWithPath("data.etag").description("S3 ETag"),
                                        fieldWithPath("data.completedAt").description("완료 시각"),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName(
            "PATCH /api/v1/upload-sessions/{sessionId}/multipart/complete - Multipart 업로드 완료 API"
                    + " 문서")
    void completeMultipartUpload() throws Exception {
        // given
        String sessionId = "session-456";

        CompleteMultipartUploadResponse response =
                new CompleteMultipartUploadResponse(
                        sessionId,
                        "COMPLETED",
                        "my-bucket",
                        "uploads/large-file.mp4",
                        "upload-id-789",
                        2,
                        List.of(
                                new CompleteMultipartUploadResponse.CompletedPartInfo(
                                        1, "etag-part1", 20971520L, LocalDateTime.now()),
                                new CompleteMultipartUploadResponse.CompletedPartInfo(
                                        2, "etag-part2", 20971520L, LocalDateTime.now())),
                        LocalDateTime.now());

        given(completeMultipartUploadUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/upload-sessions/{sessionId}/multipart/complete", sessionId))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "upload-session-complete-multipart",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("sessionId").description("세션 ID")),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.status").description("세션 상태"),
                                        fieldWithPath("data.bucket").description("S3 버킷명"),
                                        fieldWithPath("data.key").description("S3 객체 키"),
                                        fieldWithPath("data.uploadId").description("S3 Upload ID"),
                                        fieldWithPath("data.totalParts").description("전체 Part 개수"),
                                        fieldWithPath("data.completedParts")
                                                .description("완료된 Part 목록"),
                                        fieldWithPath("data.completedParts[].partNumber")
                                                .description("Part 번호"),
                                        fieldWithPath("data.completedParts[].etag")
                                                .description("Part ETag"),
                                        fieldWithPath("data.completedParts[].size")
                                                .description("Part 크기 (bytes)"),
                                        fieldWithPath("data.completedParts[].uploadedAt")
                                                .description("업로드 시각"),
                                        fieldWithPath("data.completedAt").description("완료 시각"),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("PATCH /api/v1/upload-sessions/{sessionId}/parts - Part 업로드 완료 표시 API 문서")
    void markPartUploaded() throws Exception {
        // given
        String sessionId = "session-456";
        MarkPartUploadedApiRequest request =
                new MarkPartUploadedApiRequest(1, "etag-part1", 20971520L);

        MarkPartUploadedResponse response =
                new MarkPartUploadedResponse(sessionId, 1, "etag-part1", LocalDateTime.now());

        given(markPartUploadedUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/upload-sessions/{sessionId}/parts", sessionId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "upload-session-mark-part",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("sessionId").description("세션 ID")),
                                requestFields(
                                        fieldWithPath("partNumber").description("Part 번호"),
                                        fieldWithPath("etag").description("Part ETag"),
                                        fieldWithPath("size").description("Part 크기 (bytes)")),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.partNumber").description("Part 번호"),
                                        fieldWithPath("data.etag").description("Part ETag"),
                                        fieldWithPath("data.uploadedAt").description("업로드 시각"),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("PATCH /api/v1/upload-sessions/{sessionId}/cancel - 업로드 세션 취소 API 문서")
    void cancelUploadSession() throws Exception {
        // given
        String sessionId = "session-789";

        CancelUploadSessionResponse response =
                new CancelUploadSessionResponse(
                        sessionId, "CANCELLED", "my-bucket", "uploads/file.jpg");

        given(cancelUploadSessionUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/upload-sessions/{sessionId}/cancel", sessionId))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "upload-session-cancel",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("sessionId").description("세션 ID")),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.status").description("세션 상태"),
                                        fieldWithPath("data.bucket").description("S3 버킷명"),
                                        fieldWithPath("data.key").description("S3 객체 키"),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @TestConfiguration
    static class DocsTestConfig {
        @Bean
        com.ryuqq.fileflow.adapter.in.rest.common.error.ErrorMapperRegistry errorMapperRegistry() {
            return mock(com.ryuqq.fileflow.adapter.in.rest.common.error.ErrorMapperRegistry.class);
        }

        @Bean
        UploadSessionApiMapper uploadSessionApiMapper() {
            return new UploadSessionApiMapper();
        }
    }
}
