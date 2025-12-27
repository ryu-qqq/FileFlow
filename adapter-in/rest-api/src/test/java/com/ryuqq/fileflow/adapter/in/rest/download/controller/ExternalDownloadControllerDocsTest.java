package com.ryuqq.fileflow.adapter.in.rest.download.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.command.RequestExternalDownloadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.download.mapper.ExternalDownloadApiMapper;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.port.in.command.RequestExternalDownloadUseCase;
import com.ryuqq.fileflow.application.download.port.in.query.GetExternalDownloadUseCase;
import com.ryuqq.fileflow.application.download.port.in.query.GetExternalDownloadsUseCase;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import java.time.Instant;
import java.util.UUID;
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
 * ExternalDownloadController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(ExternalDownloadController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.endpoints.base-v1=/api/v1")
@Import(ExternalDownloadControllerDocsTest.DocsTestConfig.class)
@DisplayName("ExternalDownloadController REST Docs")
class ExternalDownloadControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private RequestExternalDownloadUseCase requestExternalDownloadUseCase;
    @MockitoBean private GetExternalDownloadUseCase getExternalDownloadUseCase;
    @MockitoBean private GetExternalDownloadsUseCase getExternalDownloadsUseCase;

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
    @DisplayName("POST /api/v1/file/external-downloads - 외부 다운로드 요청 API 문서")
    void requestExternalDownload() throws Exception {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        RequestExternalDownloadApiRequest request =
                new RequestExternalDownloadApiRequest(
                        idempotencyKey,
                        "https://example.com/image.jpg",
                        "https://webhook.example.com/callback");

        ExternalDownloadResponse response =
                new ExternalDownloadResponse(
                        "00000000-0000-0000-0000-000000000001", "PENDING", Instant.now());

        given(requestExternalDownloadUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/file/external-downloads")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(
                        document(
                                "external-download-request",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("idempotencyKey")
                                                .description("멱등성 보장을 위한 UUID 키"),
                                        fieldWithPath("sourceUrl").description("다운로드할 외부 URL"),
                                        fieldWithPath("webhookUrl")
                                                .description("완료 시 호출할 웹훅 URL (선택적)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.id").description("외부 다운로드 ID"),
                                        fieldWithPath("data.status").description("다운로드 상태"),
                                        fieldWithPath("data.createdAt").description("생성 시각"),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/file/external-downloads/{id} - 외부 다운로드 상태 조회 API 문서")
    void getExternalDownload() throws Exception {
        // given
        String downloadId = "00000000-0000-0000-0000-000000000001";

        ExternalDownloadDetailResponse response =
                new ExternalDownloadDetailResponse(
                        downloadId,
                        "https://example.com/image.jpg",
                        "COMPLETED",
                        "asset-123",
                        null,
                        0,
                        null,
                        Instant.now(),
                        Instant.now());

        given(getExternalDownloadUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/file/external-downloads/{id}", downloadId))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "external-download-get",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description("외부 다운로드 ID")),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.id").description("외부 다운로드 ID"),
                                        fieldWithPath("data.sourceUrl").description("원본 URL"),
                                        fieldWithPath("data.status").description("다운로드 상태"),
                                        fieldWithPath("data.fileAssetId")
                                                .description("생성된 파일 자산 ID")
                                                .optional(),
                                        fieldWithPath("data.errorMessage")
                                                .description("에러 메시지")
                                                .optional(),
                                        fieldWithPath("data.retryCount").description("재시도 횟수"),
                                        fieldWithPath("data.webhookUrl")
                                                .description("웹훅 URL")
                                                .optional(),
                                        fieldWithPath("data.createdAt").description("생성 시각"),
                                        fieldWithPath("data.updatedAt").description("수정 시각"),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/file/external-downloads - 외부 다운로드 목록 조회 API 문서")
    void getExternalDownloads() throws Exception {
        // given
        Instant now = Instant.now();
        ExternalDownloadDetailResponse item1 =
                new ExternalDownloadDetailResponse(
                        "00000000-0000-0000-0000-000000000001",
                        "https://example.com/image1.jpg",
                        "COMPLETED",
                        "asset-123",
                        null,
                        0,
                        null,
                        now,
                        now);

        ExternalDownloadDetailResponse item2 =
                new ExternalDownloadDetailResponse(
                        "00000000-0000-0000-0000-000000000002",
                        "https://example.com/image2.jpg",
                        "PENDING",
                        null,
                        null,
                        0,
                        "https://webhook.example.com/callback",
                        now,
                        now);

        PageResponse<ExternalDownloadDetailResponse> response =
                PageResponse.of(java.util.List.of(item1, item2), 0, 20, 2L, 1, true, true);

        given(getExternalDownloadsUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/v1/file/external-downloads")
                                .param("status", "COMPLETED")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "external-download-list",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                queryParameters(
                                        parameterWithName("status")
                                                .description(
                                                        "상태 필터 (PENDING, PROCESSING, COMPLETED,"
                                                                + " FAILED)")
                                                .optional(),
                                        parameterWithName("page")
                                                .description("페이지 번호 (0부터 시작)")
                                                .optional(),
                                        parameterWithName("size").description("페이지 크기").optional()),
                                responseFields(
                                        fieldWithPath("success").description("성공 여부"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.content").description("외부 다운로드 목록"),
                                        fieldWithPath("data.content[].id")
                                                .description("외부 다운로드 ID"),
                                        fieldWithPath("data.content[].sourceUrl")
                                                .description("원본 URL"),
                                        fieldWithPath("data.content[].status")
                                                .description("다운로드 상태"),
                                        fieldWithPath("data.content[].fileAssetId")
                                                .description("생성된 파일 자산 ID")
                                                .optional(),
                                        fieldWithPath("data.content[].errorMessage")
                                                .description("에러 메시지")
                                                .optional(),
                                        fieldWithPath("data.content[].retryCount")
                                                .description("재시도 횟수"),
                                        fieldWithPath("data.content[].webhookUrl")
                                                .description("웹훅 URL")
                                                .optional(),
                                        fieldWithPath("data.content[].createdAt")
                                                .description("생성 시각"),
                                        fieldWithPath("data.content[].updatedAt")
                                                .description("수정 시각"),
                                        fieldWithPath("data.page").description("현재 페이지 번호"),
                                        fieldWithPath("data.size").description("페이지 크기"),
                                        fieldWithPath("data.totalElements").description("전체 요소 수"),
                                        fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                        fieldWithPath("data.first").description("첫 페이지 여부"),
                                        fieldWithPath("data.last").description("마지막 페이지 여부"),
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
        ExternalDownloadApiMapper externalDownloadApiMapper() {
            return new ExternalDownloadApiMapper();
        }
    }
}
