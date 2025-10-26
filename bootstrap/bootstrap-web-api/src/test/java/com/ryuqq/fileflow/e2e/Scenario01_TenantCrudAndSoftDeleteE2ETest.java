package com.ryuqq.fileflow.e2e;

import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.CreateTenantRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.UpdateTenantRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.UpdateTenantStatusRequest;
import com.ryuqq.fileflow.e2e.fixture.TenantFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Scenario01_TenantCrudAndSoftDeleteE2ETest - 시나리오 1: Tenant CRUD + Soft Delete
 *
 * <p><strong>테스트 시나리오:</strong></p>
 * <ol>
 *   <li>Tenant 생성 (POST /api/v1/tenants) → 201 Created</li>
 *   <li>Tenant 조회 (GET /api/v1/tenants/{tenantId}) → 200 OK</li>
 *   <li>Tenant 수정 (PATCH /api/v1/tenants/{tenantId}) → 200 OK</li>
 *   <li>Tenant 상태 변경 (PATCH /api/v1/tenants/{tenantId}/status) → 200 OK</li>
 *   <li>Tenant Soft Delete (DELETE /api/v1/tenants/{tenantId}) → 204 No Content</li>
 *   <li>삭제된 Tenant 조회 시도 → 404 Not Found 또는 deleted=true</li>
 * </ol>
 *
 * <p><strong>검증 사항:</strong></p>
 * <ul>
 *   <li>✅ Tenant CRUD 전체 플로우 정상 동작</li>
 *   <li>✅ Soft Delete 후 deleted 플래그 확인</li>
 *   <li>✅ 삭제된 Tenant는 조회되지 않음 (또는 deleted=true로 표시)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@DisplayName("시나리오 1: Tenant CRUD + Soft Delete E2E 테스트")
class Scenario01_TenantCrudAndSoftDeleteE2ETest extends EndToEndTestBase {

    @Test
    @DisplayName("Tenant CRUD와 Soft Delete 전체 플로우가 정상 동작한다")
    void tenantCrudAndSoftDelete_FullFlow_Success() throws Exception {
        // 1. Tenant 생성 (CREATE)
        CreateTenantRequest createRequest = TenantFixture.createRequest();

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
        String tenantId = JsonPath.read(createResponseJson, "$.data.tenantId");

        // 2. Tenant 조회 (READ)
        mockMvc.perform(get("/api/v1/tenants/{tenantId}", tenantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantId").value(tenantId))
            .andExpect(jsonPath("$.data.name").value(createRequest.name()))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andExpect(jsonPath("$.data.deleted").value(false));

        // 3. Tenant 수정 (UPDATE)
        UpdateTenantRequest updateRequest = new UpdateTenantRequest("updated-tenant-name");

        mockMvc.perform(patch("/api/v1/tenants/{tenantId}", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantId").value(tenantId))
            .andExpect(jsonPath("$.data.name").value("updated-tenant-name"))
            .andExpect(jsonPath("$.data.deleted").value(false));

        // 4. Tenant 상태 변경 (UPDATE STATUS)
        UpdateTenantStatusRequest statusRequest = new UpdateTenantStatusRequest("SUSPENDED");

        mockMvc.perform(patch("/api/v1/tenants/{tenantId}/status", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(statusRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantId").value(tenantId))
            .andExpect(jsonPath("$.data.status").value("SUSPENDED"))
            .andExpect(jsonPath("$.data.deleted").value(false));

        // 5. Tenant Soft Delete (DELETE)
        mockMvc.perform(delete("/api/v1/tenants/{tenantId}", tenantId))
            .andExpect(status().isNoContent());

        // 6. 삭제된 Tenant 조회 시도
        // 구현 방식에 따라 404 또는 deleted=true 반환
        mockMvc.perform(get("/api/v1/tenants/{tenantId}", tenantId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Tenant 목록 조회 시 Soft Delete된 Tenant는 제외된다")
    void getTenants_ExcludesSoftDeletedTenants() throws Exception {
        // 1. 2개의 Tenant 생성
        CreateTenantRequest[] requests = TenantFixture.createRequests(2);

        String tenant1Id = null;
        String tenant2Id = null;

        for (CreateTenantRequest request : requests) {
            MvcResult result = mockMvc.perform(post("/api/v1/tenants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            String tenantId = JsonPath.read(responseJson, "$.data.tenantId");

            if (tenant1Id == null) {
                tenant1Id = tenantId;
            } else {
                tenant2Id = tenantId;
            }
        }

        // 2. 첫 번째 Tenant만 Soft Delete
        mockMvc.perform(delete("/api/v1/tenants/{tenantId}", tenant1Id))
            .andExpect(status().isNoContent());

        // 3. Tenant 목록 조회 - 삭제된 Tenant는 제외
        mockMvc.perform(get("/api/v1/tenants")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            // 삭제되지 않은 Tenant만 조회되어야 함
            .andExpect(jsonPath("$.data.content[?(@.tenantId == '" + tenant1Id + "')]").doesNotExist())
            .andExpect(jsonPath("$.data.content[?(@.tenantId == '" + tenant2Id + "')]").exists());
    }

    @Test
    @DisplayName("중복된 Tenant 이름으로 생성 시도 시 409 Conflict 반환")
    void createTenant_DuplicateName_Returns409() throws Exception {
        // 1. 첫 번째 Tenant 생성
        CreateTenantRequest request = TenantFixture.createRequest("duplicate-tenant");

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
    @DisplayName("존재하지 않는 Tenant 조회 시 404 Not Found 반환")
    void getTenant_NotFound_Returns404() throws Exception {
        String nonExistentTenantId = "non-existent-tenant-id";

        mockMvc.perform(get("/api/v1/tenants/{tenantId}", nonExistentTenantId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Not Found"))
            .andExpect(jsonPath("$.status").value(404));
    }
}
