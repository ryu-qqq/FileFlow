package com.ryuqq.fileflow.adapter.rest.upload.controller;

import com.ryuqq.fileflow.adapter.rest.upload.dto.request.InitMultipartApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.dto.request.MarkPartUploadedApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.dto.request.SingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.CompleteMultipartApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.InitMultipartApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.PartPresignedUrlApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.SingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.mapper.UploadApiMapper;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.command.GeneratePartUrlCommand;
import com.ryuqq.fileflow.application.upload.dto.command.InitMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteMultipartResponse;
import com.ryuqq.fileflow.application.upload.dto.response.InitMultipartResponse;
import com.ryuqq.fileflow.application.upload.dto.response.PartPresignedUrlResponse;
import com.ryuqq.fileflow.application.upload.dto.response.SingleUploadResponse;
import com.ryuqq.fileflow.application.upload.port.in.CompleteMultipartUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.in.GeneratePartPresignedUrlUseCase;
import com.ryuqq.fileflow.application.upload.port.in.InitMultipartUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.in.InitSingleUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.in.MarkPartUploadedUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UploadController Slice Test
 *
 * <p><strong>테스트 레벨:</strong> REST Layer (Slice Test)</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>HTTP 요청/응답 검증</li>
 *   <li>Status Code 검증</li>
 *   <li>Bean Validation 검증</li>
 *   <li>JSON 직렬화/역직렬화 검증</li>
 *   <li>Path Variable Validation 검증 (@Min, @Max)</li>
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
@WebMvcTest(UploadController.class)
@DisplayName("UploadController Slice Test")
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InitSingleUploadUseCase initSingleUploadUseCase;

    @MockBean
    private InitMultipartUploadUseCase initMultipartUploadUseCase;

    @MockBean
    private GeneratePartPresignedUrlUseCase generatePartPresignedUrlUseCase;

    @MockBean
    private MarkPartUploadedUseCase markPartUploadedUseCase;

    @MockBean
    private CompleteMultipartUploadUseCase completeMultipartUploadUseCase;

    @MockBean
    private UploadApiMapper mapper;

    // ========================================
    // 단일 업로드 테스트
    // ========================================

    @Test
    @DisplayName("POST /api/v1/uploads/single - 단일 업로드 초기화 성공 (201 Created)")
    void initSingleUpload_Success_ReturnsCreated() throws Exception {
        // Given
        String requestBody = """
            {
              "fileName": "document.pdf",
              "fileSize": 5242880,
              "contentType": "application/pdf",
              "checksum": "d41d8cd98f00b204e9800998ecf8427e"
            }
            """;

        Long tenantId = 1L;
        String sessionKey = "spu_abc123def456";
        String uploadUrl = "https://s3.amazonaws.com/bucket/uploads/document.pdf?signature=...";
        String storageKey = "uploads/2024/10/31/document.pdf";

        InitSingleUploadCommand command = InitSingleUploadCommand.of(
            com.ryuqq.fileflow.domain.iam.tenant.TenantId.of(tenantId),
            "document.pdf",
            5242880L,
            "application/pdf"
        );

        SingleUploadResponse useCaseResponse = new SingleUploadResponse(
            sessionKey, uploadUrl, storageKey
        );

        SingleUploadApiResponse apiResponse = SingleUploadApiResponse.of(
            sessionKey, uploadUrl, storageKey
        );

        when(mapper.toCommand(any(SingleUploadApiRequest.class), eq(tenantId)))
            .thenReturn(command);
        when(initSingleUploadUseCase.execute(command))
            .thenReturn(useCaseResponse);
        when(mapper.toApiResponse(useCaseResponse))
            .thenReturn(apiResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/uploads/single")
                .header("X-Tenant-Id", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sessionKey").value(sessionKey))
            .andExpect(jsonPath("$.data.uploadUrl").value(uploadUrl))
            .andExpect(jsonPath("$.data.storageKey").value(storageKey));
    }

    @Test
    @DisplayName("POST /api/v1/uploads/single - fileName이 null이면 400 Bad Request")
    void initSingleUpload_NullFileName_ReturnsBadRequest() throws Exception {
        // Given: fileName이 null
        String requestBody = """
            {
              "fileName": null,
              "fileSize": 5242880,
              "contentType": "application/pdf",
              "checksum": "d41d8cd98f00b204e9800998ecf8427e"
            }
            """;

        // When & Then: Bean Validation 실패
        mockMvc.perform(post("/api/v1/uploads/single")
                .header("X-Tenant-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/uploads/single - fileSize가 음수면 400 Bad Request")
    void initSingleUpload_NegativeFileSize_ReturnsBadRequest() throws Exception {
        // Given: fileSize가 음수
        String requestBody = """
            {
              "fileName": "document.pdf",
              "fileSize": -1,
              "contentType": "application/pdf",
              "checksum": "d41d8cd98f00b204e9800998ecf8427e"
            }
            """;

        // When & Then: Bean Validation 실패 (@Positive)
        mockMvc.perform(post("/api/v1/uploads/single")
                .header("X-Tenant-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    // ========================================
    // Multipart 업로드 초기화 테스트
    // ========================================

    @Test
    @DisplayName("POST /api/v1/uploads/multipart/init - Multipart 초기화 성공 (201 Created)")
    void initMultipartUpload_Success_ReturnsCreated() throws Exception {
        // Given
        String requestBody = """
            {
              "fileName": "large-video.mp4",
              "fileSize": 524288000,
              "contentType": "video/mp4",
              "checksum": "d41d8cd98f00b204e9800998ecf8427e"
            }
            """;

        Long tenantId = 1L;
        String sessionKey = "mpu_abc123def456";
        String uploadId = "upload-xyz789";
        int totalParts = 10;
        String storageKey = "uploads/2024/10/31/large-video.mp4";

        InitMultipartCommand command = InitMultipartCommand.of(
            com.ryuqq.fileflow.domain.iam.tenant.TenantId.of(tenantId),
            "large-video.mp4",
            524288000L,
            "video/mp4"
        );

        InitMultipartResponse useCaseResponse = new InitMultipartResponse(
            sessionKey, uploadId, totalParts, storageKey
        );

        InitMultipartApiResponse apiResponse = InitMultipartApiResponse.of(
            sessionKey, uploadId, totalParts, storageKey
        );

        when(mapper.toCommand(any(InitMultipartApiRequest.class), eq(tenantId)))
            .thenReturn(command);
        when(initMultipartUploadUseCase.execute(command))
            .thenReturn(useCaseResponse);
        when(mapper.toApiResponse(useCaseResponse))
            .thenReturn(apiResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/uploads/multipart/init")
                .header("X-Tenant-Id", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sessionKey").value(sessionKey))
            .andExpect(jsonPath("$.data.uploadId").value(uploadId))
            .andExpect(jsonPath("$.data.totalParts").value(totalParts))
            .andExpect(jsonPath("$.data.storageKey").value(storageKey));
    }

    // ========================================
    // 파트 업로드 URL 생성 테스트
    // ========================================

    @Test
    @DisplayName("POST /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url - 파트 URL 생성 성공 (200 OK)")
    void generatePartUrl_Success_ReturnsOk() throws Exception {
        // Given
        String sessionKey = "mpu_abc123def456";
        int partNumber = 1;
        String presignedUrl = "https://s3.amazonaws.com/bucket/uploads/part1?signature=...";
        Long expiresInSeconds = 3600L;

        GeneratePartUrlCommand command = new GeneratePartUrlCommand(sessionKey, partNumber);

        PartPresignedUrlResponse useCaseResponse = new PartPresignedUrlResponse(
            partNumber, presignedUrl, java.time.Duration.ofSeconds(expiresInSeconds)
        );

        PartPresignedUrlApiResponse apiResponse = PartPresignedUrlApiResponse.of(
            partNumber, presignedUrl, expiresInSeconds
        );

        when(mapper.toCommand(sessionKey, partNumber))
            .thenReturn(command);
        when(generatePartPresignedUrlUseCase.execute(command))
            .thenReturn(useCaseResponse);
        when(mapper.toApiResponse(useCaseResponse))
            .thenReturn(apiResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url",
                sessionKey, partNumber))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.partNumber").value(partNumber))
            .andExpect(jsonPath("$.data.presignedUrl").value(presignedUrl))
            .andExpect(jsonPath("$.data.expiresInSeconds").value(expiresInSeconds));
    }

    @Test
    @DisplayName("POST /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url - partNumber가 0이면 400 Bad Request")
    void generatePartUrl_PartNumberZero_ReturnsBadRequest() throws Exception {
        // Given: partNumber가 0 (최소값 1)
        String sessionKey = "mpu_abc123def456";
        int partNumber = 0;

        // When & Then: @Min(1) Validation 실패
        mockMvc.perform(post("/api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url",
                sessionKey, partNumber))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url - partNumber가 10001이면 400 Bad Request")
    void generatePartUrl_PartNumberExceedsMax_ReturnsBadRequest() throws Exception {
        // Given: partNumber가 10001 (최대값 10000)
        String sessionKey = "mpu_abc123def456";
        int partNumber = 10001;

        // When & Then: @Max(10000) Validation 실패
        mockMvc.perform(post("/api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url",
                sessionKey, partNumber))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    // ========================================
    // 파트 업로드 완료 통보 테스트
    // ========================================

    @Test
    @DisplayName("PUT /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber} - 파트 업로드 완료 통보 성공 (204 No Content)")
    void markPartUploaded_Success_ReturnsNoContent() throws Exception {
        // Given
        String sessionKey = "mpu_abc123def456";
        int partNumber = 1;
        String requestBody = """
            {
              "etag": "\\"d41d8cd98f00b204e9800998ecf8427e\\"",
              "partSize": 5242880
            }
            """;

        MarkPartUploadedCommand command = new MarkPartUploadedCommand(
            sessionKey, partNumber, "\"d41d8cd98f00b204e9800998ecf8427e\"", 5242880L
        );

        when(mapper.toCommand(eq(sessionKey), eq(partNumber), any(MarkPartUploadedApiRequest.class)))
            .thenReturn(command);
        doNothing().when(markPartUploadedUseCase).execute(command);

        // When & Then
        mockMvc.perform(put("/api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}",
                sessionKey, partNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber} - etag가 null이면 400 Bad Request")
    void markPartUploaded_NullEtag_ReturnsBadRequest() throws Exception {
        // Given: etag가 null
        String sessionKey = "mpu_abc123def456";
        int partNumber = 1;
        String requestBody = """
            {
              "etag": null,
              "partSize": 5242880
            }
            """;

        // When & Then: Bean Validation 실패
        mockMvc.perform(put("/api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}",
                sessionKey, partNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    // ========================================
    // Multipart 업로드 완료 테스트
    // ========================================

    @Test
    @DisplayName("POST /api/v1/uploads/multipart/{sessionKey}/complete - Multipart 완료 성공 (200 OK)")
    void completeMultipartUpload_Success_ReturnsOk() throws Exception {
        // Given
        String sessionKey = "mpu_abc123def456";
        Long fileId = 12345L;
        String etag = "\"abc123def456\"";
        String location = "https://s3.amazonaws.com/bucket/uploads/2024/10/31/large-video.mp4";

        CompleteMultipartCommand command = new CompleteMultipartCommand(sessionKey);

        CompleteMultipartResponse useCaseResponse = new CompleteMultipartResponse(
            fileId, etag, location
        );

        CompleteMultipartApiResponse apiResponse = CompleteMultipartApiResponse.of(
            fileId, etag, location
        );

        when(mapper.toCommand(sessionKey))
            .thenReturn(command);
        when(completeMultipartUploadUseCase.execute(command))
            .thenReturn(useCaseResponse);
        when(mapper.toApiResponse(useCaseResponse))
            .thenReturn(apiResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/uploads/multipart/{sessionKey}/complete", sessionKey))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.fileId").value(fileId))
            .andExpect(jsonPath("$.data.etag").value(etag))
            .andExpect(jsonPath("$.data.location").value(location));
    }

}
