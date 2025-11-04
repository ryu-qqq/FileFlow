package com.ryuqq.fileflow.adapter.rest.download.controller;

import com.ryuqq.fileflow.adapter.rest.download.dto.request.StartDownloadApiRequest;
import com.ryuqq.fileflow.adapter.rest.download.dto.response.DownloadStatusApiResponse;
import com.ryuqq.fileflow.adapter.rest.download.dto.response.StartDownloadApiResponse;
import com.ryuqq.fileflow.adapter.rest.download.mapper.DownloadApiMapper;
import com.ryuqq.fileflow.application.download.dto.command.StartExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.port.in.GetDownloadStatusUseCase;
import com.ryuqq.fileflow.application.download.port.in.StartExternalDownloadUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DownloadController Slice Test
 *
 * <p><strong>테스트 레벨:</strong> REST Layer (Slice Test)</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>HTTP 요청/응답 검증</li>
 *   <li>Status Code 검증</li>
 *   <li>Bean Validation 검증</li>
 *   <li>JSON 직렬화/역직렬화 검증</li>
 * </ul>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>@WebMvcTest: Controller Layer만 로딩 (빠름)</li>
 *   <li>MockMvc: HTTP 요청 시뮬레이션</li>
 *   <li>@MockBean: UseCase Mock 처리</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@WebMvcTest(DownloadController.class)
