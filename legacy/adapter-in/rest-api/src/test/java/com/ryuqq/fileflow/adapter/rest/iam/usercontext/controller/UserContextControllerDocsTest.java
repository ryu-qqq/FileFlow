package com.ryuqq.fileflow.adapter.rest.iam.usercontext.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.request.AssignRoleApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.request.CreateUserContextApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.request.RevokeRoleApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.usercontext.dto.response.UserContextApiResponse;
import com.ryuqq.fileflow.application.iam.usercontext.dto.response.UserContextResponse;
import com.ryuqq.fileflow.application.iam.usercontext.port.in.AssignRoleUseCase;
import com.ryuqq.fileflow.application.iam.usercontext.port.in.CreateUserContextUseCase;
import com.ryuqq.fileflow.application.iam.usercontext.port.in.RevokeRoleUseCase;
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
 * UserContextController REST API Documentation Test
 *
 * <p>Spring REST Docs를 사용하여 UserContext API 문서를 자동 생성합니다.</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>POST /api/v1/user-contexts - UserContext 생성</li>
 *   <li>POST /api/v1/user-contexts/{userId}/roles - Role 할당</li>
 *   <li>DELETE /api/v1/user-contexts/{userId}/roles - Role 철회</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@WebMvcTest(UserContextController.class)
@AutoConfigureRestDocs
@Import(IntegrationTestConfiguration.class)
@DisplayName("UserContextController API 문서 생성 테스트")
class UserContextControllerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateUserContextUseCase createUserContextUseCase;

    @MockBean
    private AssignRoleUseCase assignRoleUseCase;

    @MockBean
    private RevokeRoleUseCase revokeRoleUseCase;

    @Test
    @DisplayName("POST /api/v1/user-contexts - UserContext 생성")
    void createUserContext() throws Exception {
        // Given
        CreateUserContextApiRequest request = new CreateUserContextApiRequest(
            "auth0|abc123",
            "john.doe@example.com"
        );
        UserContextResponse response = new UserContextResponse(
            12345L,
            "auth0|abc123",
            "john.doe@example.com",
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(createUserContextUseCase.createUserContext(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            post("/api/v1/user-contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userContextId").exists())
        .andDo(document("user-context/create",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("externalUserId")
                    .type(JsonFieldType.STRING)
                    .description("외부 인증 시스템 사용자 ID (예: Auth0, OAuth2 Provider)"),
                fieldWithPath("email")
                    .type(JsonFieldType.STRING)
                    .description("사용자 이메일 (Email 형식 검증)"),
                fieldWithPath("displayName")
                    .type(JsonFieldType.STRING)
                    .description("사용자 표시 이름"),
                fieldWithPath("tenantId")
                    .type(JsonFieldType.NUMBER)
                    .description("테넌트 ID"),
                fieldWithPath("orgId")
                    .type(JsonFieldType.NUMBER)
                    .description("조직 ID")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.userContextId").type(JsonFieldType.NUMBER).description("생성된 UserContext ID"),
                fieldWithPath("data.externalUserId").type(JsonFieldType.STRING).description("외부 인증 시스템 사용자 ID"),
                fieldWithPath("data.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                fieldWithPath("data.displayName").type(JsonFieldType.STRING).description("사용자 표시 이름"),
                fieldWithPath("data.tenantId").type(JsonFieldType.NUMBER).description("테넌트 ID"),
                fieldWithPath("data.orgId").type(JsonFieldType.NUMBER).description("조직 ID"),
                fieldWithPath("data.roles").type(JsonFieldType.ARRAY).description("할당된 역할 목록 (기본: [\"ROLE_USER\"])"),
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
    @DisplayName("POST /api/v1/user-contexts/{userId}/roles - Role 할당")
    void assignRole() throws Exception {
        // Given
        Long userId = 12345L;
        AssignRoleApiRequest request = new AssignRoleApiRequest(
            1L,
            1L,
            "ADMIN"
        );

        doNothing().when(assignRoleUseCase).execute(any());

        // When & Then
        mockMvc.perform(
            post("/api/v1/user-contexts/{userId}/roles", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isNoContent())
        .andDo(document("user-context/assign-role",
            preprocessRequest(prettyPrint()),
            pathParameters(
                parameterWithName("userId")
                    .description("Role을 할당할 사용자 ID")
            ),
            requestFields(
                fieldWithPath("roleId")
                    .type(JsonFieldType.NUMBER)
                    .description("할당할 Role ID"),
                fieldWithPath("orgId")
                    .type(JsonFieldType.NUMBER)
                    .description("조직 ID (컨텍스트 검증용)"),
                fieldWithPath("membershipType")
                    .type(JsonFieldType.STRING)
                    .description("멤버십 타입 (예: ADMIN, MEMBER, GUEST)")
            )
        ));
    }

    @Test
    @DisplayName("DELETE /api/v1/user-contexts/{userId}/roles - Role 철회")
    void revokeRole() throws Exception {
        // Given
        Long userId = 12345L;
        RevokeRoleApiRequest request = new RevokeRoleApiRequest(
            1L,
            1L
        );

        doNothing().when(revokeRoleUseCase).execute(any());

        // When & Then
        mockMvc.perform(
            delete("/api/v1/user-contexts/{userId}/roles", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isNoContent())
        .andDo(document("user-context/revoke-role",
            preprocessRequest(prettyPrint()),
            pathParameters(
                parameterWithName("userId")
                    .description("Role을 철회할 사용자 ID")
            ),
            requestFields(
                fieldWithPath("roleId")
                    .type(JsonFieldType.NUMBER)
                    .description("철회할 Role ID"),
                fieldWithPath("orgId")
                    .type(JsonFieldType.NUMBER)
                    .description("조직 ID (컨텍스트 검증용)")
            )
        ));
    }
}
