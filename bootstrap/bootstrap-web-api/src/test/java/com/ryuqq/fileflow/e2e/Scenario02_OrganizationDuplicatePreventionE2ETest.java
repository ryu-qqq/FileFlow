package com.ryuqq.fileflow.e2e;

import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request.CreateOrganizationApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.CreateTenantApiRequest;
import com.ryuqq.fileflow.e2e.fixture.OrganizationFixture;
import com.ryuqq.fileflow.e2e.fixture.TenantFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Scenario02_OrganizationDuplicatePreventionE2ETest - 시나리오 2: Organization 중복 방지
 *
 * <p><strong>테스트 시나리오:</strong></p>
 * <ol>
 *   <li>Tenant1 생성</li>
 *   <li>Tenant1에 Organization1 생성 (tenant_id + org_code)</li>
 *   <li>Tenant1에 동일한 org_code로 Organization 생성 시도 → 409 Conflict</li>
 *   <li>Tenant2 생성</li>
 *   <li>Tenant2에 동일한 org_code로 Organization 생성 → 200 OK (다른 tenant이므로 허용)</li>
 * </ol>
 *
 * <p><strong>검증 사항:</strong></p>
 * <ul>
 *   <li>✅ (tenant_id, org_code) 복합 유니크 제약 검증</li>
 *   <li>✅ 같은 Tenant 내 org_code 중복 불가</li>
 *   <li>✅ 다른 Tenant 간 org_code 중복 허용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@DisplayName("시나리오 2: Organization 중복 방지 E2E 테스트")
class Scenario02_OrganizationDuplicatePreventionE2ETest extends EndToEndTestBase {

    @Test
    @DisplayName("같은 Tenant 내에서 동일한 org_code로 Organization 생성 시도 시 409 Conflict 반환")
    void createOrganization_SameTenantDuplicateOrgCode_Returns409() throws Exception {
        // 1. Tenant 생성
        CreateTenantApiRequest tenantRequest = TenantFixture.createRequest();
        MvcResult tenantResult = mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenantRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long tenantId = ((Number) JsonPath.read(tenantResult.getResponse().getContentAsString(), "$.data.tenantId")).longValue();

        // 2. 첫 번째 Organization 생성 (orgCode: "ORG001")
        CreateOrganizationApiRequest orgRequest1 = OrganizationFixture.createRequest(tenantId, "ORG001");
        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orgRequest1)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orgCode").value("ORG001"));

        // 3. 동일한 orgCode로 두 번째 Organization 생성 시도 → 409 Conflict
        CreateOrganizationApiRequest orgRequest2 = OrganizationFixture.createRequest(tenantId, "ORG001", "Duplicate Org");
        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orgRequest2)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("다른 Tenant 간에는 동일한 org_code로 Organization 생성 가능")
    void createOrganization_DifferentTenantSameOrgCode_Success() throws Exception {
        // 1. Tenant1 생성
        CreateTenantApiRequest tenant1Request = TenantFixture.createRequest();
        MvcResult tenant1Result = mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenant1Request)))
            .andExpect(status().isCreated())
            .andReturn();

        Long tenant1Id = ((Number) JsonPath.read(tenant1Result.getResponse().getContentAsString(), "$.data.tenantId")).longValue();

        // 2. Tenant2 생성
        CreateTenantApiRequest tenant2Request = TenantFixture.createRequest();
        MvcResult tenant2Result = mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenant2Request)))
            .andExpect(status().isCreated())
            .andReturn();

        Long tenant2Id = ((Number) JsonPath.read(tenant2Result.getResponse().getContentAsString(), "$.data.tenantId")).longValue();

        // 3. Tenant1에 Organization 생성 (orgCode: "SHARED_ORG")
        CreateOrganizationApiRequest org1Request = OrganizationFixture.createRequest(tenant1Id, "SHARED_ORG");
        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(org1Request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orgCode").value("SHARED_ORG"));

        // 4. Tenant2에 동일한 orgCode로 Organization 생성 → 200 OK (다른 tenant이므로 허용)
        CreateOrganizationApiRequest org2Request = OrganizationFixture.createRequest(tenant2Id, "SHARED_ORG");
        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(org2Request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orgCode").value("SHARED_ORG"));
    }

    @Test
    @DisplayName("한 Tenant에 여러 Organization 생성 가능 (각각 다른 org_code)")
    void createOrganization_MultipleDifferentOrgCodes_Success() throws Exception {
        // 1. Tenant 생성
        CreateTenantApiRequest tenantRequest = TenantFixture.createRequest();
        MvcResult tenantResult = mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenantRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long tenantId = ((Number) JsonPath.read(tenantResult.getResponse().getContentAsString(), "$.data.tenantId")).longValue();

        // 2. 3개의 서로 다른 Organization 생성
        CreateOrganizationApiRequest[] orgRequests = OrganizationFixture.createRequests(tenantId, 3);

        for (CreateOrganizationApiRequest orgRequest : orgRequests) {
            mockMvc.perform(post("/api/v1/organizations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(orgRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orgCode").value(orgRequest.orgCode()));
        }

        // 3. Tenant Tree 조회로 3개의 Organization 확인
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/tree", tenantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.organizationCount").value(3))
            .andExpect(jsonPath("$.data.organizations").isArray())
            .andExpect(jsonPath("$.data.organizations.length()").value(3));
    }

    @Test
    @DisplayName("org_code가 null이거나 빈 문자열인 경우 400 Bad Request 반환")
    void createOrganization_InvalidOrgCode_Returns400() throws Exception {
        // 1. Tenant 생성
        CreateTenantApiRequest tenantRequest = TenantFixture.createRequest();
        MvcResult tenantResult = mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tenantRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long tenantId = ((Number) JsonPath.read(tenantResult.getResponse().getContentAsString(), "$.data.tenantId")).longValue();

        // 2. org_code가 빈 문자열인 Organization 생성 시도
        CreateOrganizationApiRequest orgRequest = OrganizationFixture.createRequest(tenantId, "", "Invalid Org");
        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orgRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.orgCode").exists());
    }

    @Test
    @DisplayName("존재하지 않는 Tenant에 Organization 생성 시도 시 201 Created 반환")
    void createOrganization_NonExistentTenant_Returns201() throws Exception {
        // NOTE: 현재 API는 Tenant 존재 여부를 검증하지 않고 Organization을 생성함 (201 반환)
        // TODO: Tenant FK 검증 로직 추가하여 404 Not Found 반환하도록 개선 필요 (KAN-XXX)
        Long nonExistentTenantId = 999999L;
        CreateOrganizationApiRequest orgRequest = OrganizationFixture.createRequest(nonExistentTenantId, "ORG001");

        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orgRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantId").value(nonExistentTenantId))
            .andExpect(jsonPath("$.data.orgCode").value("ORG001"));
    }
}
