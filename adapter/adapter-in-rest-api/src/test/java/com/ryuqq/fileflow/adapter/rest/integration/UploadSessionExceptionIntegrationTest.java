package com.ryuqq.fileflow.adapter.rest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.dto.request.CreateUploadSessionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Upload Session Exception Scenario Integration Test
 *
 * 예외 시나리오를 테스트합니다:
 * 1. 정책 위반 (파일 크기 초과, 허용되지 않은 포맷)
 * 2. 존재하지 않는 정책 키
 * 3. Validation 실패
 * 4. 세션 만료 시나리오
 *
 * @author sangwon-ryu
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@DisplayName("Upload Session 예외 시나리오 통합 테스트")
@WithMockUser
@Sql(scripts = "/sql/cleanup-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/insert-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UploadSessionExceptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("예외: 파일 크기 초과 - b2c:CONSUMER:REVIEW 정책 (최대 10MB)")
    void createUploadSession_FileSizeExceeded_PolicyViolation() throws Exception {
        // Given: 11MB 파일 (정책 위반: 최대 10MB)
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "too-large-image.jpg",
                11534336L, // 11MB
                "image/jpeg",
                "user-123",
                30,
                null
        );

        // When & Then: 400 Bad Request with FILE_SIZE_EXCEEDED
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("FILE_SIZE_EXCEEDED"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("예외: 허용되지 않은 파일 포맷 - b2c:CONSUMER:REVIEW는 JPG/PNG/WEBP만 허용")
    void createUploadSession_UnsupportedFormat_PolicyViolation() throws Exception {
        // Given: GIF 파일 (정책 위반: JPG/PNG/WEBP만 허용)
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "animated.gif",
                5242880L,
                "image/gif",
                "user-123",
                30,
                null
        );

        // When & Then: 400 Bad Request with FILE_FORMAT_NOT_ALLOWED
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("FILE_FORMAT_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("예외: 존재하지 않는 정책 키")
    void createUploadSession_PolicyNotFound() throws Exception {
        // Given: 존재하지 않는 정책 키
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "non-existent:POLICY:KEY",
                "test-image.jpg",
                1024000L,
                "image/jpeg",
                "user-123",
                30,
                null
        );

        // When & Then: 404 Not Found
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Policy Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("예외: Validation 실패 - 빈 파일명")
    void createUploadSession_EmptyFileName_ValidationFailed() throws Exception {
        // Given: 빈 파일명
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "", // 빈 파일명
                1024000L,
                "image/jpeg",
                "user-123",
                30,
                null
        );

        // When & Then: 400 Bad Request with Validation Failed
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("예외: Validation 실패 - 음수 파일 크기")
    void createUploadSession_NegativeFileSize_ValidationFailed() throws Exception {
        // Given: 음수 파일 크기
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                -1L, // 음수 파일 크기
                "image/jpeg",
                "user-123",
                30,
                null
        );

        // When & Then: 400 Bad Request
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("예외: Validation 실패 - 0 이하의 만료 시간")
    void createUploadSession_ZeroExpirationMinutes_ValidationFailed() throws Exception {
        // Given: 0 이하의 만료 시간
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                1024000L,
                "image/jpeg",
                "user-123",
                0, // 0 만료 시간
                null
        );

        // When & Then: 400 Bad Request
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("예외: Validation 실패 - 빈 업로더 ID")
    void createUploadSession_EmptyUploaderId_ValidationFailed() throws Exception {
        // Given: 빈 업로더 ID
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                1024000L,
                "image/jpeg",
                "", // 빈 업로더 ID
                30,
                null
        );

        // When & Then: 400 Bad Request
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("예외: 빈 Request Body")
    void createUploadSession_EmptyRequestBody_ValidationFailed() throws Exception {
        // Given: null request body
        // When & Then: 400 Bad Request
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예외: 잘못된 JSON 형식")
    void createUploadSession_MalformedJson_BadRequest() throws Exception {
        // Given: 잘못된 JSON
        String malformedJson = "{\"policyKey\": \"b2c:CONSUMER:REVIEW\", \"fileName\": "; // 중괄호 누락

        // When & Then: 400 Bad Request
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예외: 파일 개수 초과 - b2c:CONSUMER:REVIEW 정책 (최대 5개)")
    void createUploadSession_TooManyFiles_PolicyViolation() throws Exception {
        // Given: 6개의 파일 업로드 시도 (정책 위반: 최대 5개)
        // NOTE: 이 테스트는 현재 단일 파일 업로드만 지원하므로 Skip
        // 추후 다중 파일 업로드 기능 구현 시 활성화
    }

    @Test
    @DisplayName("예외: Rate Limiting 초과 - 시간당 요청 수 제한")
    void createUploadSession_RateLimitExceeded_TooManyRequests() throws Exception {
        // Given: b2c:CONSUMER:REVIEW 정책 (시간당 100회)
        // NOTE: Rate Limiting 기능이 구현되면 활성화
        // 현재는 Skip
    }

    @Test
    @DisplayName("예외: 이미지 해상도 초과 - b2c:CONSUMER:REVIEW (최대 2048x2048)")
    void createUploadSession_ImageResolutionExceeded_PolicyViolation() throws Exception {
        // Given: 이미지 메타데이터 검증이 Presigned URL 발급 시점에 수행된다고 가정
        // NOTE: 실제 메타데이터 추출은 S3 업로드 후 이벤트 핸들러에서 수행
        // 해상도 검증은 사후 검증으로 처리되므로 이 테스트는 Skip
    }

    @Test
    @DisplayName("경계값 테스트: 정책 최대 파일 크기 정확히 일치 (10MB)")
    void createUploadSession_ExactMaxFileSize_Success() throws Exception {
        // Given: 정확히 10MB (b2c:CONSUMER:REVIEW 정책 최대값)
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "exact-10mb-image.jpg",
                10485760L, // 정확히 10MB
                "image/jpeg",
                "user-123",
                30,
                null
        );

        // When & Then: 성공 (경계값 허용)
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.fileSize").value(10485760));
    }

    @Test
    @DisplayName("경계값 테스트: 정책 최대 파일 크기 1바이트 초과 (10MB + 1)")
    void createUploadSession_ExceedMaxFileSizeByOneByte_PolicyViolation() throws Exception {
        // Given: 10MB + 1 바이트 (정책 위반)
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "10mb-plus-1byte.jpg",
                10485761L, // 10MB + 1 byte
                "image/jpeg",
                "user-123",
                30,
                null
        );

        // When & Then: 400 Bad Request with FILE_SIZE_EXCEEDED
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("FILE_SIZE_EXCEEDED"));
    }
}
