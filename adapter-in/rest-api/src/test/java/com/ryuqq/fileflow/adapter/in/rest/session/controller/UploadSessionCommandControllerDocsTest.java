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
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import com.ryuqq.fileflow.application.session.port.in.command.CancelUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteMultipartUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteSingleUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.InitMultipartUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.InitSingleUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.MarkPartUploadedUseCase;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
@TestPropertySource(
        properties = {
            "api.endpoints.base-v1=/api/v1",
            "api.endpoints.upload-session.base=/upload-sessions",
            "api.endpoints.upload-session.single-init=/single",
            "api.endpoints.upload-session.multipart-init=/multipart",
            "api.endpoints.upload-session.single-complete=/{sessionId}/single/complete",
            "api.endpoints.upload-session.multipart-complete=/{sessionId}/multipart/complete",
            "api.endpoints.upload-session.parts=/{sessionId}/parts",
            "api.endpoints.upload-session.cancel=/{sessionId}/cancel"
        })
@Import(UploadSessionCommandControllerDocsTest.DocsTestConfig.class)
@DisplayName("UploadSessionCommandController REST Docs")
class UploadSessionCommandControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private InitSingleUploadUseCase initSingleUploadUseCase;
    @MockitoBean private InitMultipartUploadUseCase initMultipartUploadUseCase;
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
                        "550e8400-e29b-41d4-a716-446655440000",
                        "image.jpg",
                        1024000L,
                        "image/jpeg",
                        1L,
                        1L,
                        null,
                        "admin@test.com",
                        "PRODUCT");

        InitSingleUploadResponse response =
                InitSingleUploadResponse.of(
                        "session-123",
                        "https://s3.amazonaws.com/bucket/uploads/image.jpg?X-Amz-Signature=...",
                        Instant.now().plus(15, ChronoUnit.MINUTES),
                        "fileflow-bucket",
                        "uploads/image.jpg");

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
                                        fieldWithPath("idempotencyKey")
                                                .description("멱등성 키 (클라이언트 제공 UUID)"),
                                        fieldWithPath("fileName").description("파일명 (확장자 포함)"),
                                        fieldWithPath("fileSize").description("파일 크기 (bytes)"),
                                        fieldWithPath("contentType")
                                                .description("Content-Type (MIME 타입)"),
                                        fieldWithPath("tenantId").description("테넌트 ID"),
                                        fieldWithPath("organizationId").description("조직 ID"),
                                        fieldWithPath("userId")
                                                .description("사용자 ID (Customer 전용)")
                                                .optional(),
                                        fieldWithPath("userEmail")
                                                .description("사용자 이메일 (Admin/Seller 전용)")
                                                .optional(),
                                        fieldWithPath("uploadCategory")
                                                .description("업로드 카테고리 (Admin/Seller 필수)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.presignedUrl")
                                                .description("Presigned URL (15분 유효)"),
                                        fieldWithPath("data.expiresAt").description("세션 만료 시각"),
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
                        "large-file.zip",
                        104857600L,
                        "application/zip",
                        5242880L,
                        1L,
                        1L,
                        null,
                        "admin@test.com");

        List<InitMultipartUploadResponse.PartInfo> parts =
                List.of(
                        InitMultipartUploadResponse.PartInfo.of(
                                1, "https://s3.amazonaws.com/bucket/file?partNumber=1&..."),
                        InitMultipartUploadResponse.PartInfo.of(
                                2, "https://s3.amazonaws.com/bucket/file?partNumber=2&..."));

        InitMultipartUploadResponse response =
                InitMultipartUploadResponse.of(
                        "session-456",
                        "upload-id-xyz",
                        20,
                        5242880L,
                        Instant.now().plus(24, ChronoUnit.HOURS),
                        "fileflow-bucket",
                        "uploads/large-file.zip",
                        parts);

        given(initMultipartUploadUseCase.execute(any())).willReturn(response);

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
                                        fieldWithPath("fileName").description("파일명 (확장자 포함)"),
                                        fieldWithPath("fileSize").description("파일 크기 (bytes)"),
                                        fieldWithPath("contentType")
                                                .description("Content-Type (MIME 타입)"),
                                        fieldWithPath("partSize")
                                                .description("각 Part 크기 (bytes, 기본: 5MB)"),
                                        fieldWithPath("tenantId").description("테넌트 ID"),
                                        fieldWithPath("organizationId").description("조직 ID"),
                                        fieldWithPath("userId")
                                                .description("사용자 ID (Customer 전용)")
                                                .optional(),
                                        fieldWithPath("userEmail")
                                                .description("사용자 이메일 (Admin/Seller 전용)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.uploadId")
                                                .description("S3 Multipart Upload ID"),
                                        fieldWithPath("data.totalParts")
                                                .description("전체 Part 개수"),
                                        fieldWithPath("data.partSize")
                                                .description("각 Part 크기 (bytes)"),
                                        fieldWithPath("data.expiresAt")
                                                .description("세션 만료 시각 (24시간)"),
                                        fieldWithPath("data.bucket").description("S3 버킷명"),
                                        fieldWithPath("data.key").description("S3 객체 키"),
                                        fieldWithPath("data.parts").description("Part 정보 목록"),
                                        fieldWithPath("data.parts[].partNumber")
                                                .description("Part 번호 (1-based)"),
                                        fieldWithPath("data.parts[].presignedUrl")
                                                .description("Part Presigned URL"),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("PATCH /api/v1/upload-sessions/{sessionId}/single/complete - 단일 업로드 완료 API 문서")
    void completeSingleUpload() throws Exception {
        // given
        String sessionId = "session-123";
        CompleteSingleUploadApiRequest request =
                new CompleteSingleUploadApiRequest("\"d41d8cd98f00b204e9800998ecf8427e\"");

        CompleteSingleUploadResponse response =
                CompleteSingleUploadResponse.of(
                        sessionId,
                        "COMPLETED",
                        "fileflow-bucket",
                        "uploads/image.jpg",
                        "\"d41d8cd98f00b204e9800998ecf8427e\"",
                        Instant.now());

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
                                pathParameters(
                                        parameterWithName("sessionId").description("업로드 세션 ID")),
                                requestFields(
                                        fieldWithPath("etag").description("S3가 반환한 ETag")),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.status")
                                                .description("세션 상태 (COMPLETED)"),
                                        fieldWithPath("data.bucket").description("S3 버킷명"),
                                        fieldWithPath("data.key").description("S3 객체 키"),
                                        fieldWithPath("data.etag").description("S3 ETag"),
                                        fieldWithPath("data.completedAt").description("완료 시각"),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("PATCH /api/v1/upload-sessions/{sessionId}/multipart/complete - Multipart 업로드 완료 API 문서")
    void completeMultipartUpload() throws Exception {
        // given
        String sessionId = "session-456";

        List<CompleteMultipartUploadResponse.CompletedPartInfo> completedParts =
                List.of(
                        CompleteMultipartUploadResponse.CompletedPartInfo.of(
                                1,
                                "\"etag1\"",
                                5242880L,
                                Instant.now().minus(10, ChronoUnit.MINUTES)),
                        CompleteMultipartUploadResponse.CompletedPartInfo.of(
                                2,
                                "\"etag2\"",
                                5242880L,
                                Instant.now().minus(5, ChronoUnit.MINUTES)));

        CompleteMultipartUploadResponse response =
                CompleteMultipartUploadResponse.of(
                        sessionId,
                        "COMPLETED",
                        "fileflow-bucket",
                        "uploads/large-file.zip",
                        "upload-id-xyz",
                        2,
                        completedParts,
                        Instant.now());

        given(completeMultipartUploadUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/upload-sessions/{sessionId}/multipart/complete", sessionId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "upload-session-complete-multipart",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("sessionId").description("업로드 세션 ID")),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.status")
                                                .description("세션 상태 (COMPLETED)"),
                                        fieldWithPath("data.bucket").description("S3 버킷명"),
                                        fieldWithPath("data.key").description("S3 객체 키"),
                                        fieldWithPath("data.uploadId")
                                                .description("S3 Multipart Upload ID"),
                                        fieldWithPath("data.totalParts")
                                                .description("전체 Part 개수"),
                                        fieldWithPath("data.completedParts")
                                                .description("완료된 Part 목록"),
                                        fieldWithPath("data.completedParts[].partNumber")
                                                .description("Part 번호"),
                                        fieldWithPath("data.completedParts[].etag")
                                                .description("Part ETag"),
                                        fieldWithPath("data.completedParts[].size")
                                                .description("Part 크기 (bytes)"),
                                        fieldWithPath("data.completedParts[].uploadedAt")
                                                .description("Part 업로드 시각"),
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
                new MarkPartUploadedApiRequest(1, "\"d41d8cd98f00b204e9800998ecf8427e\"", 5242880L);

        MarkPartUploadedResponse response =
                MarkPartUploadedResponse.of(
                        sessionId, 1, "\"d41d8cd98f00b204e9800998ecf8427e\"", Instant.now());

        given(markPartUploadedUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/upload-sessions/{sessionId}/parts", sessionId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "upload-session-mark-part-uploaded",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("sessionId").description("업로드 세션 ID")),
                                requestFields(
                                        fieldWithPath("partNumber")
                                                .description("Part 번호 (1-based)"),
                                        fieldWithPath("etag").description("S3가 반환한 Part ETag"),
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
        String sessionId = "session-123";

        CancelUploadSessionResponse response =
                CancelUploadSessionResponse.of(
                        sessionId, "CANCELLED", "fileflow-bucket", "uploads/image.jpg");

        given(cancelUploadSessionUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/upload-sessions/{sessionId}/cancel", sessionId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "upload-session-cancel",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("sessionId").description("업로드 세션 ID")),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.status")
                                                .description("세션 상태 (CANCELLED)"),
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
