package com.ryuqq.fileflow.adapter.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.dto.request.CreateUploadSessionRequest;
import com.ryuqq.fileflow.application.upload.dto.CreateUploadSessionCommand;
import com.ryuqq.fileflow.application.upload.dto.PresignedUrlResponse;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;
import com.ryuqq.fileflow.application.upload.port.in.CreateUploadSessionUseCase;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;
import com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UploadSessionController MockMvc Test
 *
 * Controller 계층의 HTTP 요청/응답을 테스트합니다.
 *
 * @author sangwon-ryu
 */
@WebMvcTest(UploadSessionController.class)
@DisplayName("UploadSessionController 테스트")
@WithMockUser
class UploadSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateUploadSessionUseCase createUploadSessionUseCase;

    @Test
    @DisplayName("POST /api/v1/upload/sessions - 세션 생성 성공")
    void createUploadSession_Success() throws Exception {
        // Given
        CreateUploadSessionRequest request = createValidRequest();
        CreateUploadSessionUseCase.UploadSessionWithUrlResponse response = createMockResponse();

        when(createUploadSessionUseCase.createSession(any(CreateUploadSessionCommand.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.sessionId").exists())
                .andExpect(jsonPath("$.session.status").value("PENDING"))
                .andExpect(jsonPath("$.session.fileName").value("test-image.jpg"))
                .andExpect(jsonPath("$.session.fileSize").value(1024000))
                .andExpect(jsonPath("$.session.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.presignedUrl.url").exists())
                .andExpect(jsonPath("$.presignedUrl.uploadPath").exists())
                .andExpect(jsonPath("$.presignedUrl.expiresAt").exists());
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions - 멱등성 키로 기존 세션 반환")
    void createUploadSession_IdempotencyKey_ReturnsExistingSession() throws Exception {
        // Given
        String idempotencyKey = UUID.randomUUID().toString();
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                1024000L,
                "image/jpeg",
                "user-123",
                30,
                idempotencyKey
        );
        CreateUploadSessionUseCase.UploadSessionWithUrlResponse response = createMockResponse();

        when(createUploadSessionUseCase.createSession(any(CreateUploadSessionCommand.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.sessionId").exists());
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions - 정책을 찾을 수 없음")
    void createUploadSession_PolicyNotFound() throws Exception {
        // Given
        CreateUploadSessionRequest request = createValidRequest();
        String policyKey = "non-existent:POLICY:KEY";

        when(createUploadSessionUseCase.createSession(any(CreateUploadSessionCommand.class)))
                .thenThrow(new PolicyNotFoundException(policyKey));

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Policy Not Found"));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions - 정책 위반")
    void createUploadSession_PolicyViolation() throws Exception {
        // Given
        CreateUploadSessionRequest request = createValidRequest();

        when(createUploadSessionUseCase.createSession(any(CreateUploadSessionCommand.class)))
                .thenThrow(new PolicyViolationException(PolicyViolationException.ViolationType.FILE_SIZE_EXCEEDED, "File size exceeds maximum allowed: 10MB"));

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("FILE_SIZE_EXCEEDED"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions - Validation 실패 (빈 파일명)")
    void createUploadSession_ValidationFailed_EmptyFileName() throws Exception {
        // Given
        CreateUploadSessionRequest invalidRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "", // 빈 파일명
                1024000L,
                "image/jpeg",
                "user-123",
                30,
                null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions - Validation 실패 (음수 파일 크기)")
    void createUploadSession_ValidationFailed_NegativeFileSize() throws Exception {
        // Given
        CreateUploadSessionRequest invalidRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                -1L, // 음수 파일 크기
                "image/jpeg",
                "user-123",
                30,
                null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions - Validation 실패 (만료 시간 0)")
    void createUploadSession_ValidationFailed_InvalidExpirationMinutes() throws Exception {
        // Given
        CreateUploadSessionRequest invalidRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                1024000L,
                "image/jpeg",
                "user-123",
                0, // 만료 시간 0
                null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions - Validation 실패 (빈 업로더 ID)")
    void createUploadSession_ValidationFailed_EmptyUploaderId() throws Exception {
        // Given
        CreateUploadSessionRequest invalidRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                1024000L,
                "image/jpeg",
                "", // 빈 업로더 ID
                30,
                null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions - 기본 만료 시간 사용 (null)")
    void createUploadSession_DefaultExpirationMinutes() throws Exception {
        // Given
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                1024000L,
                "image/jpeg",
                "user-123",
                null, // 만료 시간 null (기본값 30 사용)
                null
        );
        CreateUploadSessionUseCase.UploadSessionWithUrlResponse response = createMockResponse();

        when(createUploadSessionUseCase.createSession(any(CreateUploadSessionCommand.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.sessionId").exists());
    }

    // Helper Methods

    private CreateUploadSessionRequest createValidRequest() {
        return new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                1024000L,
                "image/jpeg",
                "user-123",
                30,
                null
        );
    }

    private CreateUploadSessionUseCase.UploadSessionWithUrlResponse createMockResponse() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(30);

        UploadSessionResponse sessionResponse = new UploadSessionResponse(
                UUID.randomUUID().toString(),
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                1024000L,
                "image/jpeg",
                "user-123",
                UploadStatus.PENDING,
                now,
                expiresAt,
                false,
                true
        );

        PresignedUrlResponse presignedUrlResponse = new PresignedUrlResponse(
                "https://s3.amazonaws.com/bucket/path/to/file?signature=xyz",
                "path/to/file",
                expiresAt
        );

        return new CreateUploadSessionUseCase.UploadSessionWithUrlResponse(
                sessionResponse,
                presignedUrlResponse,
                null  // multipartUpload: 단일 파일 업로드이므로 null
        );
    }
}
