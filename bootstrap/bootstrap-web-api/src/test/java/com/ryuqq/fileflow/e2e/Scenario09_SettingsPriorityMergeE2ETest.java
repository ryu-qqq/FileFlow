package com.ryuqq.fileflow.e2e;

import com.jayway.jsonpath.JsonPath;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.CreateOrganizationRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.CreateTenantRequest;
import com.ryuqq.fileflow.adapter.rest.settings.dto.UpdateSettingRequest;
import com.ryuqq.fileflow.e2e.fixture.OrganizationFixture;
import com.ryuqq.fileflow.e2e.fixture.TenantFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Scenario09_SettingsPriorityMergeE2ETest - 시나리오 9: 설정 우선순위 병합 테스트
 *
 * <p><strong>테스트 시나리오:</strong></p>
 * <ol>
 *   <li>DEFAULT 레벨 설정 생성 (MAX_UPLOAD_SIZE=100MB)</li>
 *   <li>Tenant 생성 후 TENANT 레벨 설정 생성 (MAX_UPLOAD_SIZE=50MB)</li>
 *   <li>Organization 생성 후 ORG 레벨 설정 생성 (MAX_UPLOAD_SIZE=200MB)</li>
 *   <li>설정 조회 시 우선순위 확인: ORG (200MB) > TENANT (50MB) > DEFAULT (100MB)</li>
 * </ol>
 *
 * <p><strong>검증 사항:</strong></p>
 * <ul>
 *   <li>✅ ORG + TENANT + DEFAULT → ORG 설정 값 반환 (200MB)</li>
 *   <li>✅ TENANT + DEFAULT (ORG 없음) → TENANT 설정 값 반환 (50MB)</li>
 *   <li>✅ DEFAULT만 (ORG, TENANT 없음) → DEFAULT 설정 값 반환 (100MB)</li>
 *   <li>✅ 병합 우선순위: ORG > TENANT > DEFAULT</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@DisplayName("시나리오 9: 설정 우선순위 병합 E2E 테스트")
class Scenario09_SettingsPriorityMergeE2ETest extends EndToEndTestBase {

