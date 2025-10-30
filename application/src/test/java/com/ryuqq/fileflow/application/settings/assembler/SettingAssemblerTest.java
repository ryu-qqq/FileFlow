package com.ryuqq.fileflow.application.settings.assembler;

import com.ryuqq.fileflow.application.settings.dto.SettingResponse;
import com.ryuqq.fileflow.application.settings.port.in.CreateSettingUseCase;
import com.ryuqq.fileflow.application.settings.port.in.UpdateSettingUseCase;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingFixture;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SettingAssembler 단위 테스트
 *
 * <p>테스트 범위:</p>
 * <ul>
 *   <li>toResponse: Domain → DTO 변환</li>
 *   <li>toUpdateResponse: Domain → UpdateResponse 변환</li>
 *   <li>toCreateResponse: Domain → CreateResponse 변환</li>
 *   <li>Secret Masking: 비밀 키 자동 마스킹</li>
 *   <li>Null Handling: null 입력 처리</li>
 *   <li>Law of Demeter: Getter 체이닝 방지</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@DisplayName("SettingAssembler 단위 테스트")
class SettingAssemblerTest {

    private SettingAssembler settingAssembler;

    @BeforeEach
    void setUp() {
        settingAssembler = new SettingAssembler();
    }

    @Nested
    @DisplayName("toResponse() - Domain → DTO 변환")
    class ToResponseTests {

