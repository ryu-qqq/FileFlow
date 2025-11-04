package com.ryuqq.fileflow.adapter.rest.download.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.download.dto.request.StartDownloadApiRequest;
import com.ryuqq.fileflow.adapter.rest.download.fixture.*;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.port.in.GetDownloadStatusUseCase;
import com.ryuqq.fileflow.application.download.port.in.StartExternalDownloadUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import com.ryuqq.fileflow.adapter.rest.integration.IntegrationTestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DownloadController REST API Documentation Test
 *
 * <p>Spring REST Docs를 사용하여 Download API 문서를 자동 생성합니다.</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>POST /api/v1/downloads/external - 외부 URL 다운로드 시작</li>
 *   <li>GET /api/v1/downloads/external/{downloadId}/status - 다운로드 상태 조회</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@WebMvcTest(DownloadController.class)
@AutoConfigureRestDocs
@Import(IntegrationTestConfiguration.class)
@DisplayName("DownloadController API 문서 생성 테스트")
class DownloadControllerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StartExternalDownloadUseCase startExternalDownloadUseCase;

    @MockBean
    private GetDownloadStatusUseCase getDownloadStatusUseCase;

    @Test
    @DisplayName("POST /api/v1/downloads/external - 외부 URL 다운로드 시작")
    void startExternalDownload() throws Exception {
        // Given
        StartDownloadApiRequest request = StartDownloadApiRequestFixture.create();
        ExternalDownloadResponse response = ExternalDownloadResponse.of(
            "idem-key-download-123",
            67890L,
            12345L,
            "https://example.com/files/document.pdf",
            "PENDING"
        );

        given(startExternalDownloadUseCase.execute(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            post("/api/v1/downloads/external")
                .header("X-Tenant-Id", 1L)
                .header("X-Idempotency-Key", "idem-key-download-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.downloadId").exists())
        .andDo(document("download/start-external",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(
                headerWithName("X-Tenant-Id")
                    .description("테넌트 ID (필수)"),
                headerWithName("X-Idempotency-Key")
                    .description("멱등성 키 (선택, 없으면 자동 생성)")
            ),
            requestFields(
                fieldWithPath("sourceUrl")
                    .type(JsonFieldType.STRING)
                    .description("다운로드할 외부 URL (예: https://example.com/files/document.pdf)"),
                fieldWithPath("fileName")
                    .type(JsonFieldType.STRING)
                    .description("저장할 파일명 (선택, 없으면 URL에서 추출)")
                    .optional()
            ),
            responseFields(
                fieldWithPath("success")
                    .type(JsonFieldType.BOOLEAN)
                    .description("성공 여부"),
                fieldWithPath("data.downloadId")
                    .type(JsonFieldType.NUMBER)
                    .description("다운로드 ID (상태 조회 시 사용)"),
                fieldWithPath("data.uploadSessionId")
                    .type(JsonFieldType.NUMBER)
                    .description("업로드 세션 ID (내부 처리용)"),
                fieldWithPath("data.status")
                    .type(JsonFieldType.STRING)
                    .description("다운로드 상태 (PENDING/DOWNLOADING/COMPLETED/FAILED)"),
                fieldWithPath("error")
                    .type(JsonFieldType.NULL)
                    .description("에러 정보 (성공 시 null)")
                    .optional(),
                fieldWithPath("timestamp")
                    .type(JsonFieldType.STRING)
                    .description("응답 시간 (ISO 8601)")
                    .optional(),
                fieldWithPath("requestId")
                    .type(JsonFieldType.STRING)
                    .description("요청 ID")
                    .optional()
            )
        ));
    }

    @Test
    @DisplayName("GET /api/v1/downloads/external/{downloadId}/status - 다운로드 상태 조회")
    void getDownloadStatus() throws Exception {
        // Given
        Long downloadId = 67890L;
        ExternalDownloadResponse response = ExternalDownloadResponse.of(
            "idem-key-download-123",
            downloadId,
            12345L,
            "https://example.com/files/document.pdf",
            "DOWNLOADING"
        );

        given(getDownloadStatusUseCase.execute(anyLong())).willReturn(response);

        // When & Then
        mockMvc.perform(
            get("/api/v1/downloads/external/{downloadId}/status", downloadId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.downloadId").value(downloadId))
        .andDo(document("download/get-status",
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("downloadId")
                    .description("조회할 다운로드 ID (startExternalDownload에서 반환받은 값)")
            ),
            responseFields(
                fieldWithPath("success")
                    .type(JsonFieldType.BOOLEAN)
                    .description("성공 여부"),
                fieldWithPath("data.downloadId")
                    .type(JsonFieldType.NUMBER)
                    .description("다운로드 ID"),
                fieldWithPath("data.status")
                    .type(JsonFieldType.STRING)
                    .description("다운로드 상태 (PENDING/DOWNLOADING/COMPLETED/FAILED)"),
                fieldWithPath("data.sourceUrl")
                    .type(JsonFieldType.STRING)
                    .description("다운로드 소스 URL"),
                fieldWithPath("data.uploadSessionId")
                    .type(JsonFieldType.NUMBER)
                    .description("업로드 세션 ID"),
                fieldWithPath("error")
                    .type(JsonFieldType.NULL)
                    .description("에러 정보 (성공 시 null)")
                    .optional(),
                fieldWithPath("timestamp")
                    .type(JsonFieldType.STRING)
                    .description("응답 시간 (ISO 8601)")
                    .optional(),
                fieldWithPath("requestId")
                    .type(JsonFieldType.STRING)
                    .description("요청 ID")
                    .optional()
            )
        ));
    }
}
