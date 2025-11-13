package com.ryuqq.fileflow.adapter.rest.iam.organization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request.CreateOrganizationApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request.UpdateOrganizationApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request.UpdateOrganizationStatusApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.fixture.*;
import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.facade.OrganizationCommandFacade;
import com.ryuqq.fileflow.application.iam.organization.facade.OrganizationQueryFacade;
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
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrganizationController REST API Documentation Test
 *
 * <p>Spring REST Docs를 사용하여 Organization API 문서를 자동 생성합니다.</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>POST /api/v1/organizations - Organization 생성</li>
 *   <li>PATCH /api/v1/organizations/{organizationId} - Organization 수정</li>
 *   <li>PATCH /api/v1/organizations/{organizationId}/status - Organization 상태 변경</li>
 *   <li>DELETE /api/v1/organizations/{organizationId} - Organization 삭제 (Soft Delete)</li>
 *   <li>GET /api/v1/organizations/{organizationId} - Organization 상세 조회</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@WebMvcTest(OrganizationController.class)
@AutoConfigureRestDocs
@Import(IntegrationTestConfiguration.class)
@DisplayName("OrganizationController API 문서 생성 테스트")
class OrganizationControllerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationCommandFacade organizationCommandFacade;

    @MockBean
    private OrganizationQueryFacade organizationQueryFacade;

    @Test
    @DisplayName("POST /api/v1/organizations - Organization 생성")
    void createOrganization() throws Exception {
        // Given
        CreateOrganizationApiRequest request = CreateOrganizationApiRequestFixture.create();
        OrganizationResponse response = new OrganizationResponse(
            1L,
            1L,
            "ORG001",
            "Engineering Department",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(organizationCommandFacade.createOrganization(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.organizationId").exists())
        .andDo(document("organization/create",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("tenantId")
                    .type(JsonFieldType.NUMBER)
                    .description("테넌트 ID"),
                fieldWithPath("orgCode")
                    .type(JsonFieldType.STRING)
                    .description("조직 코드 (예: ORG001, 고유값)"),
                fieldWithPath("name")
                    .type(JsonFieldType.STRING)
                    .description("조직 이름")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.organizationId").type(JsonFieldType.NUMBER).description("생성된 조직 ID"),
                fieldWithPath("data.tenantId").type(JsonFieldType.NUMBER).description("테넌트 ID"),
                fieldWithPath("data.orgCode").type(JsonFieldType.STRING).description("조직 코드"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("조직 이름"),
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
    @DisplayName("PATCH /api/v1/organizations/{organizationId} - Organization 수정")
    void updateOrganization() throws Exception {
        // Given
        Long organizationId = 1L;
        UpdateOrganizationApiRequest request = new UpdateOrganizationApiRequest("Updated Department Name");
        OrganizationResponse response = new OrganizationResponse(
            organizationId,
            1L,
            "ORG001",
            "Updated Department Name",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(organizationCommandFacade.updateOrganization(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            patch("/api/v1/organizations/{organizationId}", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.organizationId").value(organizationId))
        .andDo(document("organization/update",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("organizationId").description("수정할 조직 ID")
            ),
            requestFields(
                fieldWithPath("name")
                    .type(JsonFieldType.STRING)
                    .description("새로운 조직 이름")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.organizationId").type(JsonFieldType.NUMBER).description("조직 ID"),
                fieldWithPath("data.tenantId").type(JsonFieldType.NUMBER).description("테넌트 ID"),
                fieldWithPath("data.orgCode").type(JsonFieldType.STRING).description("조직 코드"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("수정된 조직 이름"),
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
    @DisplayName("PATCH /api/v1/organizations/{organizationId}/status - Organization 상태 변경")
    void updateOrganizationStatus() throws Exception {
        // Given
        Long organizationId = 1L;
        UpdateOrganizationStatusApiRequest request = new UpdateOrganizationStatusApiRequest("INACTIVE");
        OrganizationResponse response = new OrganizationResponse(
            organizationId,
            1L,
            "ORG001",
            "Engineering Department",
            "INACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(organizationCommandFacade.updateOrganizationStatus(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            patch("/api/v1/organizations/{organizationId}/status", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andDo(document("organization/update-status",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("organizationId").description("상태를 변경할 조직 ID")
            ),
            requestFields(
                fieldWithPath("status")
                    .type(JsonFieldType.STRING)
                    .description("새로운 상태 (ACTIVE/INACTIVE, 주의: INACTIVE→ACTIVE 복원 불가)")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.organizationId").type(JsonFieldType.NUMBER).description("조직 ID"),
                fieldWithPath("data.tenantId").type(JsonFieldType.NUMBER).description("테넌트 ID"),
                fieldWithPath("data.orgCode").type(JsonFieldType.STRING).description("조직 코드"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("조직 이름"),
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
    @DisplayName("DELETE /api/v1/organizations/{organizationId} - Organization 삭제 (Soft Delete)")
    void deleteOrganization() throws Exception {
        // Given
        Long organizationId = 1L;
        doNothing().when(organizationCommandFacade).deleteOrganization(any());

        // When & Then
        mockMvc.perform(
            delete("/api/v1/organizations/{organizationId}", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(document("organization/delete",
            pathParameters(
                parameterWithName("organizationId").description("삭제할 조직 ID (Soft Delete)")
            )
        ));
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{organizationId} - Organization 상세 조회")
    void getOrganization() throws Exception {
        // Given
        Long organizationId = 1L;
        OrganizationResponse response = new OrganizationResponse(
            organizationId,
            1L,
            "ORG001",
            "Engineering Department",
            "ACTIVE",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(organizationQueryFacade.getOrganization(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            get("/api/v1/organizations/{organizationId}", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.organizationId").value(organizationId))
        .andDo(document("organization/get-detail",
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("organizationId").description("조회할 조직 ID")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.organizationId").type(JsonFieldType.NUMBER).description("조직 ID"),
                fieldWithPath("data.tenantId").type(JsonFieldType.NUMBER).description("테넌트 ID"),
                fieldWithPath("data.orgCode").type(JsonFieldType.STRING).description("조직 코드"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("조직 이름"),
                fieldWithPath("data.deleted").type(JsonFieldType.BOOLEAN).description("삭제 여부"),
                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 시간 (ISO 8601)"),
                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 시간 (ISO 8601)"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }
}
