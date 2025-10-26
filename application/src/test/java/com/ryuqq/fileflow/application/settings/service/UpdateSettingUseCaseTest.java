package com.ryuqq.fileflow.application.settings.service;

import com.ryuqq.fileflow.application.settings.assembler.SettingAssembler;
import com.ryuqq.fileflow.application.settings.port.SchemaValidator;
import com.ryuqq.fileflow.application.settings.port.in.UpdateSettingUseCase;
import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort;
import com.ryuqq.fileflow.application.settings.port.out.SaveSettingPort;
import com.ryuqq.fileflow.application.settings.service.command.UpdateSettingService;
import com.ryuqq.fileflow.domain.settings.exception.InvalidSettingException;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.fixtures.SettingFixtures;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.exception.SettingNotFoundException;
import com.ryuqq.fileflow.domain.settings.SettingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UpdateSettingUseCase 테스트
 *
 * <p>설정 업데이트 UseCase의 정확성을 검증합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("UpdateSettingUseCase 테스트")
class UpdateSettingUseCaseTest {

    private LoadSettingsPort loadSettingsPort;
    private SaveSettingPort saveSettingPort;
    private SchemaValidator schemaValidator;
    private SettingAssembler settingAssembler;
    private UpdateSettingService updateSettingService;

    @BeforeEach
    void setUp() {
        loadSettingsPort = mock(LoadSettingsPort.class);
        saveSettingPort = mock(SaveSettingPort.class);
        schemaValidator = mock(SchemaValidator.class);
        settingAssembler = mock(SettingAssembler.class);
        updateSettingService = new UpdateSettingService(
            loadSettingsPort, saveSettingPort, schemaValidator, settingAssembler
        );
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("DEFAULT 레벨 설정을 성공적으로 수정한다")
        void shouldUpdateDefaultSettingSuccessfully() {
            // Arrange
            String key = "MAX_UPLOAD_SIZE";
            String newValue = "200MB";
            UpdateSettingUseCase.Command command = new UpdateSettingUseCase.Command(key, newValue, "DEFAULT", null);

            Setting existingSetting = SettingFixtures.createDefaultSetting(); // 100MB
            Setting updatedSetting = SettingFixtures.createCustomDefaultSetting(key, newValue, SettingType.STRING);
            UpdateSettingUseCase.Response expectedResponse = new UpdateSettingUseCase.Response(
                null, key, newValue, SettingType.STRING.name(), "DEFAULT", null, false,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null)))
                .thenReturn(Optional.of(existingSetting));
            when(schemaValidator.validate(eq(newValue), eq(SettingType.STRING)))
                .thenReturn(true);
            when(saveSettingPort.save(any(Setting.class)))
                .thenReturn(updatedSetting);
            when(settingAssembler.toUpdateResponse(any(Setting.class)))
                .thenReturn(expectedResponse);

