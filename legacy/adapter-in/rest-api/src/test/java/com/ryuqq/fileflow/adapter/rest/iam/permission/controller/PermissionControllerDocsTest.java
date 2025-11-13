package com.ryuqq.fileflow.adapter.rest.iam.permission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.application.iam.permission.port.in.EvaluatePermissionUseCase;
import com.ryuqq.fileflow.application.iam.permission.dto.response.EvaluatePermissionResponse;
import java.util.List;
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
 * PermissionController REST API Documentation Test
 *
 * <p>Spring REST Docs를 사용하여 Permission API 문서를 자동 생성합니다.</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>GET /api/v1/permissions/evaluate - Permission 평가 (ABAC 엔진)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@WebMvcTest(PermissionController.class)
@AutoConfigureRestDocs
@Import(IntegrationTestConfiguration.class)
@DisplayName("PermissionController API 문서 생성 테스트")
class PermissionControllerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EvaluatePermissionUseCase evaluatePermissionUseCase;

    @Test
    @DisplayName("GET /api/v1/permissions/evaluate - Permission 평가 (ABAC 엔진)")
    void evaluatePermission() throws Exception {
        // Given
        Long userContextId = 12345L;
        String resource = "file";
        String action = "upload";
        EvaluatePermissionResponse response = EvaluatePermissionResponse.ofAllowed();

        given(evaluatePermissionUseCase.execute(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            get("/api/v1/permissions/evaluate")
                .param("userContextId", userContextId.toString())
                .param("resource", resource)
                .param("action", action)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.allowed").value(true))
        .andDo(document("permission/evaluate",
            preprocessResponse(prettyPrint()),
            queryParameters(
                parameterWithName("userContextId")
                    .description("사용자 컨텍스트 ID (User-Organization-Tenant 조합)"),
                parameterWithName("resource")
                    .description("리소스 타입 (예: file, upload, download, tenant, organization)"),
                parameterWithName("action")
                    .description("액션 타입 (예: read, write, delete, upload, download)")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.allowed").type(JsonFieldType.BOOLEAN).description("권한 허용 여부 (ABAC 평가 결과)"),
                fieldWithPath("data.grantedPermissions").type(JsonFieldType.ARRAY).description("부여된 권한 목록 (예: [\"file:upload\", \"file:read\"])"),
                fieldWithPath("data.reason").type(JsonFieldType.STRING).description("권한 허용/거부 이유 (디버깅/감사용)"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }
}
