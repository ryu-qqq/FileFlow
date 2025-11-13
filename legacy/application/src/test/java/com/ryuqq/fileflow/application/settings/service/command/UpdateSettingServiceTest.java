package com.ryuqq.fileflow.application.settings.service.command;

import com.ryuqq.fileflow.application.settings.fixture.UpdateSettingCommandFixture;
import com.ryuqq.fileflow.application.settings.assembler.SettingAssembler;
import com.ryuqq.fileflow.application.settings.port.SchemaValidator;
import com.ryuqq.fileflow.application.settings.port.in.UpdateSettingUseCase;
import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort;
import com.ryuqq.fileflow.application.settings.port.out.SaveSettingPort;
import com.ryuqq.fileflow.domain.settings.exception.InvalidSettingException;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.fixture.SettingFixture;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

/**
 * UpdateSettingService 단위 테스트
 *
 * <p>테스트 범위:</p>
 * <ul>
 *   <li>Happy Path: 정상 설정 업데이트</li>
 *   <li>Upsert Logic: 없으면 생성, 있으면 수정</li>
 *   <li>Transaction Boundary: @Transactional 경계 검증</li>
 *   <li>Port Mock: 의존성 Port 모킹</li>
 *   <li>Schema Validation: JSON 스키마 검증</li>
 *   <li>Secret Preservation: 기존 secret 여부 유지</li>
 *   <li>Type Preservation: 기존 type 유지</li>
 *   <li>Exception Handling: 예외 처리</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSettingService 단위 테스트")
class UpdateSettingServiceTest {

    @Mock
    private LoadSettingsPort loadSettingsPort;

    @Mock
    private SaveSettingPort saveSettingPort;

    @Mock
    private SchemaValidator schemaValidator;

    @Mock
    private SettingAssembler settingAssembler;

    @InjectMocks
    private UpdateSettingService updateSettingService;

    @Nested
    @DisplayName("Happy Path - 기존 설정 수정")
    class UpdateExistingSettingTests {

        @Test
        @DisplayName("기존 설정이 있으면 값만 업데이트 (type, secret 유지)")
        void execute_UpdatesValue_WhenSettingExists() {
            // Given
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.createDefaultLevel();
            Setting existingSetting = SettingFixture.createWithId(1L, "app.default.config", "old-value", SettingLevel.DEFAULT, null);
            Setting updatedSetting = SettingFixture.createWithId(1L, "app.default.config", "updated-default-value", SettingLevel.DEFAULT, null);

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.of(existingSetting));
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(updatedSetting);
            given(settingAssembler.toUpdateResponse(any(Setting.class)))
                .willReturn(new UpdateSettingUseCase.Response(
                    updatedSetting.getIdValue(),
                    updatedSetting.getKeyValue(),
                    updatedSetting.getDisplayValue(),
                    updatedSetting.getValueType().name(),
                    updatedSetting.getLevel().name(),
                    updatedSetting.getContextId(),
                    updatedSetting.isSecret(),
                    updatedSetting.getCreatedAt(),
                    updatedSetting.getUpdatedAt()
                ));

            // When
            UpdateSettingUseCase.Response response = updateSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.key()).isEqualTo("app.default.config");
            assertThat(response.value()).isEqualTo("updated-default-value");

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any());
            verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort).save(any(Setting.class));
            verify(settingAssembler).toUpdateResponse(any(Setting.class));
        }

        @Test
        @DisplayName("기존 설정의 secret 여부 유지")
        void execute_PreservesSecretFlag_WhenUpdatingExistingSetting() {
            // Given
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.create("api.key", "new-secret-value", "DEFAULT", null);
            Setting existingSecretSetting = SettingFixture.builder()
                .id(1L)
                .key("api.key")
                .value("old-secret-value")
                .secret(true)
                .build();
            Setting updatedSecretSetting = SettingFixture.builder()
                .id(1L)
                .key("api.key")
                .value("new-secret-value")
                .secret(true)
                .build();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.of(existingSecretSetting));
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(updatedSecretSetting);
            given(settingAssembler.toUpdateResponse(any(Setting.class)))
                .willReturn(new UpdateSettingUseCase.Response(
                    updatedSecretSetting.getIdValue(),
                    updatedSecretSetting.getKeyValue(),
                    updatedSecretSetting.getDisplayValue(),
                    updatedSecretSetting.getValueType().name(),
                    updatedSecretSetting.getLevel().name(),
                    updatedSecretSetting.getContextId(),
                    updatedSecretSetting.isSecret(),
                    updatedSecretSetting.getCreatedAt(),
                    updatedSecretSetting.getUpdatedAt()
                ));

            // When
            UpdateSettingUseCase.Response response = updateSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.secret()).isTrue();
            assertThat(response.value()).isEqualTo("********");

            verify(saveSettingPort).save(any(Setting.class));
        }

        @Test
        @DisplayName("기존 설정의 type 유지")
        void execute_PreservesType_WhenUpdatingExistingSetting() {
            // Given
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.createNumberUpdate();
            Setting existingNumberSetting = SettingFixture.createWithId(1L, "file.max-size", "1024", SettingLevel.DEFAULT, null);
            Setting updatedNumberSetting = SettingFixture.builder()
                .id(1L)
                .key("file.max-size")
                .value("2048")
                .type(SettingType.NUMBER)
                .build();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.of(existingNumberSetting));
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(updatedNumberSetting);
            given(settingAssembler.toUpdateResponse(any(Setting.class)))
                .willReturn(new UpdateSettingUseCase.Response(
                    updatedNumberSetting.getIdValue(),
                    updatedNumberSetting.getKeyValue(),
                    updatedNumberSetting.getDisplayValue(),
                    updatedNumberSetting.getValueType().name(),
                    updatedNumberSetting.getLevel().name(),
                    updatedNumberSetting.getContextId(),
                    updatedNumberSetting.isSecret(),
                    updatedNumberSetting.getCreatedAt(),
                    updatedNumberSetting.getUpdatedAt()
                ));

            // When
            UpdateSettingUseCase.Response response = updateSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.valueType()).isEqualTo("NUMBER");

            verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort).save(any(Setting.class));
        }
    }

    @Nested
    @DisplayName("Upsert Logic - 없으면 생성")
    class UpsertCreateTests {

        @Test
        @DisplayName("기존 설정이 없으면 새로 생성 (type=STRING, secret=false 기본값)")
        void execute_CreatesNewSetting_WhenSettingDoesNotExist() {
            // Given
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.createDefaultLevel();
            Setting newSetting = SettingFixture.createDefaultLevel();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(newSetting);
            given(settingAssembler.toUpdateResponse(any(Setting.class)))
                .willReturn(new UpdateSettingUseCase.Response(
                    newSetting.getIdValue(),
                    newSetting.getKeyValue(),
                    newSetting.getDisplayValue(),
                    newSetting.getValueType().name(),
                    newSetting.getLevel().name(),
                    newSetting.getContextId(),
                    newSetting.isSecret(),
                    newSetting.getCreatedAt(),
                    newSetting.getUpdatedAt()
                ));

            // When
            UpdateSettingUseCase.Response response = updateSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.key()).isEqualTo("app.default.config");
            assertThat(response.valueType()).isEqualTo("STRING");
            assertThat(response.secret()).isFalse();

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any());
            verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort).save(any(Setting.class));
        }

        @Test
        @DisplayName("새로 생성 시 'password' 패턴 키는 자동으로 secret=true")
        void execute_AutoDetectsSecret_WhenCreatingNewSetting() {
            // Given
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.create("db.password", "secret-value", "DEFAULT", null);
            Setting newSecretSetting = SettingFixture.builder()
                .key("db.password")
                .value("secret-value")
                .secret(true)
                .build();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(newSecretSetting);
            given(settingAssembler.toUpdateResponse(any(Setting.class)))
                .willReturn(new UpdateSettingUseCase.Response(
                    newSecretSetting.getIdValue(),
                    newSecretSetting.getKeyValue(),
                    newSecretSetting.getDisplayValue(),
                    newSecretSetting.getValueType().name(),
                    newSecretSetting.getLevel().name(),
                    newSecretSetting.getContextId(),
                    newSecretSetting.isSecret(),
                    newSecretSetting.getCreatedAt(),
                    newSecretSetting.getUpdatedAt()
                ));

            // When
            UpdateSettingUseCase.Response response = updateSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.secret()).isTrue();
            assertThat(response.value()).isEqualTo("********");

            verify(saveSettingPort).save(any(Setting.class));
        }

        @Test
        @DisplayName("TENANT 레벨 신규 생성 성공")
        void execute_CreatesNewSetting_TenantLevel() {
            // Given
            Long tenantId = 100L;
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.createTenantLevel(tenantId);
            Setting newTenantSetting = SettingFixture.createTenantLevel(tenantId);

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(newTenantSetting);
            given(settingAssembler.toUpdateResponse(any(Setting.class)))
                .willReturn(new UpdateSettingUseCase.Response(
                    newTenantSetting.getIdValue(),
                    newTenantSetting.getKeyValue(),
                    newTenantSetting.getDisplayValue(),
                    newTenantSetting.getValueType().name(),
                    newTenantSetting.getLevel().name(),
                    newTenantSetting.getContextId(),
                    newTenantSetting.isSecret(),
                    newTenantSetting.getCreatedAt(),
                    newTenantSetting.getUpdatedAt()
                ));

            // When
            UpdateSettingUseCase.Response response = updateSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.level()).isEqualTo("TENANT");
            assertThat(response.contextId()).isEqualTo(tenantId);

            verify(saveSettingPort).save(any(Setting.class));
        }

        @Test
        @DisplayName("ORG 레벨 신규 생성 성공")
        void execute_CreatesNewSetting_OrgLevel() {
            // Given
            Long orgId = 10L;
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.createOrgLevel(orgId);
            Setting newOrgSetting = SettingFixture.createOrgLevel(orgId);

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(newOrgSetting);
            given(settingAssembler.toUpdateResponse(any(Setting.class)))
                .willReturn(new UpdateSettingUseCase.Response(
                    newOrgSetting.getIdValue(),
                    newOrgSetting.getKeyValue(),
                    newOrgSetting.getDisplayValue(),
                    newOrgSetting.getValueType().name(),
                    newOrgSetting.getLevel().name(),
                    newOrgSetting.getContextId(),
                    newOrgSetting.isSecret(),
                    newOrgSetting.getCreatedAt(),
                    newOrgSetting.getUpdatedAt()
                ));

            // When
            UpdateSettingUseCase.Response response = updateSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.level()).isEqualTo("ORG");
            assertThat(response.contextId()).isEqualTo(orgId);

            verify(saveSettingPort).save(any(Setting.class));
        }
    }

    @Nested
    @DisplayName("Schema Validation - 스키마 검증")
    class SchemaValidationTests {

        @Test
        @DisplayName("스키마 검증 실패 시 InvalidSettingException 발생")
        void execute_ThrowsException_WhenSchemaValidationFails() {
            // Given
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.createNumberUpdate();
            Setting existingSetting = SettingFixture.createNumberSetting();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.of(existingSetting));
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(false);

            // When & Then
            assertThatThrownBy(() -> updateSettingService.execute(command))
                .isInstanceOf(InvalidSettingException.class)
                .hasMessageContaining("설정 값이 타입과 호환되지 않습니다");

            verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort, never()).save(any(Setting.class));
        }

        @Test
        @DisplayName("신규 생성 시 스키마 검증 실패하면 예외 발생")
        void execute_ThrowsException_WhenSchemaValidationFailsOnCreate() {
            // Given
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.create();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(false);

            // When & Then
            assertThatThrownBy(() -> updateSettingService.execute(command))
                .isInstanceOf(InvalidSettingException.class)
                .hasMessageContaining("타입과 호환되지 않습니다");

            verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort, never()).save(any(Setting.class));
        }
    }

    @Nested
    @DisplayName("Command Validation - 입력 검증")
    class CommandValidationTests {

        @Test
        @DisplayName("level이 유효하지 않으면 예외 발생")
        void execute_ThrowsException_WhenInvalidLevel() {
            // Given
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.create(
                "app.config",
                "value",
                "INVALID_LEVEL",
                null
            );

            // When & Then
            assertThatThrownBy(() -> updateSettingService.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

            verify(saveSettingPort, never()).save(any(Setting.class));
        }
    }

    @Nested
    @DisplayName("Transaction Boundary - 트랜잭션 경계")
    class TransactionBoundaryTests {

        @Test
        @DisplayName("execute() 메서드는 @Transactional이 있어야 함")
        void executeMethod_ShouldHaveTransactional() throws NoSuchMethodException {
            // Given
            Method executeMethod = UpdateSettingService.class.getMethod("execute", UpdateSettingUseCase.Command.class);

            // Then
            assertThat(executeMethod.isAnnotationPresent(Transactional.class)
                || UpdateSettingService.class.isAnnotationPresent(Transactional.class))
                .isTrue();
        }

        @Test
        @DisplayName("execute() 메서드는 Public이어야 함 (@Transactional은 Public 메서드에만 작동)")
        void executeMethod_ShouldBePublic() throws NoSuchMethodException {
            // Given
            Method executeMethod = UpdateSettingService.class.getMethod("execute", UpdateSettingUseCase.Command.class);

            // Then
            assertThat(Modifier.isPublic(executeMethod.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Service 클래스는 Final이 아니어야 함 (Spring Proxy 제약)")
        void serviceClass_ShouldNotBeFinal() {
            // Then
            assertThat(Modifier.isFinal(UpdateSettingService.class.getModifiers())).isFalse();
        }
    }

    @Nested
    @DisplayName("Port Interaction - Port 상호작용")
    class PortInteractionTests {

        @Test
        @DisplayName("기존 설정 수정 시 Port 호출 순서: LoadSettingsPort → SchemaValidator → SaveSettingPort → Assembler")
        void execute_CallsPortsInCorrectOrder_WhenUpdating() {
            // Given
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.create();
            Setting existingSetting = SettingFixture.createNew();
            Setting updatedSetting = SettingFixture.createWithId(1L);

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.of(existingSetting));
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(updatedSetting);
            given(settingAssembler.toUpdateResponse(any(Setting.class)))
                .willReturn(new UpdateSettingUseCase.Response(
                    updatedSetting.getIdValue(),
                    updatedSetting.getKeyValue(),
                    updatedSetting.getDisplayValue(),
                    updatedSetting.getValueType().name(),
                    updatedSetting.getLevel().name(),
                    updatedSetting.getContextId(),
                    updatedSetting.isSecret(),
                    updatedSetting.getCreatedAt(),
                    updatedSetting.getUpdatedAt()
                ));

            // When
            updateSettingService.execute(command);

            // Then
            var inOrder = org.mockito.Mockito.inOrder(
                loadSettingsPort,
                schemaValidator,
                saveSettingPort,
                settingAssembler
            );
            inOrder.verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any());
            inOrder.verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            inOrder.verify(saveSettingPort).save(any(Setting.class));
            inOrder.verify(settingAssembler).toUpdateResponse(any(Setting.class));
        }

        @Test
        @DisplayName("신규 생성 시 Port 호출 순서: LoadSettingsPort → SchemaValidator → SaveSettingPort → Assembler")
        void execute_CallsPortsInCorrectOrder_WhenCreating() {
            // Given
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.create();
            Setting newSetting = SettingFixture.createNew();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(newSetting);
            given(settingAssembler.toUpdateResponse(any(Setting.class)))
                .willReturn(new UpdateSettingUseCase.Response(
                    newSetting.getIdValue(),
                    newSetting.getKeyValue(),
                    newSetting.getDisplayValue(),
                    newSetting.getValueType().name(),
                    newSetting.getLevel().name(),
                    newSetting.getContextId(),
                    newSetting.isSecret(),
                    newSetting.getCreatedAt(),
                    newSetting.getUpdatedAt()
                ));

            // When
            updateSettingService.execute(command);

            // Then
            var inOrder = org.mockito.Mockito.inOrder(
                loadSettingsPort,
                schemaValidator,
                saveSettingPort,
                settingAssembler
            );
            inOrder.verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any());
            inOrder.verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            inOrder.verify(saveSettingPort).save(any(Setting.class));
            inOrder.verify(settingAssembler).toUpdateResponse(any(Setting.class));
        }

        @Test
        @DisplayName("스키마 검증 실패 시 SaveSettingPort는 호출되지 않음")
        void execute_DoesNotCallSavePort_WhenSchemaValidationFails() {
            // Given
            UpdateSettingUseCase.Command command = UpdateSettingCommandFixture.create();
            Setting existingSetting = SettingFixture.createNew();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.of(existingSetting));
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(false);

            // When & Then
            assertThatThrownBy(() -> updateSettingService.execute(command))
                .isInstanceOf(InvalidSettingException.class);

            verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort, never()).save(any(Setting.class));
            verify(settingAssembler, never()).toUpdateResponse(any(Setting.class));
        }
    }
}