            // Act
            UpdateSettingUseCase.Response response = updateSettingService.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.key()).isEqualTo(key);
            assertThat(response.value()).isEqualTo(newValue);
            assertThat(response.level()).isEqualTo("DEFAULT");

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null));
            verify(schemaValidator).validate(eq(newValue), eq(SettingType.STRING));
            verify(saveSettingPort).save(any(Setting.class));
            verify(settingAssembler).toUpdateResponse(any(Setting.class));
        }

        @Test
        @DisplayName("ORG 레벨 설정을 성공적으로 수정한다")
        void shouldUpdateOrgSettingSuccessfully() {
            // Arrange
            String key = "MAX_UPLOAD_SIZE";
            String newValue = "300MB";
            Long orgId = 1L;
            UpdateSettingUseCase.Command command = new UpdateSettingUseCase.Command(key, newValue, "ORG", orgId);

            Setting existingSetting = SettingFixtures.createOrgSetting(orgId); // 200MB
            Setting updatedSetting = SettingFixtures.createCustomOrgSetting(key, newValue, SettingType.STRING, orgId);
            UpdateSettingUseCase.Response expectedResponse = new UpdateSettingUseCase.Response(
                null, key, newValue, SettingType.STRING.name(), "ORG", orgId, false,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.ORG), eq(orgId)))
                .thenReturn(Optional.of(existingSetting));
            when(schemaValidator.validate(eq(newValue), eq(SettingType.STRING)))
                .thenReturn(true);
            when(saveSettingPort.save(any(Setting.class)))
                .thenReturn(updatedSetting);
            when(settingAssembler.toUpdateResponse(any(Setting.class)))
                .thenReturn(expectedResponse);

            // Act
            UpdateSettingUseCase.Response response = updateSettingService.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.key()).isEqualTo(key);
            assertThat(response.value()).isEqualTo(newValue);
            assertThat(response.level()).isEqualTo("ORG");
            assertThat(response.contextId()).isEqualTo(orgId);

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.ORG), eq(orgId));
            verify(saveSettingPort).save(any(Setting.class));
        }

        @Test
        @DisplayName("TENANT 레벨 설정을 성공적으로 수정한다")
        void shouldUpdateTenantSettingSuccessfully() {
            // Arrange
            String key = "MAX_UPLOAD_SIZE";
            String newValue = "80MB";
            Long tenantId = 100L;
            UpdateSettingUseCase.Command command = new UpdateSettingUseCase.Command(key, newValue, "TENANT", tenantId);

            Setting existingSetting = SettingFixtures.createTenantSetting(tenantId); // 50MB
            Setting updatedSetting = SettingFixtures.createCustomTenantSetting(key, newValue, SettingType.STRING, tenantId);
            UpdateSettingUseCase.Response expectedResponse = new UpdateSettingUseCase.Response(
                null, key, newValue, SettingType.STRING.name(), "TENANT", tenantId, false,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.TENANT), eq(tenantId)))
                .thenReturn(Optional.of(existingSetting));
            when(schemaValidator.validate(eq(newValue), eq(SettingType.STRING)))
                .thenReturn(true);
            when(saveSettingPort.save(any(Setting.class)))
                .thenReturn(updatedSetting);
            when(settingAssembler.toUpdateResponse(any(Setting.class)))
                .thenReturn(expectedResponse);

            // Act
            UpdateSettingUseCase.Response response = updateSettingService.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.key()).isEqualTo(key);
            assertThat(response.value()).isEqualTo(newValue);
            assertThat(response.level()).isEqualTo("TENANT");
            assertThat(response.contextId()).isEqualTo(tenantId);

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.TENANT), eq(tenantId));
            verify(saveSettingPort).save(any(Setting.class));
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Command가 null이면 NullPointerException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> updateSettingService.execute(null))
                .isInstanceOf(NullPointerException.class);

            verify(loadSettingsPort, never()).findByKeyAndLevel(any(), any(), any());
        }

        @Test
        @DisplayName("Setting이 존재하지 않으면 SettingNotFoundException 발생")
        void shouldThrowExceptionWhenSettingNotFound() {
            // Arrange
            String key = "NON_EXISTENT_KEY";
            UpdateSettingUseCase.Command command = new UpdateSettingUseCase.Command(key, "value", "DEFAULT", null);

            when(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> updateSettingService.execute(command))
                .isInstanceOf(SettingNotFoundException.class);

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null));
            verify(saveSettingPort, never()).save(any());
        }

        @Test
        @DisplayName("스키마 검증 실패 시 InvalidSettingException 발생")
        void shouldThrowExceptionWhenSchemaValidationFails() {
            // Arrange
            String key = "API_TIMEOUT";
            String invalidValue = "invalid-number";
            UpdateSettingUseCase.Command command = new UpdateSettingUseCase.Command(key, invalidValue, "DEFAULT", null);

            Setting existingSetting = SettingFixtures.createDefaultNumberSetting(); // API_TIMEOUT = 30

            when(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null)))
                .thenReturn(Optional.of(existingSetting));
            when(schemaValidator.validate(eq(invalidValue), eq(SettingType.NUMBER)))
                .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> updateSettingService.execute(command))
                .isInstanceOf(InvalidSettingException.class)
                .hasMessageContaining("설정 값이 타입과 호환되지 않습니다");

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null));
            verify(schemaValidator).validate(eq(invalidValue), eq(SettingType.NUMBER));
            verify(saveSettingPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Command 검증")
    class CommandValidationScenarios {

        @Test
        @DisplayName("key가 null이면 예외 발생")
        void shouldThrowExceptionWhenKeyIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> new UpdateSettingUseCase.Command(null, "value", "DEFAULT", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("설정 키는 필수입니다");
        }

        @Test
        @DisplayName("key가 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenKeyIsBlank() {
            // Act & Assert
            assertThatThrownBy(() -> new UpdateSettingUseCase.Command("", "value", "DEFAULT", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("설정 키는 필수입니다");
        }

        @Test
        @DisplayName("value가 null이면 예외 발생")
        void shouldThrowExceptionWhenValueIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> new UpdateSettingUseCase.Command("KEY", null, "DEFAULT", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("설정 값은 필수입니다");
        }

        @Test
        @DisplayName("level이 null이면 예외 발생")
        void shouldThrowExceptionWhenLevelIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> new UpdateSettingUseCase.Command("KEY", "value", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("설정 레벨은 필수입니다");
        }

        @Test
        @DisplayName("level이 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenLevelIsBlank() {
            // Act & Assert
            assertThatThrownBy(() -> new UpdateSettingUseCase.Command("KEY", "value", "", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("설정 레벨은 필수입니다");
        }

        @Test
        @DisplayName("유효하지 않은 level이면 예외 발생")
        void shouldThrowExceptionWhenLevelIsInvalid() {
            // Arrange
            UpdateSettingUseCase.Command command = new UpdateSettingUseCase.Command("KEY", "value", "INVALID", null);

            Setting existingSetting = SettingFixtures.createDefaultSetting();

            when(loadSettingsPort.findByKeyAndLevel(any(), any(), any()))
                .thenReturn(Optional.of(existingSetting));

            // Act & Assert
            assertThatThrownBy(() -> updateSettingService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No enum constant")
                .hasMessageContaining("INVALID");
        }
    }

    @Nested
    @DisplayName("비밀 설정 수정 테스트")
    class SecretSettingUpdateScenarios {

        @Test
        @DisplayName("비밀 설정을 수정하면 새 값도 비밀로 유지된다")
        void shouldMaintainSecretFlagWhenUpdatingSecretSetting() {
            // Arrange
            String key = "API_KEY";
            String newSecretValue = "new-secret-789";
            UpdateSettingUseCase.Command command = new UpdateSettingUseCase.Command(key, newSecretValue, "DEFAULT", null);

            Setting existingSecretSetting = SettingFixtures.createDefaultSecretSetting(); // API_KEY = secret-key-123
            Setting updatedSecretSetting = SettingFixtures.createDefaultSecretSetting();
            UpdateSettingUseCase.Response expectedResponse = new UpdateSettingUseCase.Response(
                null, key, "********", SettingType.STRING.name(), "DEFAULT", null, true,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null)))
                .thenReturn(Optional.of(existingSecretSetting));
            when(schemaValidator.validate(eq(newSecretValue), eq(SettingType.STRING)))
                .thenReturn(true);
            when(saveSettingPort.save(any(Setting.class)))
                .thenReturn(updatedSecretSetting);
            when(settingAssembler.toUpdateResponse(any(Setting.class)))
                .thenReturn(expectedResponse);

            // Act
            UpdateSettingUseCase.Response response = updateSettingService.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.secret()).isTrue();
            assertThat(response.value()).isEqualTo("********"); // 마스킹됨

            verify(saveSettingPort).save(any(Setting.class));
        }
    }
}
