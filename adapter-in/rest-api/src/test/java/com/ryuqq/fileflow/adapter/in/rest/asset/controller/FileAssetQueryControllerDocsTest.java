package com.ryuqq.fileflow.adapter.in.rest.asset.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.asset.mapper.FileAssetApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetsUseCase;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import java.time.Instant;
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
 * FileAssetQueryController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(FileAssetQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.endpoints.base-v1=/api/v1")
@Import(FileAssetQueryControllerDocsTest.DocsTestConfig.class)
@DisplayName("FileAssetQueryController REST Docs")
class FileAssetQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private GetFileAssetUseCase getFileAssetUseCase;
    @MockitoBean private GetFileAssetsUseCase getFileAssetsUseCase;

    @BeforeEach
    void setUpUserContext() {
        UserContext userContext = UserContext.admin("admin@test.com");
        UserContextHolder.set(userContext);
    }

    @AfterEach
    void clearUserContext() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("GET /api/v1/file-assets/{id} - 파일 자산 단건 조회 API 문서")
    void getFileAsset() throws Exception {
        // given
        String fileAssetId = "asset-123";

        FileAssetResponse response =
                new FileAssetResponse(
                        fileAssetId,
                        "session-123",
                        "example.jpg",
                        1024000L,
                        "image/jpeg",
                        "IMAGE",
                        "bucket",
                        "path/example.jpg",
                        "etag-abc123",
                        "COMPLETED",
                        Instant.now(),
                        Instant.now());

        given(getFileAssetUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/file-assets/{id}", fileAssetId))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "file-asset-get",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description("파일 자산 ID")),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.id").description("파일 자산 ID"),
                                        fieldWithPath("data.sessionId").description("업로드 세션 ID"),
                                        fieldWithPath("data.fileName").description("파일명"),
                                        fieldWithPath("data.fileSize").description("파일 크기 (bytes)"),
                                        fieldWithPath("data.contentType")
                                                .description("Content-Type"),
                                        fieldWithPath("data.category").description("파일 카테고리"),
                                        fieldWithPath("data.bucket").description("S3 버킷"),
                                        fieldWithPath("data.s3Key").description("S3 객체 키"),
                                        fieldWithPath("data.etag").description("ETag"),
                                        fieldWithPath("data.status").description("상태"),
                                        fieldWithPath("data.createdAt").description("생성 시각"),
                                        fieldWithPath("data.processedAt").description("처리 완료 시각"),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/file-assets - 파일 자산 목록 조회 API 문서")
    void getFileAssets() throws Exception {
        // given
        List<FileAssetResponse> content =
                List.of(
                        new FileAssetResponse(
                                "asset-123",
                                "session-123",
                                "example1.jpg",
                                1024000L,
                                "image/jpeg",
                                "IMAGE",
                                "bucket",
                                "path/example1.jpg",
                                "etag-abc123",
                                "COMPLETED",
                                Instant.now(),
                                Instant.now()),
                        new FileAssetResponse(
                                "asset-456",
                                "session-456",
                                "example2.png",
                                2048000L,
                                "image/png",
                                "IMAGE",
                                "bucket",
                                "path/example2.png",
                                "etag-def456",
                                "COMPLETED",
                                Instant.now(),
                                Instant.now()));

        PageResponse<FileAssetResponse> response =
                new PageResponse<>(content, 0, 10, 2, 1, true, false);

        given(getFileAssetsUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/v1/file-assets")
                                .param("page", "0")
                                .param("size", "10")
                                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "file-asset-list",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                queryParameters(
                                        parameterWithName("page")
                                                .description("페이지 번호 (0부터 시작)")
                                                .optional(),
                                        parameterWithName("size").description("페이지 크기").optional(),
                                        parameterWithName("status")
                                                .description("파일 상태 필터")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.content").description("파일 자산 목록"),
                                        fieldWithPath("data.content[].id").description("파일 자산 ID"),
                                        fieldWithPath("data.content[].sessionId")
                                                .description("업로드 세션 ID"),
                                        fieldWithPath("data.content[].fileName").description("파일명"),
                                        fieldWithPath("data.content[].fileSize")
                                                .description("파일 크기 (bytes)"),
                                        fieldWithPath("data.content[].contentType")
                                                .description("Content-Type"),
                                        fieldWithPath("data.content[].category")
                                                .description("파일 카테고리"),
                                        fieldWithPath("data.content[].bucket").description("S3 버킷"),
                                        fieldWithPath("data.content[].s3Key")
                                                .description("S3 객체 키"),
                                        fieldWithPath("data.content[].etag").description("ETag"),
                                        fieldWithPath("data.content[].status").description("상태"),
                                        fieldWithPath("data.content[].createdAt")
                                                .description("생성 시각"),
                                        fieldWithPath("data.content[].processedAt")
                                                .description("처리 완료 시각"),
                                        fieldWithPath("data.page").description("현재 페이지 번호"),
                                        fieldWithPath("data.size").description("페이지 크기"),
                                        fieldWithPath("data.totalElements").description("전체 요소 수"),
                                        fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                        fieldWithPath("data.first").description("첫 페이지 여부"),
                                        fieldWithPath("data.last").description("마지막 페이지 여부"),
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
        FileAssetApiMapper fileAssetApiMapper() {
            return new FileAssetApiMapper();
        }
    }
}
