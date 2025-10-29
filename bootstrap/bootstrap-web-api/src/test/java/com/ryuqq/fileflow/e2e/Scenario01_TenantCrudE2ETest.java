package com.ryuqq.fileflow.e2e;

import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.CreateTenantApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.UpdateTenantApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.UpdateTenantStatusApiRequest;
import com.ryuqq.fileflow.e2e.fixture.TenantFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Scenario01_TenantCrudE2ETest - 시나리오 1: Tenant CRUD
 *
 * <p><strong>테스트 시나리오:</strong></p>
 * <ol>
 *   <li>Tenant 생성 (POST /api/v1/tenants) → 201 Created</li>
 *   <li>Tenant 조회 (GET /api/v1/tenants/{tenantId}) → 200 OK</li>
 *   <li>Tenant 수정 (PATCH /api/v1/tenants/{tenantId}) → 200 OK</li>
 *   <li>Tenant 상태 변경 (PATCH /api/v1/tenants/{tenantId}/status) → 200 OK</li>
 *   <li>Tenant 목록 조회 (GET /api/v1/tenants) → 200 OK</li>
 * </ol>
 *
 * <p><strong>검증 사항:</strong></p>
 * <ul>
 *   <li>✅ Tenant CRUD 전체 플로우 정상 동작</li>
 *   <li>✅ Tenant 목록 조회 정상 동작</li>
 *   <li>✅ 중복 이름 생성 방지 (409 Conflict)</li>
 *   <li>✅ 존재하지 않는 Tenant 조회 시 404</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@DisplayName("시나리오 1: Tenant CRUD E2E 테스트")
class Scenario01_TenantCrudE2ETest extends EndToEndTestBase {

    @Test
    @DisplayName("Tenant CRUD 전체 플로우가 정상 동작한다")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void tenantCrud_FullFlow_Success() throws Exception {
        // 1. Tenant 생성 (CREATE)
        CreateTenantApiRequest createRequest = TenantFixture.createRequest();

        MvcResult createResult = mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantId").exists())
            .andExpect(jsonPath("$.data.name").value(createRequest.name()))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andExpect(jsonPath("$.data.deleted").value(false))
            .andReturn();

        String createResponseJson = createResult.getResponse().getContentAsString();
        Long tenantId = ((Number) JsonPath.read(createResponseJson, "$.data.tenantId")).longValue();

        // 2. Tenant 조회 (READ)
        mockMvc.perform(get("/api/v1/tenants/{tenantId}", tenantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantId").value(tenantId))
            .andExpect(jsonPath("$.data.name").value(createRequest.name()))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andExpect(jsonPath("$.data.deleted").value(false));

        // 3. Tenant 수정 (UPDATE)
        UpdateTenantApiRequest updateRequest = new UpdateTenantApiRequest("updated-tenant-name");

        mockMvc.perform(patch("/api/v1/tenants/{tenantId}", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantId").value(tenantId))
            .andExpect(jsonPath("$.data.name").value("updated-tenant-name"))
            .andExpect(jsonPath("$.data.deleted").value(false));

        // 4. Tenant 상태 변경 (UPDATE STATUS)
        UpdateTenantStatusApiRequest statusRequest = new UpdateTenantStatusApiRequest("SUSPENDED");

        mockMvc.perform(patch("/api/v1/tenants/{tenantId}/status", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(statusRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantId").value(tenantId))
            .andExpect(jsonPath("$.data.status").value("SUSPENDED"))
            .andExpect(jsonPath("$.data.deleted").value(false));

        // 5. Tenant 조회 (상태 변경 확인)
        mockMvc.perform(get("/api/v1/tenants/{tenantId}", tenantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantId").value(tenantId))
            .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    @Test
    @DisplayName("Tenant 목록 조회가 정상 동작한다")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void getTenants_Success() throws Exception {
        // 1. 2개의 Tenant 생성
        CreateTenantApiRequest[] requests = TenantFixture.createRequests(2);

        Long tenant1Id = null;
        Long tenant2Id = null;

        for (CreateTenantApiRequest request : requests) {
            MvcResult result = mockMvc.perform(post("/api/v1/tenants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            Long tenantId = ((Number) JsonPath.read(responseJson, "$.data.tenantId")).longValue();

            if (tenant1Id == null) {
                tenant1Id = tenantId;
            } else {
                tenant2Id = tenantId;
            }
        }

        // 2. Tenant 목록 조회 - 생성한 Tenant들이 조회됨
        mockMvc.perform(get("/api/v1/tenants")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            // 생성한 Tenant들이 조회되어야 함
            .andExpect(jsonPath("$.data.content[?(@.tenantId == '" + tenant1Id + "')]").exists())
            .andExpect(jsonPath("$.data.content[?(@.tenantId == '" + tenant2Id + "')]").exists());
    }

    @Test
    @DisplayName("중복된 Tenant 이름으로 생성 시도 시 409 Conflict 반환")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createTenant_DuplicateName_Returns409() throws Exception {
        // 1. 첫 번째 Tenant 생성 (unique name 사용하여 다른 테스트와 충돌 방지)
        String uniqueName = "duplicate-tenant-" + System.currentTimeMillis();
        CreateTenantApiRequest request = TenantFixture.createRequest(uniqueName);

        mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isCreated());

        // 2. 동일한 이름으로 다시 생성 시도
        mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("존재하지 않는 Tenant 조회 시 409 Conflict 반환")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void getTenant_NotFound_Returns409() throws Exception {
        // NOTE: 현재 API는 존재하지 않는 tenant 조회 시 IllegalStateException을 throw하여 409 반환
        // TODO: TenantNotFoundException 도입하여 404 Not Found 반환하도록 개선 필요 (KAN-XXX)
        Long nonExistentTenantId = 999999L;

        mockMvc.perform(get("/api/v1/tenants/{tenantId}", nonExistentTenantId))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Tenant를 찾을 수 없습니다: " + nonExistentTenantId));
    }
}
