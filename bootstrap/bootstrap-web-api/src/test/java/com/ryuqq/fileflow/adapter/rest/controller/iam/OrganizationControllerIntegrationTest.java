package com.ryuqq.fileflow.adapter.rest.controller.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.dto.iam.organization.CreateOrganizationRequest;
import com.ryuqq.fileflow.adapter.rest.dto.iam.organization.UpdateOrganizationRequest;
import com.ryuqq.fileflow.adapter.rest.exception.GlobalExceptionHandler;
import com.ryuqq.fileflow.application.iam.organization.dto.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.usecase.CreateOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.usecase.SoftDeleteOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.usecase.UpdateOrganizationUseCase;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private CreateOrganizationUseCase createOrganizationUseCase;

    @MockBean
    private UpdateOrganizationUseCase updateOrganizationUseCase;

    @MockBean
    private SoftDeleteOrganizationUseCase softDeleteOrganizationUseCase;

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

        when(createOrganizationUseCase.execute(any())).thenReturn(mockResponse);

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

        when(createOrganizationUseCase.execute(any()))
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

        when(updateOrganizationUseCase.execute(any())).thenReturn(mockResponse);

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
     * DELETE /api/v1/organizations/{organizationId} - Organization 삭제 성공 (204 No Content)
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Test
    @DisplayName("DELETE /api/v1/organizations/{organizationId} - Organization 삭제 성공 (204 No Content)")
    void deleteOrganization_Success_Returns204() throws Exception {
        // Given
        Long organizationId = 1L;
        doNothing().when(softDeleteOrganizationUseCase).execute(any());

        // When & Then
        mockMvc.perform(delete("/api/v1/organizations/{organizationId}", organizationId))
            .andExpect(status().isNoContent());
    }
}
