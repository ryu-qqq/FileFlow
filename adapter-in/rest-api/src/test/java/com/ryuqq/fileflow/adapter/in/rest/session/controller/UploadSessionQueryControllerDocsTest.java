package com.ryuqq.fileflow.adapter.in.rest.session.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.UploadSessionApiMapper;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.application.common.dto.response.SliceResponse;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionResponse;
import com.ryuqq.fileflow.application.session.port.in.query.GetUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.query.GetUploadSessionsUseCase;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * UploadSessionQueryController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(UploadSessionQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        properties = {
            "api.endpoints.base-v1=/api/v1",
            "api.endpoints.upload-session.base=/upload-sessions",
            "api.endpoints.upload-session.by-id=/{sessionId}"
        })
@Import(UploadSessionQueryControllerDocsTest.DocsTestConfig.class)
@DisplayName("UploadSessionQueryController REST Docs")
class UploadSessionQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private GetUploadSessionUseCase getUploadSessionUseCase;
    @MockitoBean private GetUploadSessionsUseCase getUploadSessionsUseCase;

    @BeforeEach
    void setUpUserContext() {
        UserContext userContext =
                UserContext.seller(OrganizationId.generate(), "Test Org", "seller@test.com");
        UserContextHolder.set(userContext);
    }

    @AfterEach
    void clearUserContext() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("GET /api/v1/file/upload-sessions/{sessionId} - 업로드 세션 상세 조회 (단일) API 문서")
    void getUploadSession_single() throws Exception {
        // given
        String sessionId = "session-123";
        Instant now = Instant.now();

        UploadSessionDetailResponse response =
                UploadSessionDetailResponse.ofSingle(
                        sessionId,
                        "image.jpg",
                        1024000L,
                        "image/jpeg",
                        "COMPLETED",
                        "fileflow-bucket",
                        "uploads/image.jpg",
                        "\"d41d8cd98f00b204e9800998ecf8427e\"",
                        now.minus(1, ChronoUnit.HOURS),
                        now.plus(14, ChronoUnit.MINUTES),
                        now);

        given(getUploadSessionUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/file/upload-sessions/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "upload-session-get-single",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("sessionId").description("업로드 세션 ID")),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.fileName").description("파일명"),
                                        fieldWithPath("data.fileSize").description("파일 크기 (bytes)"),
                                        fieldWithPath("data.contentType")
                                                .description("Content-Type"),
                                        fieldWithPath("data.uploadType")
                                                .description("업로드 타입 (SINGLE)"),
                                        fieldWithPath("data.status").description("세션 상태"),
                                        fieldWithPath("data.bucket").description("S3 버킷명"),
                                        fieldWithPath("data.key").description("S3 객체 키"),
                                        fieldWithPath("data.uploadId")
                                                .description(
                                                        "S3 Multipart Upload ID (단일 업로드 시 null)")
                                                .optional(),
                                        fieldWithPath("data.totalParts")
                                                .description("전체 Part 개수 (단일 업로드 시 null)")
                                                .optional(),
                                        fieldWithPath("data.uploadedParts")
                                                .description("업로드 완료된 Part 개수 (단일 업로드 시 null)")
                                                .optional(),
                                        fieldWithPath("data.parts")
                                                .description("Part 정보 목록 (단일 업로드 시 null)")
                                                .optional(),
                                        fieldWithPath("data.etag").description("ETag").optional(),
                                        fieldWithPath("data.createdAt").description("생성 시각"),
                                        fieldWithPath("data.expiresAt").description("만료 시각"),
                                        fieldWithPath("data.completedAt")
                                                .description("완료 시각")
                                                .optional(),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/file/upload-sessions/{sessionId} - 업로드 세션 상세 조회 (Multipart) API 문서")
    void getUploadSession_multipart() throws Exception {
        // given
        String sessionId = "session-456";
        Instant now = Instant.now();

        List<UploadSessionDetailResponse.PartDetailResponse> parts =
                List.of(
                        UploadSessionDetailResponse.PartDetailResponse.of(
                                1, "\"etag1\"", 5242880L, now.minus(10, ChronoUnit.MINUTES)),
                        UploadSessionDetailResponse.PartDetailResponse.of(
                                2, "\"etag2\"", 5242880L, now.minus(5, ChronoUnit.MINUTES)));

        UploadSessionDetailResponse response =
                UploadSessionDetailResponse.ofMultipart(
                        sessionId,
                        "large-file.zip",
                        104857600L,
                        "application/zip",
                        "IN_PROGRESS",
                        "fileflow-bucket",
                        "uploads/large-file.zip",
                        "upload-id-xyz",
                        20,
                        2,
                        parts,
                        null,
                        now.minus(1, ChronoUnit.HOURS),
                        now.plus(23, ChronoUnit.HOURS),
                        null);

        given(getUploadSessionUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/file/upload-sessions/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "upload-session-get-multipart",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("sessionId").description("업로드 세션 ID")),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.sessionId").description("세션 ID"),
                                        fieldWithPath("data.fileName").description("파일명"),
                                        fieldWithPath("data.fileSize").description("파일 크기 (bytes)"),
                                        fieldWithPath("data.contentType")
                                                .description("Content-Type"),
                                        fieldWithPath("data.uploadType")
                                                .description("업로드 타입 (MULTIPART)"),
                                        fieldWithPath("data.status").description("세션 상태"),
                                        fieldWithPath("data.bucket").description("S3 버킷명"),
                                        fieldWithPath("data.key").description("S3 객체 키"),
                                        fieldWithPath("data.uploadId")
                                                .description("S3 Multipart Upload ID"),
                                        fieldWithPath("data.totalParts").description("전체 Part 개수"),
                                        fieldWithPath("data.uploadedParts")
                                                .description("업로드 완료된 Part 개수"),
                                        fieldWithPath("data.parts").description("Part 정보 목록"),
                                        fieldWithPath("data.parts[].partNumber")
                                                .description("Part 번호"),
                                        fieldWithPath("data.parts[].etag").description("Part ETag"),
                                        fieldWithPath("data.parts[].size")
                                                .description("Part 크기 (bytes)"),
                                        fieldWithPath("data.parts[].uploadedAt")
                                                .description("Part 업로드 시각"),
                                        fieldWithPath("data.etag")
                                                .description("ETag (완료 시)")
                                                .optional(),
                                        fieldWithPath("data.createdAt").description("생성 시각"),
                                        fieldWithPath("data.expiresAt").description("만료 시각"),
                                        fieldWithPath("data.completedAt")
                                                .description("완료 시각")
                                                .optional(),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/file/upload-sessions - 업로드 세션 목록 조회 API 문서")
    void getUploadSessions() throws Exception {
        // given
        Instant now = Instant.now();

        List<UploadSessionResponse> content =
                List.of(
                        UploadSessionResponse.of(
                                "session-123",
                                "image.jpg",
                                1024000L,
                                "image/jpeg",
                                "SINGLE",
                                "COMPLETED",
                                "fileflow-bucket",
                                "uploads/image.jpg",
                                now.minus(1, ChronoUnit.HOURS),
                                now.plus(14, ChronoUnit.MINUTES)),
                        UploadSessionResponse.of(
                                "session-456",
                                "large-file.zip",
                                104857600L,
                                "application/zip",
                                "MULTIPART",
                                "IN_PROGRESS",
                                "fileflow-bucket",
                                "uploads/large-file.zip",
                                now.minus(30, ChronoUnit.MINUTES),
                                now.plus(23, ChronoUnit.HOURS)));

        SliceResponse<UploadSessionResponse> response =
                new SliceResponse<>(content, 20, true, null);

        given(getUploadSessionsUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/v1/file/upload-sessions")
                                .param("page", "0")
                                .param("size", "20")
                                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "upload-session-list",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                queryParameters(
                                        parameterWithName("page")
                                                .description("페이지 번호 (0부터 시작)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기 (기본값 20, 최대 100)")
                                                .optional(),
                                        parameterWithName("status")
                                                .description(
                                                        "세션 상태 필터"
                                                            + " (PENDING/IN_PROGRESS/COMPLETED/EXPIRED/CANCELLED)")
                                                .optional(),
                                        parameterWithName("uploadType")
                                                .description("업로드 타입 필터 (SINGLE/MULTIPART)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.content").description("업로드 세션 목록"),
                                        fieldWithPath("data.content[].sessionId")
                                                .description("세션 ID"),
                                        fieldWithPath("data.content[].fileName").description("파일명"),
                                        fieldWithPath("data.content[].fileSize")
                                                .description("파일 크기 (bytes)"),
                                        fieldWithPath("data.content[].contentType")
                                                .description("Content-Type"),
                                        fieldWithPath("data.content[].uploadType")
                                                .description("업로드 타입 (SINGLE/MULTIPART)"),
                                        fieldWithPath("data.content[].status").description("세션 상태"),
                                        fieldWithPath("data.content[].bucket")
                                                .description("S3 버킷명"),
                                        fieldWithPath("data.content[].key").description("S3 객체 키"),
                                        fieldWithPath("data.content[].createdAt")
                                                .description("생성 시각"),
                                        fieldWithPath("data.content[].expiresAt")
                                                .description("만료 시각"),
                                        fieldWithPath("data.size").description("페이지 크기"),
                                        fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                        fieldWithPath("data.nextCursor")
                                                .description("다음 커서")
                                                .optional(),
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
