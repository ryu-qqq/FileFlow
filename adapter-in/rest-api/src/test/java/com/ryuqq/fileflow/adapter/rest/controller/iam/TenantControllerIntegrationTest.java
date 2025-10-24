package com.ryuqq.fileflow.adapter.rest.controller.iam;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ryuqq.fileflow.adapter.rest.exception.GlobalExceptionHandler;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.controller.TenantController;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.CreateTenantRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.UpdateTenantRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.UpdateTenantStatusRequest;
import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantTreeResponse;
import com.ryuqq.fileflow.application.iam.tenant.facade.TenantCommandFacade;
import com.ryuqq.fileflow.application.iam.tenant.facade.TenantQueryFacade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

/**
 * TenantControllerIntegrationTest - Tenant Controller Integration Test
 *
 * <p>TenantController의 REST API 엔드포인트를 MockMvc를 사용하여 통합 테스트합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ MockMvc를 사용한 HTTP 요청/응답 테스트</li>
 *   <li>✅ {@code @WebMvcTest} - Controller Layer만 테스트</li>
 *   <li>✅ UseCase는 {@code @MockBean}으로 Mocking</li>
 *   <li>✅ Validation 검증 포함</li>
 *   <li>✅ HTTP 상태 코드 검증</li>
 *   <li>✅ RFC 7807 응답 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Tag("integration")
@Tag("controller")
@Tag("slow")
@WebMvcTest(controllers = TenantController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("TenantController Integration Test")
@ContextConfiguration(classes = {TenantController.class, GlobalExceptionHandler.class})
class TenantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantCommandFacade tenantCommandFacade;

    @MockBean
    private TenantQueryFacade tenantQueryFacade;

    /**
     * POST /api/v1/tenants - Tenant 생성 성공 (201 Created)
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Test
    @DisplayName("POST /api/v1/tenants - Tenant 생성 성공 (201 Created)")
    void createTenant_Success_Returns201() throws Exception {
        // Given
        CreateTenantRequest request = new CreateTenantRequest("my-tenant");
        TenantResponse mockResponse = new TenantResponse(
            "tenant-id-123",
            "my-tenant",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(tenantCommandFacade.createTenant(any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.tenantId").value("tenant-id-123"))
            .andExpect(jsonPath("$.data.name").value("my-tenant"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andExpect(jsonPath("$.data.deleted").value(false));
    }

    /**
     * POST /api/v1/tenants - Validation 실패 (400 Bad Request)
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Test
    @DisplayName("POST /api/v1/tenants - Validation 실패 (400 Bad Request)")
    void createTenant_ValidationFails_Returns400() throws Exception {
        // Given - name이 빈 문자열
        CreateTenantRequest request = new CreateTenantRequest("");

        // When & Then - RFC 7807 응답 검증
        mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").exists())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.errors.name").exists());
    }

    /**
     * POST /api/v1/tenants - 중복 Tenant 이름 (409 Conflict)
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Test
    @DisplayName("POST /api/v1/tenants - 중복 Tenant 이름 (409 Conflict)")
    void createTenant_DuplicateName_Returns409() throws Exception {
        // Given
        CreateTenantRequest request = new CreateTenantRequest("my-tenant");

        when(tenantCommandFacade.createTenant(any()))
            .thenThrow(new IllegalStateException("동일한 이름의 Tenant가 이미 존재합니다: my-tenant"));

        // When & Then - RFC 7807 응답 검증
        mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("동일한 이름의 Tenant가 이미 존재합니다: my-tenant"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * PATCH /api/v1/tenants/{tenantId} - Tenant 수정 성공 (200 OK)
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Test
    @DisplayName("PATCH /api/v1/tenants/{tenantId} - Tenant 수정 성공 (200 OK)")
    void updateTenant_Success_Returns200() throws Exception {
        // Given
        String tenantId = "tenant-id-123";
        UpdateTenantRequest request = new UpdateTenantRequest("updated-tenant-name");
        TenantResponse mockResponse = new TenantResponse(
            tenantId,
            "updated-tenant-name",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(tenantCommandFacade.updateTenant(any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(patch("/api/v1/tenants/{tenantId}", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tenantId").value(tenantId))
            .andExpect(jsonPath("$.data.name").value("updated-tenant-name"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    /**
     * PATCH /api/v1/tenants/{tenantId} - Validation 실패 (400 Bad Request)
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Test
    @DisplayName("PATCH /api/v1/tenants/{tenantId} - Validation 실패 (400 Bad Request)")
    void updateTenant_ValidationFails_Returns400() throws Exception {
        // Given - name이 빈 문자열
        String tenantId = "tenant-id-123";
        UpdateTenantRequest request = new UpdateTenantRequest("");

        // When & Then - RFC 7807 응답 검증
        mockMvc.perform(patch("/api/v1/tenants/{tenantId}", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.name").exists());
    }

    /**
     * PATCH /api/v1/tenants/{tenantId}/status - Tenant 상태 변경 성공 (200 OK)
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Test
    @DisplayName("PATCH /api/v1/tenants/{tenantId}/status - Tenant 상태 변경 성공 (200 OK)")
    void updateTenantStatus_Success_Returns200() throws Exception {
        // Given
        String tenantId = "tenant-id-123";
        UpdateTenantStatusRequest request = new UpdateTenantStatusRequest("SUSPENDED");
        TenantResponse mockResponse = new TenantResponse(
            tenantId,
            "my-tenant",
            "SUSPENDED",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(tenantCommandFacade.updateTenantStatus(any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(patch("/api/v1/tenants/{tenantId}/status", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tenantId").value(tenantId))
            .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    /**
     * GET /api/v1/tenants - Tenant 목록 조회 (Offset-based Pagination, 200 OK)
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Test
    @DisplayName("GET /api/v1/tenants - Tenant 목록 조회 Offset-based (200 OK)")
    void getTenants_OffsetBased_Returns200() throws Exception {
        // Given
        List<TenantResponse> tenants = new ArrayList<>();
        tenants.add(new TenantResponse(
            "tenant-1",
            "tenant-one",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        ));
        tenants.add(new TenantResponse(
            "tenant-2",
            "tenant-two",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        ));

        PageResponse<TenantResponse> pageResponse = new PageResponse<>(
            tenants,
            0,
            20,
            2L,
            1,
            true,
            true
        );

        when(tenantQueryFacade.getTenantsWithPage(any())).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(20))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    /**
     * GET /api/v1/tenants - Tenant 목록 조회 (Cursor-based Pagination, 200 OK)
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Test
    @DisplayName("GET /api/v1/tenants - Tenant 목록 조회 Cursor-based (200 OK)")
    void getTenants_CursorBased_Returns200() throws Exception {
        // Given
        List<TenantResponse> tenants = new ArrayList<>();
        tenants.add(new TenantResponse(
            "tenant-1",
            "tenant-one",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        ));

        SliceResponse<TenantResponse> sliceResponse = new SliceResponse<>(
            tenants,
            20,
            true,
            "next-cursor-encoded"
        );

        when(tenantQueryFacade.getTenantsWithSlice(any())).thenReturn(sliceResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants")
                .param("cursor", "current-cursor")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.size").value(20))
            .andExpect(jsonPath("$.data.hasNext").value(true))
            .andExpect(jsonPath("$.data.nextCursor").value("next-cursor-encoded"));
    }

    /**
     * GET /api/v1/tenants/{tenantId} - Tenant 단건 조회 (200 OK)
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Test
    @DisplayName("GET /api/v1/tenants/{tenantId} - Tenant 단건 조회 (200 OK)")
    void getTenant_Success_Returns200() throws Exception {
        // Given
        String tenantId = "tenant-id-123";
        TenantResponse mockResponse = new TenantResponse(
            tenantId,
            "my-tenant",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(tenantQueryFacade.getTenant(any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}", tenantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tenantId").value(tenantId))
            .andExpect(jsonPath("$.data.name").value("my-tenant"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    /**
     * GET /api/v1/tenants/{tenantId}/tree - Tenant 트리 조회 (200 OK)
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Test
    @DisplayName("GET /api/v1/tenants/{tenantId}/tree - Tenant 트리 조회 (200 OK)")
    void getTenantTree_Success_Returns200() throws Exception {
        // Given
        String tenantId = "tenant-id-123";
        TenantTreeResponse mockResponse = new TenantTreeResponse(
            tenantId,
            "my-tenant",
            "ACTIVE",
            false,
            0,
            new ArrayList<>(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(tenantQueryFacade.getTenantTree(any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/tree", tenantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tenantId").value(tenantId))
            .andExpect(jsonPath("$.data.name").value("my-tenant"))
            .andExpect(jsonPath("$.data.organizationCount").value(0))
            .andExpect(jsonPath("$.data.organizations").isArray());
    }
}
