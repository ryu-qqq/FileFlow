package com.ryuqq.fileflow.adapter.rest.controller.iam;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ryuqq.fileflow.adapter.rest.exception.GlobalExceptionHandler;
import com.ryuqq.fileflow.adapter.rest.iam.organization.controller.OrganizationController;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.CreateOrganizationRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.UpdateOrganizationRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.UpdateOrganizationStatusRequest;
import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.facade.OrganizationCommandFacade;
import com.ryuqq.fileflow.application.iam.organization.facade.OrganizationQueryFacade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

/**
 * OrganizationControllerIntegrationTest - Organization Controller Integration Test
 *
 * <p>OrganizationController의 REST API 엔드포인트를 MockMvc를 사용하여 통합 테스트합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ MockMvc를 사용한 HTTP 요청/응답 테스트</li>
 *   <li>✅ {@code @WebMvcTest} - Controller Layer만 테스트</li>
 *   <li>✅ UseCase는 {@code @MockBean}으로 Mocking</li>
 *   <li>✅ Validation 검증 포함</li>
 *   <li>✅ HTTP 상태 코드 검증</li>
 *   <li>✅ RFC 7807 응답 검증</li>
 *   <li>✅ Long FK 전략 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Tag("integration")
@Tag("controller")
@Tag("slow")
@WebMvcTest(controllers = OrganizationController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("OrganizationController Integration Test")
class OrganizationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationCommandFacade organizationCommandFacade;

    @MockBean
    private OrganizationQueryFacade organizationQueryFacade;

    /**
     * POST /api/v1/organizations - Organization 생성 성공 (201 Created)
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Test
    @DisplayName("POST /api/v1/organizations - Organization 생성 성공 (201 Created)")
    void createOrganization_Success_Returns201() throws Exception {
        // Given
        CreateOrganizationRequest request = new CreateOrganizationRequest(
            1L,
            "ORG001",
            "Engineering Department"
        );
        OrganizationResponse mockResponse = new OrganizationResponse(
            1L,
            1L,
            "ORG001",
            "Engineering Department",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(organizationCommandFacade.createOrganization(any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.organizationId").value(1))
            .andExpect(jsonPath("$.tenantId").value(1))
            .andExpect(jsonPath("$.orgCode").value("ORG001"))
            .andExpect(jsonPath("$.name").value("Engineering Department"))
            .andExpect(jsonPath("$.deleted").value(false));
    }

    /**
     * POST /api/v1/organizations - Validation 실패 (400 Bad Request)
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Test
    @DisplayName("POST /api/v1/organizations - Validation 실패 (400 Bad Request)")
    void createOrganization_ValidationFails_Returns400() throws Exception {
        // Given - orgCode가 빈 문자열
        CreateOrganizationRequest request = new CreateOrganizationRequest(
            1L,
            "",  // Invalid: 빈 문자열
            "Engineering Department"
        );

        // When & Then - RFC 7807 응답 검증
        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").exists())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.errors.orgCode").exists());
    }

    /**
     * POST /api/v1/organizations - 중복 조직 코드 (409 Conflict)
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Test
    @DisplayName("POST /api/v1/organizations - 중복 조직 코드 (409 Conflict)")
    void createOrganization_DuplicateOrgCode_Returns409() throws Exception {
        // Given
        CreateOrganizationRequest request = new CreateOrganizationRequest(
            1L,
            "ORG001",
            "Engineering Department"
        );

        when(organizationCommandFacade.createOrganization(any()))
            .thenThrow(new IllegalStateException(
                "동일한 Tenant 내에 동일한 조직 코드가 이미 존재합니다. TenantId: 1, OrgCode: ORG001"
            ));

        // When & Then - RFC 7807 응답 검증
        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * PATCH /api/v1/organizations/{organizationId} - Organization 수정 성공 (200 OK)
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Test
    @DisplayName("PATCH /api/v1/organizations/{organizationId} - Organization 수정 성공 (200 OK)")
    void updateOrganization_Success_Returns200() throws Exception {
        // Given
        Long organizationId = 1L;
        UpdateOrganizationRequest request = new UpdateOrganizationRequest("Updated Department Name");
        OrganizationResponse mockResponse = new OrganizationResponse(
            organizationId,
            1L,
            "ORG001",
            "Updated Department Name",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(organizationCommandFacade.updateOrganization(any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(patch("/api/v1/organizations/{organizationId}", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.organizationId").value(1))
            .andExpect(jsonPath("$.name").value("Updated Department Name"))
            .andExpect(jsonPath("$.deleted").value(false));
    }

    /**
     * PATCH /api/v1/organizations/{organizationId} - Validation 실패 (400 Bad Request)
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Test
    @DisplayName("PATCH /api/v1/organizations/{organizationId} - Validation 실패 (400 Bad Request)")
    void updateOrganization_ValidationFails_Returns400() throws Exception {
        // Given - name이 빈 문자열
        Long organizationId = 1L;
        UpdateOrganizationRequest request = new UpdateOrganizationRequest("");

        // When & Then - RFC 7807 응답 검증
        mockMvc.perform(patch("/api/v1/organizations/{organizationId}", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.name").exists());
    }

    /**
     * PATCH /api/v1/organizations/{organizationId}/status - Organization 상태 변경 성공 (200 OK)
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Test
    @DisplayName("PATCH /api/v1/organizations/{organizationId}/status - Organization 상태 변경 성공 (200 OK)")
    void updateOrganizationStatus_Success_Returns200() throws Exception {
        // Given
        Long organizationId = 1L;
        UpdateOrganizationStatusRequest request = new UpdateOrganizationStatusRequest("SUSPENDED");
        OrganizationResponse mockResponse = new OrganizationResponse(
            organizationId,
            1L,
            "ORG001",
            "Engineering Department",
            "SUSPENDED",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(organizationCommandFacade.updateOrganizationStatus(any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(patch("/api/v1/organizations/{organizationId}/status", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.organizationId").value(organizationId))
            .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    /**
     * GET /api/v1/organizations - Organization 목록 조회 (Offset-based Pagination, 200 OK)
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Test
    @DisplayName("GET /api/v1/organizations - Organization 목록 조회 Offset-based (200 OK)")
    void getOrganizations_OffsetBased_Returns200() throws Exception {
        // Given
        List<OrganizationResponse> organizations = new ArrayList<>();
        organizations.add(new OrganizationResponse(
            1L,
            1L,
            "ORG001",
            "Engineering Department",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        ));
        organizations.add(new OrganizationResponse(
            2L,
            1L,
            "ORG002",
            "Marketing Department",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        ));

        PageResponse<OrganizationResponse> pageResponse = new PageResponse<>(
            organizations,
            0,
            20,
            2,
            1
        );

        when(organizationQueryFacade.getOrganizationsWithPage(any())).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/organizations")
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
     * GET /api/v1/organizations - Organization 목록 조회 (Cursor-based Pagination, 200 OK)
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Test
    @DisplayName("GET /api/v1/organizations - Organization 목록 조회 Cursor-based (200 OK)")
    void getOrganizations_CursorBased_Returns200() throws Exception {
        // Given
        List<OrganizationResponse> organizations = new ArrayList<>();
        organizations.add(new OrganizationResponse(
            1L,
            1L,
            "ORG001",
            "Engineering Department",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        ));

        SliceResponse<OrganizationResponse> sliceResponse = new SliceResponse<>(
            organizations,
            20,
            true,
            "next-cursor-encoded"
        );

        when(organizationQueryFacade.getOrganizationsWithSlice(any())).thenReturn(sliceResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/organizations")
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
     * GET /api/v1/organizations/{organizationId} - Organization 단건 조회 (200 OK)
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Test
    @DisplayName("GET /api/v1/organizations/{organizationId} - Organization 단건 조회 (200 OK)")
    void getOrganization_Success_Returns200() throws Exception {
        // Given
        Long organizationId = 1L;
        OrganizationResponse mockResponse = new OrganizationResponse(
            organizationId,
            1L,
            "ORG001",
            "Engineering Department",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(organizationQueryFacade.getOrganization(any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}", organizationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.organizationId").value(organizationId))
            .andExpect(jsonPath("$.data.orgCode").value("ORG001"))
            .andExpect(jsonPath("$.data.name").value("Engineering Department"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

}
