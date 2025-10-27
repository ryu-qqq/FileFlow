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
 * Scenario 10: Redis Cache 무효화 검증 E2E Test
 *
 * <p><strong>테스트 시나리오</strong>:</p>
 * <ul>
 *   <li>✅ User1에게 VIEWER Role 부여 → Permission 평가 → Cache 생성</li>
 *   <li>✅ VIEWER Role 해제 → Cache 무효화 → Permission 평가 결과 변경 (allowed → denied)</li>
 *   <li>✅ ADMIN Role 부여 → Cache 무효화 → Permission 평가 결과 변경 (denied → allowed)</li>
 * </ul>
 *
 * <p><strong>검증 대상</strong>:</p>
 * <ul>
 *   <li>RoleAssignedEvent 리스너가 Cache를 무효화하는지 확인</li>
 *   <li>RoleRevokedEvent 리스너가 Cache를 무효화하는지 확인</li>
 *   <li>Cache 무효화 후 Permission 평가 결과가 DB 기준으로 변경되는지 확인</li>
 * </ul>
 *
 * <p><strong>Cache 메커니즘</strong>:</p>
 * <ol>
 *   <li>Role 할당 → RoleAssignedEvent 발행 → GrantsCachePort.invalidateUser() 호출</li>
 *   <li>Permission 평가 → Cache Miss → DB 조회 → Cache 저장</li>
 *   <li>Role 해제 → RoleRevokedEvent 발행 → GrantsCachePort.invalidateUser() 호출</li>
 *   <li>Permission 평가 → Cache Miss → DB 조회 (변경된 Grant 정보 반영)</li>
 * </ol>
 *
 * <p><strong>Note</strong>: E2E 테스트에서는 GrantsCachePort를 No-op으로 구현하여 항상 DB 조회를 수행합니다.
 * 따라서 Cache 무효화 로직이 정상 작동하는지는 간접적으로 검증됩니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@DisplayName("Scenario 10: Redis Cache 무효화 검증 E2E Test")
class Scenario10_CacheInvalidationE2ETest extends EndToEndTestBase {

    @Autowired
    private OrganizationFixture organizationFixture;

    @Autowired
    private PermissionFixture permissionFixture;

    private Long tenantId;
    private Long organizationId;
    private Long userContext1Id;

