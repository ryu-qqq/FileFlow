package com.ryuqq.fileflow.adapter.rest.iam.tenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.CreateTenantApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.UpdateTenantApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.UpdateTenantStatusApiRequest;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantTreeResponse;
import com.ryuqq.fileflow.application.iam.tenant.facade.TenantCommandFacade;
import com.ryuqq.fileflow.application.iam.tenant.facade.TenantQueryFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import com.ryuqq.fileflow.adapter.rest.integration.IntegrationTestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TenantController REST API Documentation Test
 *
 * <p>Spring REST Docs를 사용하여 Tenant API 문서를 자동 생성합니다.</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>POST /api/v1/tenants - Tenant 생성</li>
 *   <li>PATCH /api/v1/tenants/{tenantId} - Tenant 수정</li>
 *   <li>PATCH /api/v1/tenants/{tenantId}/status - Tenant 상태 변경</li>
 *   <li>GET /api/v1/tenants/{tenantId} - Tenant 상세 조회</li>
 *   <li>GET /api/v1/tenants/{tenantId}/tree - Tenant 트리 조회</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@WebMvcTest(TenantController.class)
@AutoConfigureRestDocs
@Import(IntegrationTestConfiguration.class)
@DisplayName("TenantController API 문서 생성 테스트")
class TenantControllerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantCommandFacade tenantCommandFacade;

    @MockBean
    private TenantQueryFacade tenantQueryFacade;

    @Test
    @DisplayName("POST /api/v1/tenants - Tenant 생성")
    void createTenant() throws Exception {
        // Given
        CreateTenantApiRequest request = new CreateTenantApiRequest("my-tenant");
        TenantResponse response = new TenantResponse(
            1L,
            "my-tenant",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(tenantCommandFacade.createTenant(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.tenantId").exists())
        .andDo(document("tenant/create",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("name")
                    .type(JsonFieldType.STRING)
                    .description("테넌트 이름 (고유값)")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.tenantId").type(JsonFieldType.NUMBER).description("생성된 테넌트 ID"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("테넌트 이름"),
                fieldWithPath("data.status").type(JsonFieldType.STRING).description("테넌트 상태 (ACTIVE/SUSPENDED)"),
                fieldWithPath("data.deleted").type(JsonFieldType.BOOLEAN).description("삭제 여부"),
                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 시간 (ISO 8601)"),
                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 시간 (ISO 8601)"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }

    @Test
    @DisplayName("PATCH /api/v1/tenants/{tenantId} - Tenant 수정")
    void updateTenant() throws Exception {
        // Given
        Long tenantId = 1L;
        UpdateTenantApiRequest request = new UpdateTenantApiRequest("updated-tenant-name");
        TenantResponse response = new TenantResponse(
            tenantId,
            "updated-tenant-name",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(tenantCommandFacade.updateTenant(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            patch("/api/v1/tenants/{tenantId}", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.tenantId").value(tenantId))
        .andDo(document("tenant/update",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("tenantId").description("수정할 테넌트 ID")
            ),
            requestFields(
                fieldWithPath("name")
                    .type(JsonFieldType.STRING)
                    .description("새로운 테넌트 이름")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.tenantId").type(JsonFieldType.NUMBER).description("테넌트 ID"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("수정된 테넌트 이름"),
                fieldWithPath("data.status").type(JsonFieldType.STRING).description("테넌트 상태"),
                fieldWithPath("data.deleted").type(JsonFieldType.BOOLEAN).description("삭제 여부"),
                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 시간"),
                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 시간"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }

    @Test
    @DisplayName("PATCH /api/v1/tenants/{tenantId}/status - Tenant 상태 변경")
    void updateTenantStatus() throws Exception {
        // Given
        Long tenantId = 1L;
        UpdateTenantStatusApiRequest request = new UpdateTenantStatusApiRequest("SUSPENDED");
        TenantResponse response = new TenantResponse(
            tenantId,
            "my-tenant",
            "SUSPENDED",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(tenantCommandFacade.updateTenantStatus(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            patch("/api/v1/tenants/{tenantId}/status", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andDo(document("tenant/update-status",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("tenantId").description("상태를 변경할 테넌트 ID")
            ),
            requestFields(
                fieldWithPath("status")
                    .type(JsonFieldType.STRING)
                    .description("새로운 상태 (ACTIVE/SUSPENDED)")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.tenantId").type(JsonFieldType.NUMBER).description("테넌트 ID"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("테넌트 이름"),
                fieldWithPath("data.status").type(JsonFieldType.STRING).description("변경된 상태"),
                fieldWithPath("data.deleted").type(JsonFieldType.BOOLEAN).description("삭제 여부"),
                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 시간"),
                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 시간"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }

    @Test
    @DisplayName("GET /api/v1/tenants/{tenantId} - Tenant 상세 조회")
    void getTenant() throws Exception {
        // Given
        Long tenantId = 1L;
        TenantResponse response = new TenantResponse(
            tenantId,
            "my-tenant",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(tenantQueryFacade.getTenant(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            get("/api/v1/tenants/{tenantId}", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.tenantId").value(tenantId))
        .andDo(document("tenant/get-detail",
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("tenantId").description("조회할 테넌트 ID")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.tenantId").type(JsonFieldType.NUMBER).description("테넌트 ID"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("테넌트 이름"),
                fieldWithPath("data.status").type(JsonFieldType.STRING).description("테넌트 상태"),
                fieldWithPath("data.deleted").type(JsonFieldType.BOOLEAN).description("삭제 여부"),
                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 시간 (ISO 8601)"),
                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 시간 (ISO 8601)"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }

    @Test
    @DisplayName("GET /api/v1/tenants/{tenantId}/tree - Tenant 트리 조회")
    void getTenantTree() throws Exception {
        // Given
        Long tenantId = 1L;
        TenantTreeResponse response = new TenantTreeResponse(
            tenantId,
            "my-tenant",
            "ACTIVE",
            false,
            0,
            List.of(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(tenantQueryFacade.getTenantTree(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            get("/api/v1/tenants/{tenantId}/tree", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.tenantId").value(tenantId))
        .andDo(document("tenant/get-tree",
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("tenantId").description("조회할 테넌트 ID")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.tenantId").type(JsonFieldType.NUMBER).description("테넌트 ID"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("테넌트 이름"),
                fieldWithPath("data.status").type(JsonFieldType.STRING).description("테넌트 상태"),
                fieldWithPath("data.deleted").type(JsonFieldType.BOOLEAN).description("삭제 여부"),
                fieldWithPath("data.organizationCount").type(JsonFieldType.NUMBER).description("소속 조직 개수"),
                fieldWithPath("data.organizations").type(JsonFieldType.ARRAY).description("소속 조직 목록"),
                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 시간 (ISO 8601)"),
                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 시간 (ISO 8601)"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }
}