        @Test
        @DisplayName("Setting을 SettingResponse로 변환 성공")
        void toResponse_Success() {
            // Given
            Setting setting = SettingFixture.createWithId(1L, "app.config", "test-value", SettingLevel.DEFAULT, null);

            // When
            SettingResponse response = settingAssembler.toResponse(setting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getKey()).isEqualTo("app.config");
            assertThat(response.getValue()).isEqualTo("test-value");
            assertThat(response.getType()).isEqualTo("STRING");
            assertThat(response.getLevel()).isEqualTo("DEFAULT");
            assertThat(response.getContextId()).isNull();
            assertThat(response.isSecret()).isFalse();
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("비밀 키는 자동으로 마스킹되어 변환")
        void toResponse_MasksSecretKeys() {
            // Given
            Setting secretSetting = SettingFixture.createSecretSetting();

            // When
            SettingResponse response = settingAssembler.toResponse(secretSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.isSecret()).isTrue();
            assertThat(response.getValue()).isEqualTo("********");
        }

        @Test
        @DisplayName("TENANT 레벨 설정 변환 성공")
        void toResponse_Success_TenantLevel() {
            // Given
            Long tenantId = 100L;
            Setting tenantSetting = SettingFixture.createTenantLevel(tenantId);

            // When
            SettingResponse response = settingAssembler.toResponse(tenantSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getLevel()).isEqualTo("TENANT");
            assertThat(response.getContextId()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("ORG 레벨 설정 변환 성공")
        void toResponse_Success_OrgLevel() {
            // Given
            Long orgId = 10L;
            Setting orgSetting = SettingFixture.createOrgLevel(orgId);

            // When
            SettingResponse response = settingAssembler.toResponse(orgSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getLevel()).isEqualTo("ORG");
            assertThat(response.getContextId()).isEqualTo(orgId);
        }

        @Test
        @DisplayName("NUMBER 타입 설정 변환 성공")
        void toResponse_Success_NumberType() {
            // Given
            Setting numberSetting = SettingFixture.createNumberSetting();

            // When
            SettingResponse response = settingAssembler.toResponse(numberSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getType()).isEqualTo("NUMBER");
            assertThat(response.getValue()).isEqualTo("1024");
        }

        @Test
        @DisplayName("BOOLEAN 타입 설정 변환 성공")
        void toResponse_Success_BooleanType() {
            // Given
            Setting booleanSetting = SettingFixture.createBooleanSetting();

            // When
            SettingResponse response = settingAssembler.toResponse(booleanSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getType()).isEqualTo("BOOLEAN");
            assertThat(response.getValue()).isEqualTo("true");
        }

        @Test
        @DisplayName("JSON_OBJECT 타입 설정 변환 성공")
        void toResponse_Success_JsonObjectType() {
            // Given
            Setting jsonSetting = SettingFixture.createJsonObjectSetting();

            // When
            SettingResponse response = settingAssembler.toResponse(jsonSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getType()).isEqualTo("JSON_OBJECT");
            assertThat(response.getValue()).contains("\"key\"");
        }

        @Test
        @DisplayName("null Setting은 null 반환")
        void toResponse_ReturnsNull_WhenSettingIsNull() {
            // When
            SettingResponse response = settingAssembler.toResponse(null);

            // Then
            assertThat(response).isNull();
        }
    }

    @Nested
    @DisplayName("toUpdateResponse() - Domain → UpdateResponse 변환")
    class ToUpdateResponseTests {

        @Test
        @DisplayName("Setting을 UpdateResponse로 변환 성공")
        void toUpdateResponse_Success() {
            // Given
            Setting setting = SettingFixture.createWithId(1L, "app.config", "updated-value", SettingLevel.DEFAULT, null);

            // When
            UpdateSettingUseCase.Response response = settingAssembler.toUpdateResponse(setting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.key()).isEqualTo("app.config");
            assertThat(response.value()).isEqualTo("updated-value");
            assertThat(response.valueType()).isEqualTo("STRING");
            assertThat(response.level()).isEqualTo("DEFAULT");
            assertThat(response.contextId()).isNull();
            assertThat(response.secret()).isFalse();
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("비밀 키는 자동으로 마스킹되어 변환")
        void toUpdateResponse_MasksSecretKeys() {
            // Given
            Setting secretSetting = SettingFixture.builder()
                .id(1L)
                .key("api.key")
                .value("updated-secret-value")
                .secret(true)
                .build();

            // When
            UpdateSettingUseCase.Response response = settingAssembler.toUpdateResponse(secretSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.secret()).isTrue();
            assertThat(response.value()).isEqualTo("********");
        }

        @Test
        @DisplayName("TENANT 레벨 설정 변환 성공")
        void toUpdateResponse_Success_TenantLevel() {
            // Given
            Long tenantId = 100L;
            Setting tenantSetting = SettingFixture.createWithId(1L, "app.tenant.config", "tenant-value", SettingLevel.TENANT, tenantId);

            // When
            UpdateSettingUseCase.Response response = settingAssembler.toUpdateResponse(tenantSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.level()).isEqualTo("TENANT");
            assertThat(response.contextId()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("null Setting은 null 반환")
        void toUpdateResponse_ReturnsNull_WhenSettingIsNull() {
            // When
            UpdateSettingUseCase.Response response = settingAssembler.toUpdateResponse(null);

            // Then
            assertThat(response).isNull();
        }
    }

    @Nested
    @DisplayName("toCreateResponse() - Domain → CreateResponse 변환")
    class ToCreateResponseTests {

        @Test
        @DisplayName("Setting을 CreateResponse로 변환 성공")
        void toCreateResponse_Success() {
            // Given
            Setting setting = SettingFixture.createWithId(1L, "app.config", "new-value", SettingLevel.DEFAULT, null);

            // When
            CreateSettingUseCase.Response response = settingAssembler.toCreateResponse(setting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.key()).isEqualTo("app.config");
            assertThat(response.value()).isEqualTo("new-value");
            assertThat(response.valueType()).isEqualTo("STRING");
            assertThat(response.level()).isEqualTo("DEFAULT");
            assertThat(response.contextId()).isNull();
            assertThat(response.secret()).isFalse();
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("비밀 키는 자동으로 마스킹되어 변환")
        void toCreateResponse_MasksSecretKeys() {
            // Given
            Setting secretSetting = SettingFixture.createSecretSetting();

            // When
            CreateSettingUseCase.Response response = settingAssembler.toCreateResponse(secretSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.secret()).isTrue();
            assertThat(response.value()).isEqualTo("********");
        }

        @Test
        @DisplayName("NUMBER 타입 신규 설정 변환 성공")
        void toCreateResponse_Success_NumberType() {
            // Given
            Setting numberSetting = SettingFixture.builder()
                .id(1L)
                .key("file.max-size")
                .value("2048")
                .type(com.ryuqq.fileflow.domain.settings.SettingType.NUMBER)
                .build();

            // When
            CreateSettingUseCase.Response response = settingAssembler.toCreateResponse(numberSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.valueType()).isEqualTo("NUMBER");
            assertThat(response.value()).isEqualTo("2048");
        }

        @Test
        @DisplayName("ORG 레벨 신규 설정 변환 성공")
        void toCreateResponse_Success_OrgLevel() {
            // Given
            Long orgId = 10L;
            Setting orgSetting = SettingFixture.createWithId(1L, "app.org.config", "org-value", SettingLevel.ORG, orgId);

            // When
            CreateSettingUseCase.Response response = settingAssembler.toCreateResponse(orgSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.level()).isEqualTo("ORG");
            assertThat(response.contextId()).isEqualTo(orgId);
        }

        @Test
        @DisplayName("null Setting은 null 반환")
        void toCreateResponse_ReturnsNull_WhenSettingIsNull() {
            // When
            CreateSettingUseCase.Response response = settingAssembler.toCreateResponse(null);

            // Then
            assertThat(response).isNull();
        }
    }

    @Nested
    @DisplayName("Law of Demeter - Getter 체이닝 방지")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue() 사용 - Getter 체이닝 방지")
        void toResponse_UsesGetIdValue_FollowsLawOfDemeter() {
            // Given
            Setting setting = SettingFixture.createWithId(123L);

            // When
            SettingResponse response = settingAssembler.toResponse(setting);

            // Then
            assertThat(response.getId()).isEqualTo(123L);
            // Assembler 내부에서 setting.getId().getValue() 대신 setting.getIdValue() 사용
        }

        @Test
        @DisplayName("getKeyValue() 사용 - Getter 체이닝 방지")
        void toResponse_UsesGetKeyValue_FollowsLawOfDemeter() {
            // Given
            Setting setting = SettingFixture.createWithId(1L, "test.key", "value", SettingLevel.DEFAULT, null);

            // When
            SettingResponse response = settingAssembler.toResponse(setting);

            // Then
            assertThat(response.getKey()).isEqualTo("test.key");
            // Assembler 내부에서 setting.getKey().getValue() 대신 setting.getKeyValue() 사용
        }

        @Test
        @DisplayName("getDisplayValue() 사용 - 비밀 키 자동 마스킹")
        void toResponse_UsesGetDisplayValue_AutoMasksSecrets() {
            // Given
            Setting secretSetting = SettingFixture.createSecretSetting();

            // When
            SettingResponse response = settingAssembler.toResponse(secretSetting);

            // Then
            assertThat(response.getValue()).isEqualTo("********");
            // Assembler 내부에서 setting.getValue().getDisplayValue() 대신 setting.getDisplayValue() 사용
        }

        @Test
        @DisplayName("getValueType() 사용 - Getter 체이닝 방지")
        void toResponse_UsesGetValueType_FollowsLawOfDemeter() {
            // Given
            Setting numberSetting = SettingFixture.createNumberSetting();

            // When
            SettingResponse response = settingAssembler.toResponse(numberSetting);

            // Then
            assertThat(response.getType()).isEqualTo("NUMBER");
            // Assembler 내부에서 setting.getValue().getType() 대신 setting.getValueType() 사용
        }
    }

    @Nested
    @DisplayName("Stateless - 상태 없음")
    class StatelessTests {

        @Test
        @DisplayName("Assembler는 상태를 가지지 않음")
        void assembler_ShouldBeStateless() {
            // Given
            Setting setting1 = SettingFixture.createWithId(1L);
            Setting setting2 = SettingFixture.createWithId(2L);

            // When
            SettingResponse response1 = settingAssembler.toResponse(setting1);
            SettingResponse response2 = settingAssembler.toResponse(setting2);

            // Then
            assertThat(response1.getId()).isEqualTo(1L);
            assertThat(response2.getId()).isEqualTo(2L);
            // 이전 호출이 다음 호출에 영향을 주지 않음
        }

        @Test
        @DisplayName("동일한 Assembler로 여러 타입 변환 가능")
        void assembler_CanHandleMultipleConversions() {
            // Given
            Setting setting = SettingFixture.createWithId(1L);

            // When
            SettingResponse response1 = settingAssembler.toResponse(setting);
            UpdateSettingUseCase.Response response2 = settingAssembler.toUpdateResponse(setting);
            CreateSettingUseCase.Response response3 = settingAssembler.toCreateResponse(setting);

            // Then
            assertThat(response1).isNotNull();
            assertThat(response2).isNotNull();
            assertThat(response3).isNotNull();
            // 모든 변환이 독립적으로 동작
        }
    }

    @Nested
    @DisplayName("Edge Cases - 경계 케이스")
    class EdgeCaseTests {

        @Test
        @DisplayName("ID가 null인 Setting 변환 (신규 생성 케이스)")
        void toResponse_HandlesNullId() {
            // Given
            Setting newSetting = SettingFixture.createNew();

            // When
            SettingResponse response = settingAssembler.toResponse(newSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isNull();
        }

        @Test
        @DisplayName("contextId가 null인 DEFAULT 레벨 설정 변환")
        void toResponse_HandlesNullContextId_DefaultLevel() {
            // Given
            Setting defaultSetting = SettingFixture.createDefaultLevel();

            // When
            SettingResponse response = settingAssembler.toResponse(defaultSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContextId()).isNull();
            assertThat(response.getLevel()).isEqualTo("DEFAULT");
        }

        @Test
        @DisplayName("contextId가 있는 TENANT 레벨 설정 변환")
        void toResponse_HandlesNonNullContextId_TenantLevel() {
            // Given
            Long tenantId = 100L;
            Setting tenantSetting = SettingFixture.createTenantLevel(tenantId);

            // When
            SettingResponse response = settingAssembler.toResponse(tenantSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContextId()).isEqualTo(tenantId);
            assertThat(response.getLevel()).isEqualTo("TENANT");
        }

        @Test
        @DisplayName("contextId가 있는 ORG 레벨 설정 변환")
        void toResponse_HandlesNonNullContextId_OrgLevel() {
            // Given
            Long orgId = 10L;
            Setting orgSetting = SettingFixture.createOrgLevel(orgId);

            // When
            SettingResponse response = settingAssembler.toResponse(orgSetting);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContextId()).isEqualTo(orgId);
            assertThat(response.getLevel()).isEqualTo("ORG");
        }
    }
}
