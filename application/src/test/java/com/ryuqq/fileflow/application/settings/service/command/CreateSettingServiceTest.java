package com.ryuqq.fileflow.application.settings.service.command;

import com.ryuqq.fileflow.application.settings.fixture.CreateSettingCommandFixture;
import com.ryuqq.fileflow.application.settings.assembler.SettingAssembler;
import com.ryuqq.fileflow.application.settings.port.SchemaValidator;
import com.ryuqq.fileflow.application.settings.port.in.CreateSettingUseCase;
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
 * CreateSettingService 단위 테스트
 *
 * <p>테스트 범위:</p>
 * <ul>
 *   <li>Happy Path: 정상 설정 생성</li>
 *   <li>Transaction Boundary: @Transactional 경계 검증</li>
 *   <li>Port Mock: 의존성 Port 모킹</li>
 *   <li>Command Validation: 입력 검증</li>
 *   <li>Duplication Check: 중복 검증</li>
 *   <li>Schema Validation: JSON 스키마 검증</li>
 *   <li>Secret Key Handling: 비밀 키 자동 감지</li>
 *   <li>Exception Handling: 예외 처리</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateSettingService 단위 테스트")
class CreateSettingServiceTest {

    @Mock
    private LoadSettingsPort loadSettingsPort;

    @Mock
    private SaveSettingPort saveSettingPort;

    @Mock
    private SchemaValidator schemaValidator;

    @Mock
    private SettingAssembler settingAssembler;

    @InjectMocks
    private CreateSettingService createSettingService;

    @Nested
    @DisplayName("Happy Path - 정상 케이스")
    class HappyPathTests {

        @Test
        @DisplayName("유효한 Command로 설정 생성 성공 - DEFAULT 레벨")
        void execute_Success_DefaultLevel() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.createDefaultLevel();
            Setting expectedSetting = SettingFixture.createDefaultLevel();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(expectedSetting);
            given(settingAssembler.toCreateResponse(any(Setting.class)))
                .willReturn(new CreateSettingUseCase.Response(
                    expectedSetting.getIdValue(),
                    expectedSetting.getKeyValue(),
                    expectedSetting.getDisplayValue(),
                    expectedSetting.getValueType().name(),
                    expectedSetting.getLevel().name(),
                    expectedSetting.getContextId(),
                    expectedSetting.isSecret(),
                    expectedSetting.getCreatedAt(),
                    expectedSetting.getUpdatedAt()
                ));