    /**
     * 각 테스트 전 데이터 준비
     *
     * <p><strong>준비 단계</strong>:</p>
     * <ol>
     *   <li>Tenant 생성</li>
     *   <li>Organization 생성</li>
     *   <li>User1 생성</li>
     *   <li>Permission 생성 (file.read - TENANT scope, file.delete - ORGANIZATION scope)</li>
     *   <li>Role 생성 (VIEWER: file.read, ADMIN: file.delete)</li>
     * </ol>
     */
    @BeforeEach
    void setUpTestData() throws Exception {
        // 1. Tenant 생성
        String uniqueName = "test-tenant-scenario10-" + System.nanoTime();
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
        String orgCode = "S10-" + (timestamp % 100000000);  // 최대 14자 (20자 제한)
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
                      "externalUserId": "google-oauth2-user1-scenario10-%d",
                      "email": "user1-scenario10-%d@test.com"
                    }
                    """.formatted(timestamp, timestamp)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        userContext1Id = ((Number) JsonPath.read(user1Response, "$.data.userContextId")).longValue();

        // 4. UserOrgMembership 생성
        organizationFixture.createUserOrgMembership(userContext1Id, organizationId, tenantId);

        // 5. Permission 생성
        permissionFixture.createPermission("file.read", "파일 조회 권한", "TENANT");
        permissionFixture.createPermission("file.delete", "파일 삭제 권한", "ORGANIZATION");

        // 6. Role 생성
        permissionFixture.createRole("VIEWER", "조회자 역할");
        permissionFixture.linkRolePermission("VIEWER", "file.read");

        permissionFixture.createRole("ADMIN", "관리자 역할");
        permissionFixture.linkRolePermission("ADMIN", "file.delete");
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
     * Test 1: Role 할당 후 Permission 평가 - Cache 생성
     *
     * <p><strong>Given</strong>: User1에게 VIEWER Role 부여됨</p>
     * <p><strong>When</strong>: User1이 file.read 권한 평가 요청</p>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>200 OK 응답</li>
     *   <li>allowed = true</li>
     *   <li>denialReason = null</li>
     *   <li>message = "권한이 허용되었습니다"</li>
     * </ul>
     *
     * <p><strong>Cache 동작</strong>:</p>
     * <ul>
     *   <li>Role 할당 → RoleAssignedEvent 발행 → GrantsCachePort.invalidateUser()</li>
     *   <li>Permission 평가 → Cache Miss → DB 조회 → Cache 저장</li>
     * </ul>
     */
    @Test
    @DisplayName("Role 할당 후 Permission 평가 - Cache 생성")
    void cacheInvalidation_RoleAssigned_PermissionAllowed() throws Exception {
        // Given: User1에게 VIEWER Role 할당
        permissionFixture.assignRoleToUser(userContext1Id, "VIEWER", tenantId, organizationId);

        // When & Then: file.read 권한 평가 → allowed
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
     * Test 2: Role 해제 후 Permission 평가 - Cache 무효화 검증
     *
     * <p><strong>Given</strong>:</p>
     * <ul>
     *   <li>User1에게 VIEWER Role 부여됨</li>
     *   <li>Permission 평가로 Cache 생성됨</li>
     * </ul>
     * <p><strong>When</strong>:</p>
     * <ul>
     *   <li>VIEWER Role 해제</li>
     *   <li>다시 file.read 권한 평가 요청</li>
     * </ul>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>200 OK 응답</li>
     *   <li>allowed = false</li>
     *   <li>denialReason = "NO_GRANT"</li>
     *   <li>message = "권한이 부여되지 않았습니다"</li>
     * </ul>
     *
     * <p><strong>Cache 동작</strong>:</p>
     * <ul>
     *   <li>Role 해제 → RoleRevokedEvent 발행 → GrantsCachePort.invalidateUser()</li>
     *   <li>Permission 평가 → Cache Miss → DB 조회 (Grant 없음) → NO_GRANT</li>
     * </ul>
     */
    @Test
    @DisplayName("Role 해제 후 Permission 평가 - Cache 무효화 검증")
    void cacheInvalidation_RoleRevoked_PermissionDenied() throws Exception {
        // Given: User1에게 VIEWER Role 할당 및 Cache 생성
        permissionFixture.assignRoleToUser(userContext1Id, "VIEWER", tenantId, organizationId);

        mockMvc.perform(get("/api/v1/permissions/evaluate")
                .param("userId", userContext1Id.toString())
                .param("tenantId", tenantId.toString())
                .param("organizationId", organizationId.toString())
                .param("permissionCode", "file.read")
                .param("scope", "TENANT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.allowed").value(true));

        // When: VIEWER Role 해제
        permissionFixture.revokeRoleFromUser(userContext1Id, "VIEWER", tenantId, organizationId);

        // Then: file.read 권한 평가 → denied (Cache 무효화로 DB 재조회)
        mockMvc.perform(get("/api/v1/permissions/evaluate")
                .param("userId", userContext1Id.toString())
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
     * Test 3: 다른 Role 할당 후 Permission 평가 - 새 Grant 적용 확인
     *
     * <p><strong>Given</strong>:</p>
     * <ul>
     *   <li>User1에게 VIEWER Role 부여 → 해제됨</li>
     *   <li>file.read 권한 없음 (NO_GRANT)</li>
     * </ul>
     * <p><strong>When</strong>:</p>
     * <ul>
     *   <li>ADMIN Role 부여 (file.delete 권한)</li>
     *   <li>file.delete 권한 평가 요청</li>
     * </ul>
     * <p><strong>Then</strong>:</p>
     * <ul>
     *   <li>200 OK 응답</li>
     *   <li>allowed = true</li>
     *   <li>denialReason = null</li>
     *   <li>message = "권한이 허용되었습니다"</li>
     * </ul>
     *
     * <p><strong>Cache 동작</strong>:</p>
     * <ul>
     *   <li>ADMIN Role 할당 → RoleAssignedEvent 발행 → GrantsCachePort.invalidateUser()</li>
     *   <li>Permission 평가 → Cache Miss → DB 조회 (새 Grant) → allowed</li>
     * </ul>
     */
    @Test
    @DisplayName("다른 Role 할당 후 Permission 평가 - 새 Grant 적용 확인")
    void cacheInvalidation_NewRoleAssigned_NewPermissionAllowed() throws Exception {
        // Given: User1에게 VIEWER Role 할당 → 해제
        permissionFixture.assignRoleToUser(userContext1Id, "VIEWER", tenantId, organizationId);
        permissionFixture.revokeRoleFromUser(userContext1Id, "VIEWER", tenantId, organizationId);

        // When: ADMIN Role 부여 (file.delete 권한)
        permissionFixture.assignRoleToUser(userContext1Id, "ADMIN", tenantId, organizationId);

        // Then: file.delete 권한 평가 → allowed (Cache 무효화로 DB 재조회)
        mockMvc.perform(get("/api/v1/permissions/evaluate")
                .param("userId", userContext1Id.toString())
                .param("tenantId", tenantId.toString())
                .param("organizationId", organizationId.toString())
                .param("permissionCode", "file.delete")
                .param("scope", "ORGANIZATION"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.allowed").value(true))
            .andExpect(jsonPath("$.data.denialReason").isEmpty())
            .andExpect(jsonPath("$.data.message").value("권한이 허용되었습니다"));
    }
}
