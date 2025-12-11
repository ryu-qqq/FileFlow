package com.ryuqq.fileflow.adapter.in.rest.asset.controller;

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

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.BatchGenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.DeleteFileAssetApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.GenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.mapper.FileAssetApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.dto.response.DeleteFileAssetResponse;
import com.ryuqq.fileflow.application.asset.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.port.in.command.BatchGenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.asset.port.in.command.DeleteFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.port.in.command.GenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import java.time.Duration;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * FileAssetCommandController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(FileAssetCommandController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.endpoints.base-v1=/api/v1")
@Import(FileAssetCommandControllerDocsTest.DocsTestConfig.class)
@DisplayName("FileAssetCommandController REST Docs")
class FileAssetCommandControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private DeleteFileAssetUseCase deleteFileAssetUseCase;
    @MockitoBean private GenerateDownloadUrlUseCase generateDownloadUrlUseCase;
    @MockitoBean private BatchGenerateDownloadUrlUseCase batchGenerateDownloadUrlUseCase;

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
    @DisplayName("PATCH /api/v1/file-assets/{id}/delete - 파일 자산 삭제 API 문서")
    void deleteFileAsset() throws Exception {
        // given
        String fileAssetId = "asset-123";
        DeleteFileAssetApiRequest request = new DeleteFileAssetApiRequest("더 이상 필요하지 않음");

        DeleteFileAssetResponse response = new DeleteFileAssetResponse(fileAssetId, Instant.now());

        given(deleteFileAssetUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/file-assets/{id}/delete", fileAssetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "file-asset-delete",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description("파일 자산 ID")),
                                requestFields(
                                        fieldWithPath("reason")
                                                .description("삭제 사유 (선택적)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.id").description("파일 자산 ID"),
                                        fieldWithPath("data.deletedAt").description("삭제 시각"),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("POST /api/v1/file-assets/{id}/download-url - Download URL 생성 API 문서")
    void generateDownloadUrl() throws Exception {
        // given
        String fileAssetId = "asset-123";
        GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest(60);

        DownloadUrlResponse response =
                new DownloadUrlResponse(
                        fileAssetId,
                        "https://s3.amazonaws.com/bucket/file.jpg?signature=...",
                        "example.jpg",
                        "image/jpeg",
                        1024000L,
                        Instant.now().plus(Duration.ofHours(1)));

        given(generateDownloadUrlUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/file-assets/{id}/download-url", fileAssetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "file-asset-generate-download-url",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description("파일 자산 ID")),
                                requestFields(
                                        fieldWithPath("expirationMinutes")
                                                .description("URL 유효 기간 (분, 선택적)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.fileAssetId").description("파일 자산 ID"),
                                        fieldWithPath("data.downloadUrl")
                                                .description("Presigned Download URL"),
                                        fieldWithPath("data.fileName").description("파일명"),
                                        fieldWithPath("data.contentType").description("컨텐츠 타입"),
                                        fieldWithPath("data.fileSize").description("파일 크기 (bytes)"),
                                        fieldWithPath("data.expiresAt").description("URL 만료 시각"),
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("POST /api/v1/file-assets/batch-download-url - 일괄 Download URL 생성 API 문서")
    void batchGenerateDownloadUrl() throws Exception {
        // given
        BatchGenerateDownloadUrlApiRequest request =
                new BatchGenerateDownloadUrlApiRequest(List.of("asset-123", "asset-456"), 60);

        BatchDownloadUrlResponse response =
                new BatchDownloadUrlResponse(
                        List.of(
                                new DownloadUrlResponse(
                                        "asset-123",
                                        "https://s3.amazonaws.com/bucket/file1.jpg?signature=...",
                                        "file1.jpg",
                                        "image/jpeg",
                                        1024000L,
                                        Instant.now().plus(Duration.ofHours(1))),
                                new DownloadUrlResponse(
                                        "asset-456",
                                        "https://s3.amazonaws.com/bucket/file2.jpg?signature=...",
                                        "file2.jpg",
                                        "image/jpeg",
                                        2048000L,
                                        Instant.now().plus(Duration.ofHours(1)))),
                        2,
                        0,
                        List.of());

        given(batchGenerateDownloadUrlUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/file-assets/batch-download-url")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "file-asset-batch-download-url",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("fileAssetIds")
                                                .description("파일 자산 ID 목록 (최대 100개)"),
                                        fieldWithPath("expirationMinutes")
                                                .description("URL 유효 기간 (분, 선택적)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.downloadUrls")
                                                .description("성공한 URL 목록"),
                                        fieldWithPath("data.downloadUrls[].fileAssetId")
                                                .description("파일 자산 ID"),
                                        fieldWithPath("data.downloadUrls[].downloadUrl")
                                                .description("Presigned Download URL"),
                                        fieldWithPath("data.downloadUrls[].fileName")
                                                .description("파일명"),
                                        fieldWithPath("data.downloadUrls[].contentType")
                                                .description("컨텐츠 타입"),
                                        fieldWithPath("data.downloadUrls[].fileSize")
                                                .description("파일 크기 (bytes)"),
                                        fieldWithPath("data.downloadUrls[].expiresAt")
                                                .description("URL 만료 시각"),
                                        fieldWithPath("data.successCount").description("성공 건수"),
                                        fieldWithPath("data.failureCount").description("실패 건수"),
                                        fieldWithPath("data.failures").description("실패한 항목 목록"),
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
