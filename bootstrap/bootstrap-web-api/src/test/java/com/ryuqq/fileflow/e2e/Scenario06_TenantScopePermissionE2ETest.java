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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Scenario 6: TENANT Scope 권한 테스트 E2E Test
 *
 * <p><strong>테스트 시나리오</strong>:</p>
 * <ul>
 *   <li>✅ User1에게 VIEWER Role 부여 (file.read permission, TENANT scope)</li>
 *   <li>✅ User1의 file.read 권한 평가 (TENANT scope) → 허용</li>
 *   <li>✅ User2의 file.read 권한 평가 → 거부 (NO_GRANT)</li>
 *   <li>✅ User1의 ORGANIZATION Scope 요청 → 허용 (TENANT Scope는 ORGANIZATION을 포함)</li>
 *   <li>✅ User1의 SELF Scope 요청 → 허용 (TENANT Scope는 SELF를 포함)</li>
 * </ul>
 *
 * <p><strong>TENANT Scope 정의</strong>:</p>
 * <ul>
 *   <li>같은 Tenant 내 모든 리소스에 접근 가능 (최상위 Scope)</li>
 *   <li>ORGANIZATION Scope 요청도 허용 (TENANT ⊇ ORGANIZATION)</li>
 *   <li>SELF Scope 요청도 허용 (TENANT ⊇ SELF)</li>
 *   <li>예시: 같은 테넌트 내 모든 파일 조회 가능</li>
 * </ul>
 *
 * <p><strong>Scope 계층</strong>: SELF < ORGANIZATION < TENANT</p>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@DisplayName("Scenario 6: TENANT Scope 권한 테스트 E2E Test")
