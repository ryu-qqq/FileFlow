package com.ryuqq.fileflow.application.settings.service;

import com.ryuqq.fileflow.application.settings.assembler.SettingAssembler;
import com.ryuqq.fileflow.application.settings.dto.SettingResponse;
import com.ryuqq.fileflow.application.settings.dto.UpdateSettingCommand;
import com.ryuqq.fileflow.application.settings.port.SchemaValidator;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.fixtures.SettingFixtures;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingNotFoundException;
import com.ryuqq.fileflow.domain.settings.SettingRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
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

    private SettingRepository settingRepository;
    private SchemaValidator schemaValidator;
    private SettingAssembler settingAssembler;
    private UpdateSettingUseCase updateSettingUseCase;

    @BeforeEach
    void setUp() {
        settingRepository = mock(SettingRepository.class);
        schemaValidator = mock(SchemaValidator.class);
        settingAssembler = mock(SettingAssembler.class);
        updateSettingUseCase = new UpdateSettingUseCase(settingRepository, schemaValidator, settingAssembler);
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
            UpdateSettingCommand command = new UpdateSettingCommand(key, newValue, "DEFAULT", null);

            Setting existingSetting = SettingFixtures.createDefaultSetting(); // 100MB
            Setting updatedSetting = SettingFixtures.createCustomDefaultSetting(key, newValue, SettingType.STRING);
            SettingResponse expectedResponse = new SettingResponse(
                null, key, newValue, SettingType.STRING.name(), "DEFAULT", null, false,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(settingRepository.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null)))
                .thenReturn(Optional.of(existingSetting));
            when(schemaValidator.validate(eq(newValue), eq(SettingType.STRING)))
                .thenReturn(true);
            when(settingRepository.save(any(Setting.class)))
                .thenReturn(updatedSetting);
            when(settingAssembler.toResponse(any(Setting.class)))
                .thenReturn(expectedResponse);

            // Act
            SettingResponse response = updateSettingUseCase.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getKey()).isEqualTo(key);
            assertThat(response.getValue()).isEqualTo(newValue);
            assertThat(response.getLevel()).isEqualTo("DEFAULT");

            verify(settingRepository).findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null));
            verify(schemaValidator).validate(eq(newValue), eq(SettingType.STRING));
            verify(settingRepository).save(any(Setting.class));
            verify(settingAssembler).toResponse(any(Setting.class));
        }

        @Test
        @DisplayName("ORG 레벨 설정을 성공적으로 수정한다")
        void shouldUpdateOrgSettingSuccessfully() {
            // Arrange
            String key = "MAX_UPLOAD_SIZE";
            String newValue = "300MB";
            Long orgId = 1L;
            UpdateSettingCommand command = new UpdateSettingCommand(key, newValue, "ORG", orgId);

            Setting existingSetting = SettingFixtures.createOrgSetting(orgId); // 200MB
            Setting updatedSetting = SettingFixtures.createCustomOrgSetting(key, newValue, SettingType.STRING, orgId);
            SettingResponse expectedResponse = new SettingResponse(
                null, key, newValue, SettingType.STRING.name(), "ORG", orgId, false,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(settingRepository.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.ORG), eq(orgId)))
                .thenReturn(Optional.of(existingSetting));
            when(schemaValidator.validate(eq(newValue), eq(SettingType.STRING)))
                .thenReturn(true);
            when(settingRepository.save(any(Setting.class)))
                .thenReturn(updatedSetting);
            when(settingAssembler.toResponse(any(Setting.class)))
                .thenReturn(expectedResponse);

            // Act
            SettingResponse response = updateSettingUseCase.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getKey()).isEqualTo(key);
            assertThat(response.getValue()).isEqualTo(newValue);
            assertThat(response.getLevel()).isEqualTo("ORG");
            assertThat(response.getContextId()).isEqualTo(orgId);

            verify(settingRepository).findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.ORG), eq(orgId));
            verify(settingRepository).save(any(Setting.class));
        }

        @Test
        @DisplayName("TENANT 레벨 설정을 성공적으로 수정한다")
        void shouldUpdateTenantSettingSuccessfully() {
            // Arrange
            String key = "MAX_UPLOAD_SIZE";
            String newValue = "80MB";
            Long tenantId = 100L;
            UpdateSettingCommand command = new UpdateSettingCommand(key, newValue, "TENANT", tenantId);

            Setting existingSetting = SettingFixtures.createTenantSetting(tenantId); // 50MB
            Setting updatedSetting = SettingFixtures.createCustomTenantSetting(key, newValue, SettingType.STRING, tenantId);
            SettingResponse expectedResponse = new SettingResponse(
                null, key, newValue, SettingType.STRING.name(), "TENANT", tenantId, false,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(settingRepository.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.TENANT), eq(tenantId)))
                .thenReturn(Optional.of(existingSetting));
            when(schemaValidator.validate(eq(newValue), eq(SettingType.STRING)))
                .thenReturn(true);
            when(settingRepository.save(any(Setting.class)))
                .thenReturn(updatedSetting);
            when(settingAssembler.toResponse(any(Setting.class)))
                .thenReturn(expectedResponse);

            // Act
            SettingResponse response = updateSettingUseCase.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getKey()).isEqualTo(key);
            assertThat(response.getValue()).isEqualTo(newValue);
            assertThat(response.getLevel()).isEqualTo("TENANT");
            assertThat(response.getContextId()).isEqualTo(tenantId);

            verify(settingRepository).findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.TENANT), eq(tenantId));
            verify(settingRepository).save(any(Setting.class));
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> updateSettingUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Command는 필수입니다");

            verify(settingRepository, never()).findByKeyAndLevel(any(), any(), any());
        }

        @Test
        @DisplayName("Setting이 존재하지 않으면 SettingNotFoundException 발생")
        void shouldThrowExceptionWhenSettingNotFound() {
            // Arrange
            String key = "NON_EXISTENT_KEY";
            UpdateSettingCommand command = new UpdateSettingCommand(key, "value", "DEFAULT", null);

            when(settingRepository.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> updateSettingUseCase.execute(command))
                .isInstanceOf(SettingNotFoundException.class);

            verify(settingRepository).findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null));
            verify(settingRepository, never()).save(any());
        }

        @Test
        @DisplayName("스키마 검증 실패 시 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenSchemaValidationFails() {
            // Arrange
            String key = "API_TIMEOUT";
            String invalidValue = "invalid-number";
            UpdateSettingCommand command = new UpdateSettingCommand(key, invalidValue, "DEFAULT", null);

            Setting existingSetting = SettingFixtures.createDefaultNumberSetting(); // API_TIMEOUT = 30

            when(settingRepository.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null)))
                .thenReturn(Optional.of(existingSetting));
            when(schemaValidator.validate(eq(invalidValue), eq(SettingType.NUMBER)))
                .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> updateSettingUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("설정 값이 타입과 호환되지 않습니다");

            verify(settingRepository).findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null));
            verify(schemaValidator).validate(eq(invalidValue), eq(SettingType.NUMBER));
            verify(settingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Command 검증")
    class CommandValidationScenarios {

        @Test
        @DisplayName("key가 null이면 예외 발생")
        void shouldThrowExceptionWhenKeyIsNull() {
            // Arrange
            UpdateSettingCommand command = new UpdateSettingCommand(null, "value", "DEFAULT", null);

            // Act & Assert
            assertThatThrownBy(() -> updateSettingUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("설정 키는 필수입니다");
        }

        @Test
        @DisplayName("key가 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenKeyIsBlank() {
            // Arrange
            UpdateSettingCommand command = new UpdateSettingCommand("", "value", "DEFAULT", null);

            // Act & Assert
            assertThatThrownBy(() -> updateSettingUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("설정 키는 필수입니다");
        }

        @Test
        @DisplayName("value가 null이면 예외 발생")
        void shouldThrowExceptionWhenValueIsNull() {
            // Arrange
            UpdateSettingCommand command = new UpdateSettingCommand("KEY", null, "DEFAULT", null);

            // Act & Assert
            assertThatThrownBy(() -> updateSettingUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("설정 값은 필수입니다");
        }

        @Test
        @DisplayName("level이 null이면 예외 발생")
        void shouldThrowExceptionWhenLevelIsNull() {
            // Arrange
            UpdateSettingCommand command = new UpdateSettingCommand("KEY", "value", null, null);

            // Act & Assert
            assertThatThrownBy(() -> updateSettingUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("설정 레벨은 필수입니다");
        }

        @Test
        @DisplayName("level이 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenLevelIsBlank() {
            // Arrange
            UpdateSettingCommand command = new UpdateSettingCommand("KEY", "value", "", null);

            // Act & Assert
            assertThatThrownBy(() -> updateSettingUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("설정 레벨은 필수입니다");
        }

        @Test
        @DisplayName("유효하지 않은 level이면 예외 발생")
        void shouldThrowExceptionWhenLevelIsInvalid() {
            // Arrange
            UpdateSettingCommand command = new UpdateSettingCommand("KEY", "value", "INVALID", null);

            Setting existingSetting = SettingFixtures.createDefaultSetting();

            when(settingRepository.findByKeyAndLevel(any(), any(), any()))
                .thenReturn(Optional.of(existingSetting));

            // Act & Assert
            assertThatThrownBy(() -> updateSettingUseCase.execute(command))
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
            UpdateSettingCommand command = new UpdateSettingCommand(key, newSecretValue, "DEFAULT", null);

            Setting existingSecretSetting = SettingFixtures.createDefaultSecretSetting(); // API_KEY = secret-key-123
            Setting updatedSecretSetting = SettingFixtures.createDefaultSecretSetting();
            SettingResponse expectedResponse = new SettingResponse(
                null, key, "********", SettingType.STRING.name(), "DEFAULT", null, true,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(settingRepository.findByKeyAndLevel(any(SettingKey.class), eq(SettingLevel.DEFAULT), eq(null)))
                .thenReturn(Optional.of(existingSecretSetting));
            when(schemaValidator.validate(eq(newSecretValue), eq(SettingType.STRING)))
                .thenReturn(true);
            when(settingRepository.save(any(Setting.class)))
                .thenReturn(updatedSecretSetting);
            when(settingAssembler.toResponse(any(Setting.class)))
                .thenReturn(expectedResponse);

            // Act
            SettingResponse response = updateSettingUseCase.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.isSecret()).isTrue();
            assertThat(response.getValue()).isEqualTo("********"); // 마스킹됨

            verify(settingRepository).save(any(Setting.class));
        }
    }
}
