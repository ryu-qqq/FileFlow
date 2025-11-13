package com.ryuqq.fileflow.adapter.rest.settings.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.settings.dto.request.CreateSettingApiRequest;
import com.ryuqq.fileflow.adapter.rest.settings.dto.request.UpdateSettingApiRequest;
import com.ryuqq.fileflow.application.settings.dto.SettingResponse;
import com.ryuqq.fileflow.application.settings.port.in.CreateSettingUseCase;
import com.ryuqq.fileflow.application.settings.port.in.UpdateSettingUseCase;
import com.ryuqq.fileflow.application.settings.port.in.GetMergedSettingsUseCase;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SettingsController REST API Documentation Test
 *
 * <p>Spring REST Docs를 사용하여 Settings API 문서를 자동 생성합니다.</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>POST /api/v1/settings - 설정 생성</li>
 *   <li>GET /api/v1/settings - 3레벨 병합된 설정 조회</li>
 *   <li>PATCH /api/v1/settings - 특정 설정 수정</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@WebMvcTest(SettingsController.class)
@AutoConfigureRestDocs
@Import(IntegrationTestConfiguration.class)
@DisplayName("SettingsController API 문서 생성 테스트")
class SettingsControllerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateSettingUseCase createSettingUseCase;

    @MockBean
    private GetMergedSettingsUseCase getMergedSettingsUseCase;

    @MockBean
    private UpdateSettingUseCase updateSettingUseCase;

    @Test
    @DisplayName("POST /api/v1/settings - 설정 생성")
    void createSetting() throws Exception {
        // Given
        CreateSettingApiRequest request = new CreateSettingApiRequest(
            "max_file_size",
            "104857600",
            "TENANT",
            1L,
            "STRING",
            false
        );
        CreateSettingUseCase.Response response = new CreateSettingUseCase.Response(
            1L,
            "max_file_size",
            "104857600",
            "STRING",
            "TENANT",
            1L,
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(createSettingUseCase.execute(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            post("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").exists())
        .andDo(document("settings/create",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("key")
                    .type(JsonFieldType.STRING)
                    .description("설정 키 (예: max_file_size, allowed_extensions)"),
                fieldWithPath("value")
                    .type(JsonFieldType.STRING)
                    .description("설정 값 (String 형태, 숫자/Boolean도 문자열로 저장)"),
                fieldWithPath("level")
                    .type(JsonFieldType.STRING)
                    .description("설정 레벨 (ORG, TENANT, DEFAULT)"),
                fieldWithPath("contextId")
                    .type(JsonFieldType.NUMBER)
                    .description("컨텍스트 ID (ORG/TENANT 레벨 필수)").optional(),
                fieldWithPath("valueType")
                    .type(JsonFieldType.STRING)
                    .description("값 타입 (STRING, NUMBER, BOOLEAN, JSON)").optional(),
                fieldWithPath("secret")
                    .type(JsonFieldType.BOOLEAN)
                    .description("비밀 설정 여부").optional()
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("생성된 설정 ID"),
                fieldWithPath("data.key").type(JsonFieldType.STRING).description("설정 키"),
                fieldWithPath("data.value").type(JsonFieldType.STRING).description("설정 값"),
                fieldWithPath("data.type").type(JsonFieldType.STRING).description("설정 타입"),
                fieldWithPath("data.level").type(JsonFieldType.STRING).description("설정 레벨"),
                fieldWithPath("data.contextId").type(JsonFieldType.NUMBER).description("컨텍스트 ID"),
                fieldWithPath("data.encrypted").type(JsonFieldType.BOOLEAN).description("암호화 여부"),
                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 시간"),
                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 시간"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }

    @Test
    @DisplayName("GET /api/v1/settings - 3레벨 병합된 설정 조회")
    void getMergedSettings() throws Exception {
        // Given
        Long tenantId = 1L;
        Long orgId = 1L;
        GetMergedSettingsUseCase.Response response = new GetMergedSettingsUseCase.Response(
            Map.of(
                "upload.max_file_size", "104857600",
                "upload.allowed_extensions", ".pdf,.jpg,.png",
                "download.timeout_seconds", "300",
                "security.enable_virus_scan", "true"
            )
        );

        given(getMergedSettingsUseCase.execute(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            get("/api/v1/settings")
                .param("tenantId", tenantId.toString())
                .param("orgId", orgId.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isMap())
        .andDo(document("settings/get-merged",
            preprocessResponse(prettyPrint()),
            queryParameters(
                parameterWithName("tenantId")
                    .description("테넌트 ID (선택, 없으면 ORG 레벨만)"),
                parameterWithName("orgId")
                    .description("조직 ID (선택, 없으면 DEFAULT 레벨만)")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data").type(JsonFieldType.OBJECT).description("3레벨 병합된 설정 맵 (DEFAULT → ORG → TENANT 우선순위)"),
                fieldWithPath("data.*").type(JsonFieldType.STRING).description("설정 키-값 쌍 (예: upload.max_file_size=104857600)"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }

    @Test
    @DisplayName("PATCH /api/v1/settings - 특정 설정 수정")
    void updateSetting() throws Exception {
        // Given
        Long settingId = 1L;
        UpdateSettingApiRequest request = new UpdateSettingApiRequest(
            "max_file_size",
            "209715200",
            "TENANT",
            1L
        );
        UpdateSettingUseCase.Response response = new UpdateSettingUseCase.Response(
            settingId,
            "max_file_size",
            "209715200",
            "STRING",
            "TENANT",
            1L,
            false,
            LocalDateTime.now().minusDays(7),
            LocalDateTime.now()
        );

        given(updateSettingUseCase.execute(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            patch("/api/v1/settings")
                .param("settingId", settingId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(settingId))
        .andDo(document("settings/update",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            queryParameters(
                parameterWithName("settingId")
                    .description("수정할 설정 ID")
            ),
            requestFields(
                fieldWithPath("value")
                    .type(JsonFieldType.STRING)
                    .description("새로운 설정 값")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("설정 ID"),
                fieldWithPath("data.key").type(JsonFieldType.STRING).description("설정 키"),
                fieldWithPath("data.value").type(JsonFieldType.STRING).description("수정된 설정 값"),
                fieldWithPath("data.type").type(JsonFieldType.STRING).description("설정 타입"),
                fieldWithPath("data.level").type(JsonFieldType.STRING).description("설정 레벨"),
                fieldWithPath("data.contextId").type(JsonFieldType.NUMBER).description("컨텍스트 ID"),
                fieldWithPath("data.encrypted").type(JsonFieldType.BOOLEAN).description("암호화 여부"),
                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 시간"),
                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 시간"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }
}
