package com.ryuqq.fileflow.e2e;

import com.jayway.jsonpath.JsonPath;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.CreateOrganizationRequest;
import com.ryuqq.fileflow.e2e.fixture.OrganizationFixture;
import com.ryuqq.fileflow.e2e.fixture.PermissionFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Scenario 4: SELF Scope 권한 테스트 E2E Test
 *
 * <p><strong>테스트 시나리오</strong>:</p>
 * <ul>
 *   <li>✅ User1에게 UPLOADER Role 부여 (file.upload permission, SELF scope)</li>
 *   <li>✅ User1의 file.upload 권한 평가 → 허용 (SELF scope 통과)</li>
 *   <li>✅ User2의 file.upload 권한 평가 → 거부 (NO_GRANT)</li>
 *   <li>✅ User1의 file.delete 권한 평가 → 거부 (NO_GRANT, 부여되지 않은 권한)</li>
 * </ul>
 *
 * <p><strong>API Endpoint</strong>:</p>
 * <ul>
 *   <li>GET /api/v1/permissions/evaluate - Permission 평가 (ABAC 엔진)</li>
 * </ul>
 *
 * <p><strong>권한 평가 파이프라인 (4단계)</strong>:</p>
 * <ol>
 *   <li>Cache Lookup - user:tenant:org 키로 Grants 조회</li>
 *   <li>Permission 필터링 - permissionCode 일치 확인</li>
 *   <li>Scope 매칭 - Grant의 Scope가 요청 Scope를 포함하는지 검증</li>
 *   <li>ABAC 평가 - CEL 조건식 평가 (조건이 있는 경우)</li>
 * </ol>
 *
 * <p><strong>SELF Scope 정의</strong>:</p>
 * <ul>
 *   <li>사용자 자신의 리소스에만 접근 가능</li>
 *   <li>예시: 자신이 업로드한 파일만 수정/삭제 가능</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@DisplayName("Scenario 4: SELF Scope 권한 테스트 E2E Test")
class Scenario04_SelfScopePermissionE2ETest extends EndToEndTestBase {


    @Autowired
    private OrganizationFixture organizationFixture;

    @Autowired
    private PermissionFixture permissionFixture;

    private Long tenantId;
    private Long organizationId;
    private Long userContext1Id;
    private Long userContext2Id;

    /**
     * 각 테스트 전 데이터 준비
     *
     * <p><strong>준비 단계</strong>:</p>
     * <ol>
     *   <li>Tenant 생성</li>
     *   <li>Organization 생성</li>
     *   <li>User1, User2 생성 (같은 Organization 소속)</li>
     *   <li>Permission 생성 (file.upload, file.delete)</li>
     *   <li>Role 생성 (UPLOADER: file.upload만 부여)</li>
     *   <li>User1에게 UPLOADER Role 할당</li>
     * </ol>
     */
    @BeforeEach
    void setUpTestData() throws Exception {
        // 1. Tenant 생성
        String uniqueName = "test-tenant-scenario4-" + System.nanoTime();
        String tenantResponse = mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s"
                    }
                    """.formatted(uniqueName)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        tenantId = ((Number) JsonPath.read(tenantResponse, "$.data.tenantId")).longValue();

        // 2. Organization 생성
        long timestamp = System.nanoTime();
        String orgCode = "S4-" + (timestamp % 100000000);  // 최대 13자 (20자 제한)
        CreateOrganizationRequest orgRequest = OrganizationFixture.createRequest(
            tenantId,
            orgCode
        );
        String orgResponse = mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orgRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        organizationId = ((Number) JsonPath.read(orgResponse, "$.data.organizationId")).longValue();

        // 3. UserContext1 생성 (User1)
        String user1Response = mockMvc.perform(post("/api/v1/user-contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "externalUserId": "google-oauth2-user1-scenario4-%d",
                      "email": "user1-scenario4-%d@test.com"
                    }
                    """.formatted(timestamp, timestamp)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        userContext1Id = ((Number) JsonPath.read(user1Response, "$.data.userContextId")).longValue();

