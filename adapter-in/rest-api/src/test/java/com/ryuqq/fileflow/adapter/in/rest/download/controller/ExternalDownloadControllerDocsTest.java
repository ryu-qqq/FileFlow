package com.ryuqq.fileflow.adapter.in.rest.download.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.command.RequestExternalDownloadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.download.mapper.ExternalDownloadApiMapper;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.port.in.command.RequestExternalDownloadUseCase;
import com.ryuqq.fileflow.application.download.port.in.query.GetExternalDownloadUseCase;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import java.time.Instant;
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
    @DisplayName("POST /api/v1/external-downloads - 외부 다운로드 요청 API 문서")
    void requestExternalDownload() throws Exception {
        // given
        RequestExternalDownloadApiRequest request =
                new RequestExternalDownloadApiRequest(
                        "https://example.com/image.jpg", "product-images");

        ExternalDownloadResponse response =
                new ExternalDownloadResponse(
                        "00000000-0000-0000-0000-000000000001", "PENDING", Instant.now());

        given(requestExternalDownloadUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/external-downloads")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(
                        document(
                                "external-download-request",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
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
                                        fieldWithPath("error").description("에러 정보").optional(),
                                        fieldWithPath("timestamp").description("응답 시각"),
                                        fieldWithPath("requestId").description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/external-downloads/{id} - 외부 다운로드 상태 조회 API 문서")
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
        mockMvc.perform(get("/api/v1/external-downloads/{id}", downloadId))
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
        ExternalDownloadApiMapper externalDownloadApiMapper() {
            return new ExternalDownloadApiMapper();
        }
    }
}
