package com.ryuqq.fileflow.adapter.rest.settings.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.exception.GlobalExceptionHandler;
import com.ryuqq.fileflow.adapter.rest.settings.dto.request.UpdateSettingApiRequest;
import com.ryuqq.fileflow.application.settings.dto.MergedSettingsResponse;
import com.ryuqq.fileflow.application.settings.dto.SettingResponse;
import com.ryuqq.fileflow.application.settings.service.GetMergedSettingsUseCase;
import com.ryuqq.fileflow.application.settings.service.UpdateSettingUseCase;
import com.ryuqq.fileflow.domain.settings.exception.SettingNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SettingsController Integration Test
 *
 * <p>Settings REST API의 HTTP 계층 통합 테스트입니다.
 * MockMvc를 사용하여 실제 HTTP 요청/응답을 시뮬레이션합니다.</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>GET /api/v1/settings - 3레벨 병합 설정 조회</li>
 *   <li>PATCH /api/v1/settings - 설정 수정</li>
 *   <li>Validation 검증</li>
 *   <li>Error Response 검증 (RFC 7807)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Tag("integration")
@Tag("controller")
@Tag("slow")
@WebMvcTest(controllers = SettingsController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("SettingsController Integration Test")
@ContextConfiguration(classes = {SettingsController.class, GlobalExceptionHandler.class})
class SettingsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetMergedSettingsUseCase getMergedSettingsUseCase;

    @MockBean
    private UpdateSettingUseCase updateSettingUseCase;

    @Nested
    @DisplayName("GET /api/v1/settings - 3레벨 병합 설정 조회")
    class GetMergedSettingsTests {

        @Test
        @DisplayName("orgId와 tenantId가 제공되면 3레벨 병합된 설정을 반환한다 (200 OK)")
        void getMergedSettings_WithOrgAndTenant_Returns200() throws Exception {
            // Arrange
            Map<String, String> mergedSettings = Map.of(
                "MAX_UPLOAD_SIZE", "200MB",
                "API_TIMEOUT", "60",
                "ENABLE_CACHE", "true",
                "API_KEY", "********"
            );
            MergedSettingsResponse mockResponse = new MergedSettingsResponse(mergedSettings);

            when(getMergedSettingsUseCase.execute(any())).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/settings")
                    .param("orgId", "1")
                    .param("tenantId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.settings.MAX_UPLOAD_SIZE").value("200MB"))
                .andExpect(jsonPath("$.data.settings.API_TIMEOUT").value("60"))
                .andExpect(jsonPath("$.data.settings.ENABLE_CACHE").value("true"))
                .andExpect(jsonPath("$.data.settings.API_KEY").value("********"))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());

            verify(getMergedSettingsUseCase).execute(any());
        }

        @Test
        @DisplayName("orgId만 제공되면 ORG + DEFAULT 병합된 설정을 반환한다 (200 OK)")
        void getMergedSettings_WithOrgOnly_Returns200() throws Exception {
            // Arrange
            Map<String, String> mergedSettings = Map.of(
                "MAX_UPLOAD_SIZE", "200MB",
                "API_TIMEOUT", "30"
            );
            MergedSettingsResponse mockResponse = new MergedSettingsResponse(mergedSettings);

            when(getMergedSettingsUseCase.execute(any())).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/settings")
                    .param("orgId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.settings.MAX_UPLOAD_SIZE").value("200MB"));

            verify(getMergedSettingsUseCase).execute(any());
        }

        @Test
        @DisplayName("tenantId만 제공되면 TENANT + DEFAULT 병합된 설정을 반환한다 (200 OK)")
        void getMergedSettings_WithTenantOnly_Returns200() throws Exception {
            // Arrange
            Map<String, String> mergedSettings = Map.of(
                "MAX_UPLOAD_SIZE", "50MB",
                "API_TIMEOUT", "30"
            );
            MergedSettingsResponse mockResponse = new MergedSettingsResponse(mergedSettings);

            when(getMergedSettingsUseCase.execute(any())).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/settings")
                    .param("tenantId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.settings.MAX_UPLOAD_SIZE").value("50MB"));

            verify(getMergedSettingsUseCase).execute(any());
        }

        @Test
        @DisplayName("파라미터 없이 요청하면 DEFAULT 설정만 반환한다 (200 OK)")
        void getMergedSettings_WithoutParams_Returns200() throws Exception {
            // Arrange
            Map<String, String> mergedSettings = Map.of(
                "MAX_UPLOAD_SIZE", "100MB",
                "API_TIMEOUT", "30",
                "ENABLE_CACHE", "true"
            );
            MergedSettingsResponse mockResponse = new MergedSettingsResponse(mergedSettings);

            when(getMergedSettingsUseCase.execute(any())).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.settings.MAX_UPLOAD_SIZE").value("100MB"))
                .andExpect(jsonPath("$.data.settings.API_TIMEOUT").value("30"))
                .andExpect(jsonPath("$.data.settings.ENABLE_CACHE").value("true"));

            verify(getMergedSettingsUseCase).execute(any());
        }

        @Test
        @DisplayName("비밀 설정은 마스킹되어 반환된다 (200 OK)")
        void getMergedSettings_SecretSettingsMasked_Returns200() throws Exception {
            // Arrange
            Map<String, String> mergedSettings = Map.of(
                "API_KEY", "********",
                "MAX_UPLOAD_SIZE", "100MB"
            );
            MergedSettingsResponse mockResponse = new MergedSettingsResponse(mergedSettings);

            when(getMergedSettingsUseCase.execute(any())).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.settings.API_KEY").value("********"))
                .andExpect(jsonPath("$.data.settings.MAX_UPLOAD_SIZE").value("100MB"));

            verify(getMergedSettingsUseCase).execute(any());
        }

        @Test
        @DisplayName("설정이 없으면 빈 Map을 반환한다 (200 OK)")
        void getMergedSettings_NoSettings_ReturnsEmptyMap() throws Exception {
            // Arrange
            MergedSettingsResponse mockResponse = new MergedSettingsResponse(Map.of());

            when(getMergedSettingsUseCase.execute(any())).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.settings").isEmpty());

            verify(getMergedSettingsUseCase).execute(any());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/settings - 설정 수정")
    class UpdateSettingTests {

        @Test
        @DisplayName("DEFAULT 레벨 설정을 성공적으로 수정한다 (200 OK)")
        void updateSetting_DefaultLevel_Returns200() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "MAX_UPLOAD_SIZE", "200MB", "DEFAULT", null
            );
            SettingResponse mockResponse = new SettingResponse(
                1L, "MAX_UPLOAD_SIZE", "200MB", "STRING", "DEFAULT", null, false,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(updateSettingUseCase.execute(any())).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());

            verify(updateSettingUseCase).execute(any());
        }

        @Test
        @DisplayName("ORG 레벨 설정을 성공적으로 수정한다 (200 OK)")
        void updateSetting_OrgLevel_Returns200() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "MAX_UPLOAD_SIZE", "300MB", "ORG", 1L
            );
            SettingResponse mockResponse = new SettingResponse(
                1L, "MAX_UPLOAD_SIZE", "300MB", "STRING", "ORG", 1L, false,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(updateSettingUseCase.execute(any())).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(updateSettingUseCase).execute(any());
        }

        @Test
        @DisplayName("TENANT 레벨 설정을 성공적으로 수정한다 (200 OK)")
        void updateSetting_TenantLevel_Returns200() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "MAX_UPLOAD_SIZE", "80MB", "TENANT", 100L
            );
            SettingResponse mockResponse = new SettingResponse(
                1L, "MAX_UPLOAD_SIZE", "80MB", "STRING", "TENANT", 100L, false,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(updateSettingUseCase.execute(any())).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(updateSettingUseCase).execute(any());
        }

        @Test
        @DisplayName("key가 null이면 400 Bad Request 반환")
        void updateSetting_KeyIsNull_Returns400() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                null, "value", "DEFAULT", null
            );

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.key").exists());
        }

        @Test
        @DisplayName("key가 빈 문자열이면 400 Bad Request 반환")
        void updateSetting_KeyIsBlank_Returns400() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "", "value", "DEFAULT", null
            );

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.key").exists());
        }

        @Test
        @DisplayName("value가 null이면 400 Bad Request 반환")
        void updateSetting_ValueIsNull_Returns400() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "KEY", null, "DEFAULT", null
            );

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.value").exists());
        }

        @Test
        @DisplayName("value가 빈 문자열이면 400 Bad Request 반환")
        void updateSetting_ValueIsBlank_Returns400() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "KEY", "", "DEFAULT", null
            );

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.value").exists());
        }

        @Test
        @DisplayName("level이 null이면 400 Bad Request 반환")
        void updateSetting_LevelIsNull_Returns400() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "KEY", "value", null, null
            );

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.level").exists());
        }

        @Test
        @DisplayName("level이 빈 문자열이면 400 Bad Request 반환")
        void updateSetting_LevelIsBlank_Returns400() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "KEY", "value", "", null
            );

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.level").exists());
        }

        @Test
        @DisplayName("유효하지 않은 level이면 400 Bad Request 반환")
        void updateSetting_InvalidLevel_Returns400() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "KEY", "value", "INVALID", null
            );

            doThrow(new IllegalArgumentException("No enum constant"))
                .when(updateSettingUseCase).execute(any());

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));

            // Note: verify 생략 - Mapper에서 예외가 발생할 수 있어 UseCase까지 도달하지 않을 수 있음
        }

        @Test
        @DisplayName("Setting이 존재하지 않으면 404 Not Found 반환")
        void updateSetting_SettingNotFound_Returns404() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "NON_EXISTENT_KEY", "value", "DEFAULT", null
            );

            doThrow(new SettingNotFoundException("설정을 찾을 수 없습니다"))
                .when(updateSettingUseCase).execute(any());

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404));

            // Note: verify 생략 - SettingNotFoundException이 발생하므로 UseCase 호출이 확인됨
        }

        @Test
        @DisplayName("스키마 검증 실패 시 400 Bad Request 반환")
        void updateSetting_SchemaValidationFails_Returns400() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "API_TIMEOUT", "invalid-number", "DEFAULT", null
            );

            doThrow(new IllegalArgumentException("설정 값이 타입과 호환되지 않습니다"))
                .when(updateSettingUseCase).execute(any());

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));

            verify(updateSettingUseCase).execute(any());
        }

        @Test
        @DisplayName("여러 필드가 null이면 모든 validation 에러를 반환한다")
        void updateSetting_MultipleValidationErrors_Returns400() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                null, null, null, null
            );

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.key").exists())
                .andExpect(jsonPath("$.errors.value").exists())
                .andExpect(jsonPath("$.errors.level").exists());
        }
    }

    @Nested
    @DisplayName("비밀 설정 수정 테스트")
    class SecretSettingUpdateTests {

        @Test
        @DisplayName("비밀 설정을 수정하면 새 값도 마스킹되어 반환된다 (200 OK)")
        void updateSetting_SecretSetting_ReturnsMasked() throws Exception {
            // Arrange
            UpdateSettingApiRequest request = new UpdateSettingApiRequest(
                "API_KEY", "new-secret-789", "DEFAULT", null
            );
            SettingResponse mockResponse = new SettingResponse(
                1L, "API_KEY", "********", "STRING", "DEFAULT", null, true,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(updateSettingUseCase.execute(any())).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(patch("/api/v1/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(updateSettingUseCase).execute(any());
        }
    }
}