            // When
            CreateSettingUseCase.Response response = createSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.key()).isEqualTo(expectedSetting.getKeyValue());
            assertThat(response.level()).isEqualTo(expectedSetting.getLevel().name());

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any());
            verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort).save(any(Setting.class));
            verify(settingAssembler).toCreateResponse(any(Setting.class));
        }

        @Test
        @DisplayName("유효한 Command로 설정 생성 성공 - TENANT 레벨")
        void execute_Success_TenantLevel() {
            // Given
            Long tenantId = 100L;
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.createTenantLevel(tenantId);
            Setting expectedSetting = SettingFixture.createTenantLevel(tenantId);

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(expectedSetting);
            given(settingAssembler.toCreateResponse(any(Setting.class)))
                .willReturn(new CreateSettingUseCase.Response(
                    expectedSetting.getIdValue(),
                    expectedSetting.getKeyValue(),
                    expectedSetting.getDisplayValue(),
                    expectedSetting.getValueType().name(),
                    expectedSetting.getLevel().name(),
                    expectedSetting.getContextId(),
                    expectedSetting.isSecret(),
                    expectedSetting.getCreatedAt(),
                    expectedSetting.getUpdatedAt()
                ));

            // When
            CreateSettingUseCase.Response response = createSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.contextId()).isEqualTo(tenantId);
            assertThat(response.level()).isEqualTo("TENANT");

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any());
            verify(saveSettingPort).save(any(Setting.class));
        }

        @Test
        @DisplayName("유효한 Command로 설정 생성 성공 - ORG 레벨")
        void execute_Success_OrgLevel() {
            // Given
            Long orgId = 10L;
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.createOrgLevel(orgId);
            Setting expectedSetting = SettingFixture.createOrgLevel(orgId);

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(expectedSetting);
            given(settingAssembler.toCreateResponse(any(Setting.class)))
                .willReturn(new CreateSettingUseCase.Response(
                    expectedSetting.getIdValue(),
                    expectedSetting.getKeyValue(),
                    expectedSetting.getDisplayValue(),
                    expectedSetting.getValueType().name(),
                    expectedSetting.getLevel().name(),
                    expectedSetting.getContextId(),
                    expectedSetting.isSecret(),
                    expectedSetting.getCreatedAt(),
                    expectedSetting.getUpdatedAt()
                ));

            // When
            CreateSettingUseCase.Response response = createSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.contextId()).isEqualTo(orgId);
            assertThat(response.level()).isEqualTo("ORG");

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any());
            verify(saveSettingPort).save(any(Setting.class));
        }

        @Test
        @DisplayName("NUMBER 타입 설정 생성 성공")
        void execute_Success_NumberType() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.createNumberSetting();
            Setting expectedSetting = SettingFixture.createNumberSetting();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(expectedSetting);
            given(settingAssembler.toCreateResponse(any(Setting.class)))
                .willReturn(new CreateSettingUseCase.Response(
                    expectedSetting.getIdValue(),
                    expectedSetting.getKeyValue(),
                    expectedSetting.getDisplayValue(),
                    expectedSetting.getValueType().name(),
                    expectedSetting.getLevel().name(),
                    expectedSetting.getContextId(),
                    expectedSetting.isSecret(),
                    expectedSetting.getCreatedAt(),
                    expectedSetting.getUpdatedAt()
                ));

            // When
            CreateSettingUseCase.Response response = createSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.valueType()).isEqualTo("NUMBER");

            verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort).save(any(Setting.class));
        }

        @Test
        @DisplayName("BOOLEAN 타입 설정 생성 성공")
        void execute_Success_BooleanType() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.createBooleanSetting();
            Setting expectedSetting = SettingFixture.createBooleanSetting();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(expectedSetting);
            given(settingAssembler.toCreateResponse(any(Setting.class)))
                .willReturn(new CreateSettingUseCase.Response(
                    expectedSetting.getIdValue(),
                    expectedSetting.getKeyValue(),
                    expectedSetting.getDisplayValue(),
                    expectedSetting.getValueType().name(),
                    expectedSetting.getLevel().name(),
                    expectedSetting.getContextId(),
                    expectedSetting.isSecret(),
                    expectedSetting.getCreatedAt(),
                    expectedSetting.getUpdatedAt()
                ));

            // When
            CreateSettingUseCase.Response response = createSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.valueType()).isEqualTo("BOOLEAN");

            verify(saveSettingPort).save(any(Setting.class));
        }

        @Test
        @DisplayName("JSON_OBJECT 타입 설정 생성 성공")
        void execute_Success_JsonObjectType() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.createJsonObjectSetting();
            Setting expectedSetting = SettingFixture.createJsonObjectSetting();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(expectedSetting);
            given(settingAssembler.toCreateResponse(any(Setting.class)))
                .willReturn(new CreateSettingUseCase.Response(
                    expectedSetting.getIdValue(),
                    expectedSetting.getKeyValue(),
                    expectedSetting.getDisplayValue(),
                    expectedSetting.getValueType().name(),
                    expectedSetting.getLevel().name(),
                    expectedSetting.getContextId(),
                    expectedSetting.isSecret(),
                    expectedSetting.getCreatedAt(),
                    expectedSetting.getUpdatedAt()
                ));

            // When
            CreateSettingUseCase.Response response = createSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.valueType()).isEqualTo("JSON_OBJECT");

            verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort).save(any(Setting.class));
        }
    }

    @Nested
    @DisplayName("Secret Key Handling - 비밀 키 처리")
    class SecretKeyHandlingTests {

        @Test
        @DisplayName("secret=true 명시 시 비밀 키로 생성")
        void execute_Success_ExplicitSecret() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.createSecretSetting();
            Setting expectedSetting = SettingFixture.createSecretSetting();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(expectedSetting);
            given(settingAssembler.toCreateResponse(any(Setting.class)))
                .willReturn(new CreateSettingUseCase.Response(
                    expectedSetting.getIdValue(),
                    expectedSetting.getKeyValue(),
                    expectedSetting.getDisplayValue(),
                    expectedSetting.getValueType().name(),
                    expectedSetting.getLevel().name(),
                    expectedSetting.getContextId(),
                    expectedSetting.isSecret(),
                    expectedSetting.getCreatedAt(),
                    expectedSetting.getUpdatedAt()
                ));

            // When
            CreateSettingUseCase.Response response = createSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.secret()).isTrue();
            assertThat(response.value()).isEqualTo("********");

            verify(saveSettingPort).save(any(Setting.class));
        }

        @Test
        @DisplayName("'password' 패턴 키는 자동으로 비밀 키로 처리")
        void execute_Success_AutoDetectSecretByPasswordPattern() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.create(
                "db.password",
                "secret-password",
                "DEFAULT",
                null,
                "STRING",
                false  // secret=false이지만 키 패턴으로 자동 감지됨
            );
            Setting expectedSetting = SettingFixture.builder()
                .key("db.password")
                .value("secret-password")
                .secret(true)
                .build();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(expectedSetting);
            given(settingAssembler.toCreateResponse(any(Setting.class)))
                .willReturn(new CreateSettingUseCase.Response(
                    expectedSetting.getIdValue(),
                    expectedSetting.getKeyValue(),
                    expectedSetting.getDisplayValue(),
                    expectedSetting.getValueType().name(),
                    expectedSetting.getLevel().name(),
                    expectedSetting.getContextId(),
                    expectedSetting.isSecret(),
                    expectedSetting.getCreatedAt(),
                    expectedSetting.getUpdatedAt()
                ));

            // When
            CreateSettingUseCase.Response response = createSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.secret()).isTrue();
            assertThat(response.value()).isEqualTo("********");
        }
    }

    @Nested
    @DisplayName("Duplication Check - 중복 검증")
    class DuplicationCheckTests {

        @Test
        @DisplayName("동일한 (key, level, contextId) 조합이 이미 존재하면 예외 발생")
        void execute_ThrowsException_WhenDuplicateKeyExists() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.createDefaultLevel();
            Setting existingSetting = SettingFixture.createDefaultLevel();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.of(existingSetting));

            // When & Then
            assertThatThrownBy(() -> createSettingService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 존재하는 설정입니다");

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any());
            verify(saveSettingPort, never()).save(any(Setting.class));
        }

        @Test
        @DisplayName("동일한 key이지만 다른 level이면 생성 가능")
        void execute_Success_WhenSameKeyButDifferentLevel() {
            // Given
            String sameKey = "app.config";
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.create(
                sameKey,
                "tenant-value",
                "TENANT",
                100L,
                "STRING",
                false
            );
            Setting expectedSetting = SettingFixture.builder()
                .key(sameKey)
                .value("tenant-value")
                .level(SettingLevel.TENANT)
                .contextId(100L)
                .build();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(expectedSetting);
            given(settingAssembler.toCreateResponse(any(Setting.class)))
                .willReturn(new CreateSettingUseCase.Response(
                    expectedSetting.getIdValue(),
                    expectedSetting.getKeyValue(),
                    expectedSetting.getDisplayValue(),
                    expectedSetting.getValueType().name(),
                    expectedSetting.getLevel().name(),
                    expectedSetting.getContextId(),
                    expectedSetting.isSecret(),
                    expectedSetting.getCreatedAt(),
                    expectedSetting.getUpdatedAt()
                ));

            // When
            CreateSettingUseCase.Response response = createSettingService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.key()).isEqualTo(sameKey);
            assertThat(response.level()).isEqualTo("TENANT");

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
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.createNumberSetting();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(false);

            // When & Then
            assertThatThrownBy(() -> createSettingService.execute(command))
                .isInstanceOf(InvalidSettingException.class)
                .hasMessageContaining("설정 값이 타입과 호환되지 않습니다");

            verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort, never()).save(any(Setting.class));
        }

        @Test
        @DisplayName("JSON 타입 스키마 검증 실패 시 예외 발생")
        void execute_ThrowsException_WhenJsonSchemaValidationFails() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.createJsonObjectSetting();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(false);

            // When & Then
            assertThatThrownBy(() -> createSettingService.execute(command))
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
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.create(
                "app.config",
                "value",
                "INVALID_LEVEL",
                null,
                "STRING",
                false
            );

            // When & Then
            assertThatThrownBy(() -> createSettingService.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

            verify(saveSettingPort, never()).save(any(Setting.class));
        }

        @Test
        @DisplayName("valueType이 유효하지 않으면 예외 발생")
        void execute_ThrowsException_WhenInvalidValueType() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.create(
                "app.config",
                "value",
                "DEFAULT",
                null,
                "INVALID_TYPE",
                false
            );

            // When & Then
            assertThatThrownBy(() -> createSettingService.execute(command))
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
            Method executeMethod = CreateSettingService.class.getMethod("execute", CreateSettingUseCase.Command.class);

            // Then
            assertThat(executeMethod.isAnnotationPresent(Transactional.class)
                || CreateSettingService.class.isAnnotationPresent(Transactional.class))
                .isTrue();
        }

        @Test
        @DisplayName("execute() 메서드는 Public이어야 함 (@Transactional은 Public 메서드에만 작동)")
        void executeMethod_ShouldBePublic() throws NoSuchMethodException {
            // Given
            Method executeMethod = CreateSettingService.class.getMethod("execute", CreateSettingUseCase.Command.class);

            // Then
            assertThat(Modifier.isPublic(executeMethod.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Service 클래스는 Final이 아니어야 함 (Spring Proxy 제약)")
        void serviceClass_ShouldNotBeFinal() {
            // Then
            assertThat(Modifier.isFinal(CreateSettingService.class.getModifiers())).isFalse();
        }
    }

    @Nested
    @DisplayName("Port Interaction - Port 상호작용")
    class PortInteractionTests {

        @Test
        @DisplayName("Port 호출 순서: LoadSettingsPort → SchemaValidator → SaveSettingPort → Assembler")
        void execute_CallsPortsInCorrectOrder() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.create();
            Setting expectedSetting = SettingFixture.createNew();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(true);
            given(saveSettingPort.save(any(Setting.class)))
                .willReturn(expectedSetting);
            given(settingAssembler.toCreateResponse(any(Setting.class)))
                .willReturn(new CreateSettingUseCase.Response(
                    expectedSetting.getIdValue(),
                    expectedSetting.getKeyValue(),
                    expectedSetting.getDisplayValue(),
                    expectedSetting.getValueType().name(),
                    expectedSetting.getLevel().name(),
                    expectedSetting.getContextId(),
                    expectedSetting.isSecret(),
                    expectedSetting.getCreatedAt(),
                    expectedSetting.getUpdatedAt()
                ));

            // When
            createSettingService.execute(command);

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
            inOrder.verify(settingAssembler).toCreateResponse(any(Setting.class));
        }

        @Test
        @DisplayName("중복 검증 실패 시 SchemaValidator와 SaveSettingPort는 호출되지 않음")
        void execute_DoesNotCallOtherPorts_WhenDuplicateCheckFails() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.create();
            Setting existingSetting = SettingFixture.createNew();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.of(existingSetting));

            // When & Then
            assertThatThrownBy(() -> createSettingService.execute(command))
                .isInstanceOf(IllegalStateException.class);

            verify(loadSettingsPort).findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any());
            verify(schemaValidator, never()).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort, never()).save(any(Setting.class));
            verify(settingAssembler, never()).toCreateResponse(any(Setting.class));
        }

        @Test
        @DisplayName("스키마 검증 실패 시 SaveSettingPort는 호출되지 않음")
        void execute_DoesNotCallSavePort_WhenSchemaValidationFails() {
            // Given
            CreateSettingUseCase.Command command = CreateSettingCommandFixture.create();

            given(loadSettingsPort.findByKeyAndLevel(any(SettingKey.class), any(SettingLevel.class), any()))
                .willReturn(Optional.empty());
            given(schemaValidator.validate(any(String.class), any(SettingType.class)))
                .willReturn(false);

            // When & Then
            assertThatThrownBy(() -> createSettingService.execute(command))
                .isInstanceOf(InvalidSettingException.class);

            verify(schemaValidator).validate(any(String.class), any(SettingType.class));
            verify(saveSettingPort, never()).save(any(Setting.class));
            verify(settingAssembler, never()).toCreateResponse(any(Setting.class));
        }
    }
}
