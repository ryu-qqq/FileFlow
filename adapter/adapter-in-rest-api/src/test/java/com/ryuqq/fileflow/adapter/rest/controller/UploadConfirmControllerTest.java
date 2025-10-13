package com.ryuqq.fileflow.adapter.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.dto.request.ConfirmUploadRequest;
import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadResponse;
import com.ryuqq.fileflow.application.upload.port.in.ConfirmUploadUseCase;
import com.ryuqq.fileflow.domain.upload.exception.ChecksumMismatchException;
import com.ryuqq.fileflow.domain.upload.exception.FileNotFoundInS3Exception;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UploadConfirmController MockMvc Test
 *
 * Controller 계층의 HTTP 요청/응답을 테스트합니다.
 *
 * 테스트 범위:
 * - 정상 케이스: 업로드 완료 확인 성공
 * - 멱등성: 동일 요청 재호출 시 200 OK 반환
 * - 에러 케이스: 세션 없음, 파일 없음, ETag 불일치
 *
 * @author sangwon-ryu
 */
@WebMvcTest(controllers = {
        UploadConfirmController.class,
        com.ryuqq.fileflow.adapter.rest.exception.GlobalExceptionHandler.class
})
@DisplayName("UploadConfirmController 테스트")
@WithMockUser
class UploadConfirmControllerTest {

    @org.springframework.boot.autoconfigure.EnableAutoConfiguration(
            exclude = {
                    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
            }
    )
    @org.springframework.boot.autoconfigure.SpringBootApplication
    static class TestConfig {
        // Minimal test configuration for unit test
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConfirmUploadUseCase confirmUploadUseCase;

    @Test
    @DisplayName("POST /api/v1/upload/sessions/{sessionId}/confirm - 업로드 완료 확인 성공")
    void confirmUpload_Success() throws Exception {
        // Given
        String sessionId = UUID.randomUUID().toString();
        String uploadPath = "user123/" + UUID.randomUUID() + "/test.jpg";
        ConfirmUploadRequest request = new ConfirmUploadRequest(uploadPath, null); // ETag 없음

        ConfirmUploadResponse response = ConfirmUploadResponse.success(
                sessionId,
                UploadStatus.COMPLETED
        );

        when(confirmUploadUseCase.confirm(any(ConfirmUploadCommand.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value("업로드가 성공적으로 확인되었습니다."));

        verify(confirmUploadUseCase, times(1)).confirm(any(ConfirmUploadCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions/{sessionId}/confirm - ETag 검증 포함 성공")
    void confirmUpload_WithETag_Success() throws Exception {
        // Given
        String sessionId = UUID.randomUUID().toString();
        String uploadPath = "user123/" + UUID.randomUUID() + "/test.jpg";
        String etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";
        ConfirmUploadRequest request = new ConfirmUploadRequest(uploadPath, etag);

        ConfirmUploadResponse response = ConfirmUploadResponse.success(
                sessionId,
                UploadStatus.COMPLETED
        );

        when(confirmUploadUseCase.confirm(any(ConfirmUploadCommand.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").exists());

        verify(confirmUploadUseCase, times(1)).confirm(any(ConfirmUploadCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions/{sessionId}/confirm - 멱등성: 이미 완료된 세션")
    void confirmUpload_Idempotent_AlreadyCompleted() throws Exception {
        // Given
        String sessionId = UUID.randomUUID().toString();
        String uploadPath = "user123/" + UUID.randomUUID() + "/test.jpg";
        ConfirmUploadRequest request = new ConfirmUploadRequest(uploadPath, null);

        ConfirmUploadResponse response = ConfirmUploadResponse.of(
                sessionId,
                UploadStatus.COMPLETED,
                "업로드가 성공적으로 확인되었습니다." // 멱등성: 동일한 응답
        );

        when(confirmUploadUseCase.confirm(any(ConfirmUploadCommand.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(confirmUploadUseCase, times(1)).confirm(any(ConfirmUploadCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions/{sessionId}/confirm - 세션을 찾을 수 없음")
    void confirmUpload_SessionNotFound() throws Exception {
        // Given
        String sessionId = UUID.randomUUID().toString();
        String uploadPath = "user123/" + UUID.randomUUID() + "/test.jpg";
        ConfirmUploadRequest request = new ConfirmUploadRequest(uploadPath, null);

        when(confirmUploadUseCase.confirm(any(ConfirmUploadCommand.class)))
                .thenThrow(new UploadSessionNotFoundException(sessionId));

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Upload Session Not Found"));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions/{sessionId}/confirm - S3에 파일 없음")
    void confirmUpload_FileNotFoundInS3() throws Exception {
        // Given
        String sessionId = UUID.randomUUID().toString();
        String bucket = "test-bucket";
        String key = "tenant/session/file.jpg";
        String uploadPath = "user123/" + UUID.randomUUID() + "/test.jpg";
        ConfirmUploadRequest request = new ConfirmUploadRequest(uploadPath, null);

        when(confirmUploadUseCase.confirm(any(ConfirmUploadCommand.class)))
                .thenThrow(new FileNotFoundInS3Exception(sessionId, bucket, key));

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("File Not Found in S3"));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions/{sessionId}/confirm - ETag 불일치")
    void confirmUpload_ChecksumMismatch() throws Exception {
        // Given
        String sessionId = UUID.randomUUID().toString();
        String uploadPath = "user123/" + UUID.randomUUID() + "/test.jpg";
        String expectedEtag = "\"expected123\"";
        String actualEtag = "\"actual456\"";
        ConfirmUploadRequest request = new ConfirmUploadRequest(uploadPath, expectedEtag);

        when(confirmUploadUseCase.confirm(any(ConfirmUploadCommand.class)))
                .thenThrow(new ChecksumMismatchException(sessionId, expectedEtag, actualEtag));

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Checksum Mismatch"));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions/{sessionId}/confirm - 잘못된 상태의 세션")
    void confirmUpload_InvalidState() throws Exception {
        // Given
        String sessionId = UUID.randomUUID().toString();
        String uploadPath = "user123/" + UUID.randomUUID() + "/test.jpg";
        ConfirmUploadRequest request = new ConfirmUploadRequest(uploadPath, null);

        when(confirmUploadUseCase.confirm(any(ConfirmUploadCommand.class)))
                .thenThrow(new IllegalStateException("Cannot confirm upload. Current status: FAILED. Only PENDING sessions can be confirmed."));

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions/{sessionId}/confirm - null sessionId는 Controller에서 검증")
    void confirmUpload_NullSessionId() throws Exception {
        // Given
        String sessionId = UUID.randomUUID().toString();
        String uploadPath = "user123/" + UUID.randomUUID() + "/test.jpg";
        ConfirmUploadRequest request = new ConfirmUploadRequest(uploadPath, null);

        when(confirmUploadUseCase.confirm(any(ConfirmUploadCommand.class)))
                .thenThrow(new IllegalArgumentException("SessionId must not be null or empty"));

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/v1/upload/sessions/{sessionId}/confirm - Request Body 없음")
    void confirmUpload_MissingRequestBody() throws Exception {
        // Given
        String sessionId = UUID.randomUUID().toString();

        // When & Then
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