    @Test
    @DisplayName("ORG > TENANT > DEFAULT 3레벨 병합 우선순위가 정상 동작한다")
    void settingsPriorityMerge_ThreeLevels_Success() throws Exception {
        // 1. DEFAULT 레벨 설정 생성 (MAX_UPLOAD_SIZE=100MB)
        UpdateSettingRequest defaultSettingRequest = new UpdateSettingRequest(
            "MAX_UPLOAD_SIZE", "100MB", "DEFAULT", null
        );
        mockMvc.perform(patch("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(defaultSettingRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.value").value("100MB"))
            .andExpect(jsonPath("$.data.level").value("DEFAULT"));

        // 2. Tenant 생성
        CreateTenantRequest tenantRequest = TenantFixture.createRequest();
        MvcResult tenantResult = mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenantRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long tenantId = ((Number) JsonPath.read(tenantResult.getResponse().getContentAsString(), "$.data.tenantId")).longValue();

        // 3. TENANT 레벨 설정 생성 (MAX_UPLOAD_SIZE=50MB)
        UpdateSettingRequest tenantSettingRequest = new UpdateSettingRequest(
            "MAX_UPLOAD_SIZE", "50MB", "TENANT", tenantId
        );
        mockMvc.perform(patch("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenantSettingRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.value").value("50MB"))
            .andExpect(jsonPath("$.data.level").value("TENANT"));

        // 4. Organization 생성
        CreateOrganizationRequest orgRequest = OrganizationFixture.createRequest(tenantId);
        MvcResult orgResult = mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orgRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long orgId = ((Number) JsonPath.read(orgResult.getResponse().getContentAsString(), "$.data.organizationId")).longValue();

        // 5. ORG 레벨 설정 생성 (MAX_UPLOAD_SIZE=200MB)
        UpdateSettingRequest orgSettingRequest = new UpdateSettingRequest(
            "MAX_UPLOAD_SIZE", "200MB", "ORG", orgId
        );
        mockMvc.perform(patch("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orgSettingRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.value").value("200MB"))
            .andExpect(jsonPath("$.data.level").value("ORG"));

        // 6. ORG + TENANT + DEFAULT 병합 조회 → ORG 우선 (200MB)
        mockMvc.perform(get("/api/v1/settings")
                .param("orgId", orgId.toString())
                .param("tenantId", tenantId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.settings.MAX_UPLOAD_SIZE").value("200MB"));

        // 7. TENANT + DEFAULT 병합 조회 (ORG 없음) → TENANT 우선 (50MB)
        mockMvc.perform(get("/api/v1/settings")
                .param("tenantId", tenantId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.settings.MAX_UPLOAD_SIZE").value("50MB"));

        // 8. DEFAULT만 조회 → DEFAULT 값 (100MB)
        mockMvc.perform(get("/api/v1/settings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.settings.MAX_UPLOAD_SIZE").value("100MB"));
    }

    @Test
    @DisplayName("ORG 레벨에만 설정이 있고 TENANT, DEFAULT에 없으면 ORG 값만 반환")
    void settingsPriorityMerge_OrgOnly_ReturnsOrgValue() throws Exception {
        // 1. Tenant 생성
        CreateTenantRequest tenantRequest = TenantFixture.createRequest();
        MvcResult tenantResult = mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenantRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long tenantId = ((Number) JsonPath.read(tenantResult.getResponse().getContentAsString(), "$.data.tenantId")).longValue();

        // 2. Organization 생성
        CreateOrganizationRequest orgRequest = OrganizationFixture.createRequest(tenantId);
        MvcResult orgResult = mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orgRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long orgId = ((Number) JsonPath.read(orgResult.getResponse().getContentAsString(), "$.data.organizationId")).longValue();

        // 3. ORG 레벨 전용 설정 생성 (ORG_SPECIFIC_KEY=org-value)
        UpdateSettingRequest orgSettingRequest = new UpdateSettingRequest(
            "ORG_SPECIFIC_KEY", "org-value", "ORG", orgId
        );
        mockMvc.perform(patch("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orgSettingRequest)))
            .andExpect(status().isOk());

        // 4. ORG + TENANT 병합 조회 → ORG 값만 반환
        mockMvc.perform(get("/api/v1/settings")
                .param("orgId", orgId.toString())
                .param("tenantId", tenantId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.settings.ORG_SPECIFIC_KEY").value("org-value"));
    }

    @Test
    @DisplayName("여러 설정 키의 병합 우선순위가 독립적으로 동작한다")
    void settingsPriorityMerge_MultipleKeys_IndependentPriority() throws Exception {
        // 1. Tenant 생성
        CreateTenantRequest tenantRequest = TenantFixture.createRequest();
        MvcResult tenantResult = mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenantRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long tenantId = ((Number) JsonPath.read(tenantResult.getResponse().getContentAsString(), "$.data.tenantId")).longValue();

        // 2. Organization 생성
        CreateOrganizationRequest orgRequest = OrganizationFixture.createRequest(tenantId);
        MvcResult orgResult = mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orgRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long orgId = ((Number) JsonPath.read(orgResult.getResponse().getContentAsString(), "$.data.organizationId")).longValue();

        // 3. 여러 레벨에 다양한 설정 생성
        // DEFAULT: KEY_A=default-a, KEY_B=default-b, KEY_C=default-c
        mockMvc.perform(patch("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new UpdateSettingRequest("KEY_A", "default-a", "DEFAULT", null))))
            .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new UpdateSettingRequest("KEY_B", "default-b", "DEFAULT", null))))
            .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new UpdateSettingRequest("KEY_C", "default-c", "DEFAULT", null))))
            .andExpect(status().isOk());

        // TENANT: KEY_A=tenant-a (KEY_B, KEY_C는 TENANT 레벨 없음)
        mockMvc.perform(patch("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new UpdateSettingRequest("KEY_A", "tenant-a", "TENANT", tenantId))))
            .andExpect(status().isOk());

        // ORG: KEY_B=org-b (KEY_A, KEY_C는 ORG 레벨 없음)
        mockMvc.perform(patch("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new UpdateSettingRequest("KEY_B", "org-b", "ORG", orgId))))
            .andExpect(status().isOk());

        // 4. 병합 조회 → 각 키의 우선순위 독립적 적용
        // KEY_A: ORG 없음 → TENANT (tenant-a)
        // KEY_B: TENANT 없음 → ORG (org-b)
        // KEY_C: ORG, TENANT 없음 → DEFAULT (default-c)
        mockMvc.perform(get("/api/v1/settings")
                .param("orgId", orgId.toString())
                .param("tenantId", tenantId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.settings.KEY_A").value("tenant-a"))
            .andExpect(jsonPath("$.data.settings.KEY_B").value("org-b"))
            .andExpect(jsonPath("$.data.settings.KEY_C").value("default-c"));
    }

    @Test
    @DisplayName("비밀 설정(is_secret=1)은 마스킹되어 반환된다")
    void settingsPriorityMerge_SecretSettings_ReturnsMasked() throws Exception {
        // 1. DEFAULT 레벨 비밀 설정 생성 (API_KEY)
        UpdateSettingRequest secretSettingRequest = new UpdateSettingRequest(
            "API_KEY", "secret-api-key-12345", "DEFAULT", null
        );
        mockMvc.perform(patch("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(secretSettingRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.secret").value(true));

        // 2. 설정 조회 → 비밀 값은 마스킹되어 반환 (****로 표시)
        mockMvc.perform(get("/api/v1/settings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.settings.API_KEY").value("********"));
    }
}