@DisplayName("DownloadController Slice Test")
class DownloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StartExternalDownloadUseCase startExternalDownloadUseCase;

    @MockBean
    private GetDownloadStatusUseCase getDownloadStatusUseCase;

    @MockBean
    private DownloadApiMapper mapper;

    @Test
    @DisplayName("POST /api/v1/downloads/external - 외부 다운로드 시작 성공 (202 Accepted)")
    void startExternalDownload_Success_ReturnsAccepted() throws Exception {
        // Given
        String requestBody = """
            {
              "sourceUrl": "https://example.com/files/document.pdf",
              "fileName": "document.pdf"
            }
            """;

        Long tenantId = 1L;
        Long uploadSessionId = 12345L;
        Long downloadId = 67890L;
        String idempotencyKey = "test-idem-key-001";

        StartExternalDownloadCommand command = new StartExternalDownloadCommand(
            idempotencyKey,
            new com.ryuqq.fileflow.domain.iam.tenant.TenantId(tenantId),
            "https://example.com/files/document.pdf",
            com.ryuqq.fileflow.domain.upload.FileName.of("document.pdf"),
            com.ryuqq.fileflow.domain.upload.FileSize.of(1024000L) // 1MB
        );

        ExternalDownloadResponse useCaseResponse = ExternalDownloadResponse.ofLegacy(
            downloadId,
            uploadSessionId,
            "https://example.com/files/document.pdf",
            "PENDING"
        );

        StartDownloadApiResponse apiResponse = new StartDownloadApiResponse(
            downloadId,
            uploadSessionId,
            "PENDING"
        );

        when(mapper.toCommand(any(StartDownloadApiRequest.class), eq(tenantId), anyString()))
            .thenReturn(command);
        when(startExternalDownloadUseCase.execute(command))
            .thenReturn(useCaseResponse);
        when(mapper.toApiResponse(useCaseResponse))
            .thenReturn(apiResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/downloads/external")
                .header("X-Tenant-Id", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.downloadId").value(downloadId))
            .andExpect(jsonPath("$.data.uploadSessionId").value(uploadSessionId))
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/downloads/external - sourceUrl이 null이면 400 Bad Request")
    void startExternalDownload_NullSourceUrl_ReturnsBadRequest() throws Exception {
        // Given: sourceUrl이 null
        String requestBody = """
            {
              "sourceUrl": null,
              "fileName": "document.pdf"
            }
            """;

        // When & Then: Bean Validation 실패
        mockMvc.perform(post("/api/v1/downloads/external")
                .header("X-Tenant-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/downloads/external - fileName이 null이면 400 Bad Request")
    void startExternalDownload_NullFileName_ReturnsBadRequest() throws Exception {
        // Given: fileName이 null
        String requestBody = """
            {
              "sourceUrl": "https://example.com/files/document.pdf",
              "fileName": null
            }
            """;

        // When & Then: Bean Validation 실패
        mockMvc.perform(post("/api/v1/downloads/external")
                .header("X-Tenant-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/downloads/external - X-Tenant-Id 헤더 없으면 400 Bad Request")
    void startExternalDownload_MissingTenantId_ReturnsBadRequest() throws Exception {
        // Given
        String requestBody = """
            {
              "sourceUrl": "https://example.com/files/document.pdf",
              "fileName": "document.pdf"
            }
            """;

        // When & Then: X-Tenant-Id 헤더 없음
        mockMvc.perform(post("/api/v1/downloads/external")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/downloads/external - sourceUrl이 빈 문자열이면 400 Bad Request")
    void startExternalDownload_EmptySourceUrl_ReturnsBadRequest() throws Exception {
        // Given: sourceUrl이 빈 문자열
        String requestBody = """
            {
              "sourceUrl": "",
              "fileName": "document.pdf"
            }
            """;

        // When & Then: Bean Validation 실패 (@NotBlank)
        mockMvc.perform(post("/api/v1/downloads/external")
                .header("X-Tenant-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/downloads/external/{downloadId}/status - 다운로드 상태 조회 성공 (200 OK)")
    void getDownloadStatus_Success_ReturnsOk() throws Exception {
        // Given
        Long downloadId = 67890L;
        Long uploadSessionId = 12345L;

        ExternalDownloadResponse useCaseResponse = ExternalDownloadResponse.ofLegacy(
            downloadId,
            uploadSessionId,
            "https://example.com/files/document.pdf",
            "DOWNLOADING"
        );

        DownloadStatusApiResponse apiResponse = new DownloadStatusApiResponse(
            downloadId,
            "DOWNLOADING",
            "https://example.com/files/document.pdf",
            uploadSessionId
        );

        when(getDownloadStatusUseCase.execute(downloadId))
            .thenReturn(useCaseResponse);
        when(mapper.toStatusApiResponse(useCaseResponse))
            .thenReturn(apiResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/downloads/external/{downloadId}/status", downloadId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.downloadId").value(downloadId))
            .andExpect(jsonPath("$.data.status").value("DOWNLOADING"))
            .andExpect(jsonPath("$.data.sourceUrl").value("https://example.com/files/document.pdf"))
            .andExpect(jsonPath("$.data.uploadSessionId").value(uploadSessionId));
    }

    @Test
    @DisplayName("GET /api/v1/downloads/external/{downloadId}/status - COMPLETED 상태 조회 성공")
    void getDownloadStatus_Completed_ReturnsOk() throws Exception {
        // Given
        Long downloadId = 67890L;
        Long uploadSessionId = 12345L;

        ExternalDownloadResponse useCaseResponse = ExternalDownloadResponse.ofLegacy(
            downloadId,
            uploadSessionId,
            "https://example.com/files/document.pdf",
            "COMPLETED"
        );

        DownloadStatusApiResponse apiResponse = new DownloadStatusApiResponse(
            downloadId,
            "COMPLETED",
            "https://example.com/files/document.pdf",
            uploadSessionId
        );

        when(getDownloadStatusUseCase.execute(downloadId))
            .thenReturn(useCaseResponse);
        when(mapper.toStatusApiResponse(useCaseResponse))
            .thenReturn(apiResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/downloads/external/{downloadId}/status", downloadId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/v1/downloads/external/{downloadId}/status - FAILED 상태 조회 성공")
    void getDownloadStatus_Failed_ReturnsOk() throws Exception {
        // Given
        Long downloadId = 67890L;
        Long uploadSessionId = 12345L;

        ExternalDownloadResponse useCaseResponse = ExternalDownloadResponse.ofLegacy(
            downloadId,
            uploadSessionId,
            "https://example.com/files/document.pdf",
            "FAILED"
        );

        DownloadStatusApiResponse apiResponse = new DownloadStatusApiResponse(
            downloadId,
            "FAILED",
            "https://example.com/files/document.pdf",
            uploadSessionId
        );

        when(getDownloadStatusUseCase.execute(downloadId))
            .thenReturn(useCaseResponse);
        when(mapper.toStatusApiResponse(useCaseResponse))
            .thenReturn(apiResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/downloads/external/{downloadId}/status", downloadId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("FAILED"));
    }

}