        // 4. UserContext2 생성 (User2)
        String user2Response = mockMvc.perform(post("/api/v1/user-contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "externalUserId": "google-oauth2-user2-scenario4-%d",
                      "email": "user2-scenario4-%d@test.com"
                    }
                    """.formatted(timestamp, timestamp)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        userContext2Id = ((Number) JsonPath.read(user2Response, "$.data.userContextId")).longValue();

        // 5. UserOrgMembership 생성 (User1, User2 모두 같은 Organization 소속)
        organizationFixture.createUserOrgMembership(userContext1Id, organizationId, tenantId);
        organizationFixture.createUserOrgMembership(userContext2Id, organizationId, tenantId);

        // 6. Permission 생성
        permissionFixture.createPermission("file.upload", "파일 업로드 권한", "SELF");
        permissionFixture.createPermission("file.delete", "파일 삭제 권한", "ORGANIZATION");

        // 7. Role 생성 (UPLOADER: file.upload만 부여)
        permissionFixture.createRole("UPLOADER", "파일 업로드 역할");
        permissionFixture.linkRolePermission("UPLOADER", "file.upload");

        // 8. User1에게 UPLOADER Role 할당
        permissionFixture.assignRoleToUser(userContext1Id, "UPLOADER", tenantId, organizationId);
    }

    /**
     * 각 테스트 후 데이터 정리
     *
     * <p>테스트 격리를 위해 모든 Permission 관련 데이터를 삭제합니다.</p>
     */
    @AfterEach
    void tearDown() {
        permissionFixture.cleanupAll();
    }

    /**
     * Test 1: User1의 file.upload 권한 평가 - SELF Scope 통과
     *
     * <p><strong>Given</strong>: User1에게 UPLOADER Role (file.upload, SELF) 부여됨</p>
     * <p><strong>When</strong>: User1이 file.upload 권한 평가 요청 (scope=SELF)</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>200 OK 응답</li>
     *   <li>allowed = true</li>
     *   <li>denialReason = null</li>
     *   <li>message = "권한이 허용되었습니다"</li>
     * </ul>
     */
    @Test
    @DisplayName("User1의 file.upload 권한 평가 - SELF Scope 통과")
    void evaluatePermission_User1_FileUpload_SelfScope_Allowed() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/permissions/evaluate")
                .param("userId", userContext1Id.toString())
                .param("tenantId", tenantId.toString())
                .param("organizationId", organizationId.toString())
                .param("permissionCode", "file.upload")
                .param("scope", "SELF"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.allowed").value(true))
            .andExpect(jsonPath("$.data.denialReason").isEmpty())
            .andExpect(jsonPath("$.data.message").value("권한이 허용되었습니다"));
    }

    /**
     * Test 2: User2의 file.upload 권한 평가 - NO_GRANT 거부
     *
     * <p><strong>Given</strong>: User2에게는 아무 Role도 부여되지 않음</p>
     * <p><strong>When</strong>: User2가 file.upload 권한 평가 요청 (scope=SELF)</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>200 OK 응답</li>
     *   <li>allowed = false</li>
     *   <li>denialReason = "NO_GRANT"</li>
     *   <li>message = "권한이 부여되지 않았습니다"</li>
     * </ul>
     */
    @Test
    @DisplayName("User2의 file.upload 권한 평가 - NO_GRANT 거부")
    void evaluatePermission_User2_FileUpload_NoGrant_Denied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/permissions/evaluate")
                .param("userId", userContext2Id.toString())
                .param("tenantId", tenantId.toString())
                .param("organizationId", organizationId.toString())
                .param("permissionCode", "file.upload")
                .param("scope", "SELF"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.allowed").value(false))
            .andExpect(jsonPath("$.data.denialReason").value("NO_GRANT"))
            .andExpect(jsonPath("$.data.message").value("사용자에게 file.upload 권한이 부여되지 않았습니다"));
    }

    /**
     * Test 3: User1의 file.delete 권한 평가 - NO_GRANT 거부 (부여되지 않은 권한)
     *
     * <p><strong>Given</strong>: User1에게 UPLOADER Role (file.upload만 부여)</p>
     * <p><strong>When</strong>: User1이 file.delete 권한 평가 요청</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>200 OK 응답</li>
     *   <li>allowed = false</li>
     *   <li>denialReason = "NO_GRANT"</li>
     *   <li>message = "권한이 부여되지 않았습니다"</li>
     * </ul>
     */
    @Test
    @DisplayName("User1의 file.delete 권한 평가 - NO_GRANT 거부 (부여되지 않은 권한)")
    void evaluatePermission_User1_FileDelete_NoGrant_Denied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/permissions/evaluate")
                .param("userId", userContext1Id.toString())
                .param("tenantId", tenantId.toString())
                .param("organizationId", organizationId.toString())
                .param("permissionCode", "file.delete")
                .param("scope", "ORGANIZATION"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.allowed").value(false))
            .andExpect(jsonPath("$.data.denialReason").value("NO_GRANT"))
            .andExpect(jsonPath("$.data.message").value("사용자에게 file.delete 권한이 부여되지 않았습니다"));
    }

    /**
     * Test 4: User1의 file.upload 권한 평가 - ORGANIZATION Scope 요청 시 SCOPE_MISMATCH
     *
     * <p><strong>Given</strong>: User1에게 file.upload (SELF Scope) 권한 부여됨</p>
     * <p><strong>When</strong>: User1이 file.upload 권한을 ORGANIZATION Scope로 요청</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>200 OK 응답</li>
     *   <li>allowed = false</li>
     *   <li>denialReason = "SCOPE_MISMATCH"</li>
     *   <li>message = "요청된 Scope가 부여된 Scope 범위를 초과합니다"</li>
     * </ul>
     *
     * <p><strong>Scope 계층</strong>: SELF < ORGANIZATION < TENANT</p>
     * <p>SELF Scope 권한으로는 ORGANIZATION Scope 요청을 충족할 수 없습니다.</p>
     */
    @Test
    @DisplayName("User1의 file.upload 권한 평가 - ORGANIZATION Scope 요청 시 SCOPE_MISMATCH")
    void evaluatePermission_User1_FileUpload_OrganizationScope_ScopeMismatch() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/permissions/evaluate")
                .param("userId", userContext1Id.toString())
                .param("tenantId", tenantId.toString())
                .param("organizationId", organizationId.toString())
                .param("permissionCode", "file.upload")
                .param("scope", "ORGANIZATION"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.allowed").value(false))
            .andExpect(jsonPath("$.data.denialReason").value("SCOPE_MISMATCH"))
            .andExpect(jsonPath("$.data.message").value("file.upload 권한 범위(SELF)로 ORGANIZATION 범위 작업을 수행할 수 없습니다"));
    }
}