class Scenario06_TenantScopePermissionE2ETest extends EndToEndTestBase {


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
     *   <li>Permission 생성 (file.read - TENANT scope)</li>
     *   <li>Role 생성 (VIEWER: file.read 부여)</li>
     *   <li>User1에게 VIEWER Role 할당</li>
     * </ol>
     */
    @BeforeEach
    void setUpTestData() throws Exception {
        // 1. Tenant 생성
        String uniqueName = "test-tenant-scenario6-" + System.nanoTime();
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
        String orgCode = "S6-" + (timestamp % 100000000);  // 최대 13자 (20자 제한)
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
                      "externalUserId": "google-oauth2-user1-scenario6-%d",
                      "email": "user1-scenario6-%d@test.com"
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
                      "externalUserId": "google-oauth2-user2-scenario6-%d",
                      "email": "user2-scenario6-%d@test.com"
                    }
                    """.formatted(timestamp, timestamp)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        userContext2Id = ((Number) JsonPath.read(user2Response, "$.data.userContextId")).longValue();

        // 5. UserOrgMembership 생성 (User1, User2 모두 같은 Organization 소속)
        organizationFixture.createUserOrgMembership(userContext1Id, organizationId, tenantId);
        organizationFixture.createUserOrgMembership(userContext2Id, organizationId, tenantId);

        // 6. Permission 생성
        permissionFixture.createPermission("file.read", "파일 조회 권한", "TENANT");

        // 7. Role 생성 (VIEWER: file.read 부여)
        permissionFixture.createRole("VIEWER", "조회자 역할");
        permissionFixture.linkRolePermission("VIEWER", "file.read");

        // 8. User1에게 VIEWER Role 할당
        permissionFixture.assignRoleToUser(userContext1Id, "VIEWER", tenantId, organizationId);
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
     * Test 1: User1의 file.read 권한 평가 - TENANT Scope 통과
     *
     * <p><strong>Given</strong>: User1에게 VIEWER Role (file.read, TENANT) 부여됨</p>
     * <p><strong>When</strong>: User1이 file.read 권한 평가 요청 (scope=TENANT)</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>200 OK 응답</li>
     *   <li>allowed = true</li>
     *   <li>denialReason = null</li>
     *   <li>message = "권한이 허용되었습니다"</li>
     * </ul>
     */
    @Test
    @DisplayName("User1의 file.read 권한 평가 - TENANT Scope 통과")
    void evaluatePermission_User1_FileRead_TenantScope_Allowed() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/permissions/evaluate")
                .param("userId", userContext1Id.toString())
                .param("tenantId", tenantId.toString())
                .param("organizationId", organizationId.toString())
                .param("permissionCode", "file.read")
                .param("scope", "TENANT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.allowed").value(true))
            .andExpect(jsonPath("$.data.denialReason").isEmpty())
            .andExpect(jsonPath("$.data.message").value("권한이 허용되었습니다"));
    }

    /**
     * Test 2: User2의 file.read 권한 평가 - NO_GRANT 거부
     *
     * <p><strong>Given</strong>: User2에게는 아무 Role도 부여되지 않음</p>
     * <p><strong>When</strong>: User2가 file.read 권한 평가 요청 (scope=TENANT)</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>200 OK 응답</li>
     *   <li>allowed = false</li>
     *   <li>denialReason = "NO_GRANT"</li>
     *   <li>message = "권한이 부여되지 않았습니다"</li>
     * </ul>
     */
    @Test
    @DisplayName("User2의 file.read 권한 평가 - NO_GRANT 거부")
    void evaluatePermission_User2_FileRead_NoGrant_Denied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/permissions/evaluate")
                .param("userId", userContext2Id.toString())
                .param("tenantId", tenantId.toString())
                .param("organizationId", organizationId.toString())
                .param("permissionCode", "file.read")
                .param("scope", "TENANT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.allowed").value(false))
            .andExpect(jsonPath("$.data.denialReason").value("NO_GRANT"))
            .andExpect(jsonPath("$.data.message").value("사용자에게 file.read 권한이 부여되지 않았습니다"));
    }

    /**
     * Test 3: User1의 file.read 권한 평가 - ORGANIZATION Scope 요청 시 허용
     *
     * <p><strong>Given</strong>: User1에게 file.read (TENANT Scope) 권한 부여됨</p>
     * <p><strong>When</strong>: User1이 file.read 권한을 ORGANIZATION Scope로 요청</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>200 OK 응답</li>
     *   <li>allowed = true</li>
     *   <li>denialReason = null</li>
     *   <li>message = "권한이 허용되었습니다"</li>
     * </ul>
     *
     * <p><strong>Scope 계층</strong>: TENANT Scope는 ORGANIZATION Scope를 포함합니다 (TENANT ⊇ ORGANIZATION)</p>
     */
    @Test
    @DisplayName("User1의 file.read 권한 평가 - ORGANIZATION Scope 요청 시 허용")
    void evaluatePermission_User1_FileRead_OrganizationScope_Allowed() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/permissions/evaluate")
                .param("userId", userContext1Id.toString())
                .param("tenantId", tenantId.toString())
                .param("organizationId", organizationId.toString())
                .param("permissionCode", "file.read")
                .param("scope", "ORGANIZATION"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.allowed").value(true))
            .andExpect(jsonPath("$.data.denialReason").isEmpty())
            .andExpect(jsonPath("$.data.message").value("권한이 허용되었습니다"));
    }

    /**
     * Test 4: User1의 file.read 권한 평가 - SELF Scope 요청 시 허용
     *
     * <p><strong>Given</strong>: User1에게 file.read (TENANT Scope) 권한 부여됨</p>
     * <p><strong>When</strong>: User1이 file.read 권한을 SELF Scope로 요청</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>200 OK 응답</li>
     *   <li>allowed = true</li>
     *   <li>denialReason = null</li>
     *   <li>message = "권한이 허용되었습니다"</li>
     * </ul>
     *
     * <p><strong>Scope 계층</strong>: TENANT Scope는 SELF Scope를 포함합니다 (TENANT ⊇ SELF)</p>
     * <p>TENANT는 최상위 Scope로, 모든 하위 Scope (ORGANIZATION, SELF)를 포함합니다.</p>
     */
    @Test
    @DisplayName("User1의 file.read 권한 평가 - SELF Scope 요청 시 허용")
    void evaluatePermission_User1_FileRead_SelfScope_Allowed() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/permissions/evaluate")
                .param("userId", userContext1Id.toString())
                .param("tenantId", tenantId.toString())
                .param("organizationId", organizationId.toString())
                .param("permissionCode", "file.read")
                .param("scope", "SELF"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.allowed").value(true))
            .andExpect(jsonPath("$.data.denialReason").isEmpty())
            .andExpect(jsonPath("$.data.message").value("권한이 허용되었습니다"));
    }
}
