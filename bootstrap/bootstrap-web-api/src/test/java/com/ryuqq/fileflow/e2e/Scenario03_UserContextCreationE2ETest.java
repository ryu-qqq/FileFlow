package com.ryuqq.fileflow.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Scenario 3: UserContext 생성 및 중복 방지 E2E Test
 *
 * <p><strong>테스트 시나리오</strong>:</p>
 * <ul>
 *   <li>✅ UserContext 생성 (OAuth 로그인 후 최초 사용자 등록)</li>
 *   <li>✅ 중복 externalUserId 검증 (409 Conflict)</li>
 *   <li>✅ Email 형식 검증 (400 Bad Request)</li>
 * </ul>
 *
 * <p><strong>API Endpoint</strong>:</p>
 * <ul>
 *   <li>POST /api/v1/user-contexts - UserContext 생성</li>
 * </ul>
 *
 * <p><strong>검증 항목</strong>:</p>
 * <ul>
 *   <li>UserContext 생성 시 201 Created 반환</li>
 *   <li>externalUserId 중복 시 409 Conflict 반환</li>
 *   <li>잘못된 Email 형식 시 400 Bad Request 반환</li>
 *   <li>Response에 userContextId, externalUserId, email, deleted, createdAt, updatedAt 포함</li>
 * </ul>
 *
 * <p><strong>Note</strong>: Phase 2 완료 후 Role 할당/조회 API로 시나리오 확장 예정</p>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@DisplayName("Scenario 3: UserContext 생성 및 중복 방지 E2E Test")
class Scenario03_UserContextCreationE2ETest extends EndToEndTestBase {

    /**
     * Test 1: UserContext 생성 - 정상 플로우
     *
     * <p><strong>Given</strong>: OAuth 로그인 성공 후 사용자 정보</p>
     * <p><strong>When</strong>: UserContext 생성 API 호출</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>201 Created 응답</li>
     *   <li>userContextId 생성됨 (not null)</li>
     *   <li>externalUserId, email 정확히 저장됨</li>
     *   <li>deleted = false</li>
     *   <li>createdAt, updatedAt 자동 생성됨</li>
     * </ul>
     */
    @Test
    @DisplayName("UserContext 생성 - 정상 플로우")
    void createUserContext_Success() throws Exception {
        // Given
        String externalUserId = "google-oauth2-123456789";
        String email = "testuser@example.com";

        String requestBody = """
            {
              "externalUserId": "%s",
              "email": "%s"
            }
            """.formatted(externalUserId, email);

        // When & Then
        mockMvc.perform(post("/api/v1/user-contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userContextId").isNumber())
            .andExpect(jsonPath("$.data.externalUserId").value(externalUserId))
            .andExpect(jsonPath("$.data.email").value(email))
            .andExpect(jsonPath("$.data.deleted").value(false))
            .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
            .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
    }

    /**
     * Test 2: 중복 externalUserId 검증 - 409 Conflict
     *
     * <p><strong>Given</strong>: 이미 등록된 externalUserId</p>
     * <p><strong>When</strong>: 동일한 externalUserId로 재생성 시도</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>409 Conflict 응답</li>
     *   <li>success = false</li>
     *   <li>에러 메시지 포함</li>
     * </ul>
     */
    @Test
    @DisplayName("중복 externalUserId 검증 - 409 Conflict")
    void createUserContext_DuplicateExternalUserId_Returns409() throws Exception {
        // Given - 첫 번째 UserContext 생성
        String externalUserId = "google-oauth2-duplicate-test";
        String email1 = "first@example.com";

        String firstRequestBody = """
            {
              "externalUserId": "%s",
              "email": "%s"
            }
            """.formatted(externalUserId, email1);

        mockMvc.perform(post("/api/v1/user-contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstRequestBody))
            .andExpect(status().isCreated());

        // When - 동일한 externalUserId로 재생성 시도
        String email2 = "second@example.com";
        String duplicateRequestBody = """
            {
              "externalUserId": "%s",
              "email": "%s"
            }
            """.formatted(externalUserId, email2);

        // Then - 409 Conflict (RFC 7807 ProblemDetail)
        mockMvc.perform(post("/api/v1/user-contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateRequestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    /**
     * Test 3: Email 형식 검증 - 400 Bad Request
     *
     * <p><strong>Given</strong>: 잘못된 Email 형식</p>
     * <p><strong>When</strong>: UserContext 생성 API 호출</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>400 Bad Request 응답</li>
     *   <li>RFC 7807 ProblemDetail 형식</li>
     *   <li>Email 형식 오류 메시지 포함</li>
     * </ul>
     */
    @Test
    @DisplayName("Email 형식 검증 - 400 Bad Request")
    void createUserContext_InvalidEmailFormat_Returns400() throws Exception {
        // Given
        String externalUserId = "google-oauth2-invalid-email-test";
        String invalidEmail = "not-an-email";

        String requestBody = """
            {
              "externalUserId": "%s",
              "email": "%s"
            }
            """.formatted(externalUserId, invalidEmail);

        // When & Then (RFC 7807 ProblemDetail)
        mockMvc.perform(post("/api/v1/user-contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    /**
     * Test 4: 빈 externalUserId 검증 - 400 Bad Request
     *
     * <p><strong>Given</strong>: 빈 externalUserId</p>
     * <p><strong>When</strong>: UserContext 생성 API 호출</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>400 Bad Request 응답</li>
     *   <li>RFC 7807 ProblemDetail 형식</li>
     *   <li>필수 필드 오류 메시지 포함</li>
     * </ul>
     */
    @Test
    @DisplayName("빈 externalUserId 검증 - 400 Bad Request")
    void createUserContext_BlankExternalUserId_Returns400() throws Exception {
        // Given
        String blankExternalUserId = "";
        String email = "valid@example.com";

        String requestBody = """
            {
              "externalUserId": "%s",
              "email": "%s"
            }
            """.formatted(blankExternalUserId, email);

        // When & Then (RFC 7807 ProblemDetail)
        mockMvc.perform(post("/api/v1/user-contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").isNotEmpty());
    }
}
