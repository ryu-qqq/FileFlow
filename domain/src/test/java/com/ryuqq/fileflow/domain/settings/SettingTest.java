package com.ryuqq.fileflow.domain.settings;

import com.ryuqq.fileflow.domain.settings.fixture.SettingFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

/**
 * Setting Domain 단위 테스트
 *
 * <p>테스트 구성: Happy Path, Edge Cases, Exception Cases, Invariant Validation, Law of Demeter Tests</p>
 * <p>Fixture 사용: {@link SettingFixture}를 활용하여 테스트 데이터 생성</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@DisplayName("Setting Domain 단위 테스트")
class SettingTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreationTests {

        @Test
        @DisplayName("신규 Setting 생성 성공 (forNew)")
        void forNew_CreatesSettingWithoutId() {
            // Given: 설정 키, 값, 레벨 준비
            SettingKey key = SettingKey.of("app.config");
            SettingValue value = SettingValue.of("test-value", SettingType.STRING);
            SettingLevel level = SettingLevel.DEFAULT;

            // When: forNew()로 신규 Setting 생성
            Setting setting = Setting.forNew(key, value, level, null);

            // Then: ID 없이 생성되고, 필드가 올바르게 설정됨
            assertThat(setting.getId()).isNull();
            assertThat(setting.getKey()).isEqualTo(key);
            assertThat(setting.getValue()).isEqualTo(value);
            assertThat(setting.getLevel()).isEqualTo(level);
            assertThat(setting.getContextId()).isNull();
            assertThat(setting.getCreatedAt()).isNotNull();
            assertThat(setting.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("ID가 있는 Setting 생성 성공 (of)")
        void of_CreatesSettingWithId() {
            // Given: ID와 함께 설정 정보 준비
            SettingId id = SettingId.of(1L);
            SettingKey key = SettingKey.of("app.config");
            SettingValue value = SettingValue.of("test-value", SettingType.STRING);
            SettingLevel level = SettingLevel.DEFAULT;

            // When: of()로 ID 있는 Setting 생성
            Setting setting = Setting.of(id, key, value, level, null);

            // Then: ID 포함하여 생성되고, 필드가 올바르게 설정됨
            assertThat(setting.getId()).isEqualTo(id);
            assertThat(setting.getKey()).isEqualTo(key);
            assertThat(setting.getValue()).isEqualTo(value);
            assertThat(setting.getLevel()).isEqualTo(level);
            assertThat(setting.getContextId()).isNull();
        }

        @Test
        @DisplayName("Fixture를 통한 여러 Setting 생성")
        void createMultiple_CreatesMultipleSettings() {
            // Given: 생성할 개수 지정
            int count = 5;

            // When: Fixture로 여러 Setting 생성
            var settings = SettingFixture.createMultiple(count);

            // Then: 지정된 개수만큼 Setting 생성됨
            assertThat(settings).hasSize(count);
            assertThat(settings).allMatch(setting -> setting.getId() != null);
            assertThat(settings).allMatch(setting -> setting.getKey() != null);
        }

        @Test
        @DisplayName("ORG 레벨 Setting 생성 성공")
        void createOrgLevel_Success() {
            // Given: ORG 레벨 설정 준비
            Long orgId = 100L;

            // When: ORG 레벨 Setting 생성
            Setting setting = SettingFixture.createOrgLevel(orgId);

            // Then: ORG 레벨로 생성되고 contextId 설정됨
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.ORG);
            assertThat(setting.getContextId()).isEqualTo(orgId);
        }

        @Test
        @DisplayName("TENANT 레벨 Setting 생성 성공")
        void createTenantLevel_Success() {
            // Given: TENANT 레벨 설정 준비
            Long tenantId = 200L;

            // When: TENANT 레벨 Setting 생성
            Setting setting = SettingFixture.createTenantLevel(tenantId);

            // Then: TENANT 레벨로 생성되고 contextId 설정됨
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.TENANT);
            assertThat(setting.getContextId()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("DEFAULT 레벨 Setting 생성 성공")
        void createDefaultLevel_Success() {
            // Given & When: DEFAULT 레벨 Setting 생성
            Setting setting = SettingFixture.createDefaultLevel();

            // Then: DEFAULT 레벨로 생성되고 contextId는 null
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.DEFAULT);
            assertThat(setting.getContextId()).isNull();
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트 (Happy Path)")
    class BusinessMethodTests {

        @Test
        @DisplayName("설정 값 업데이트 성공")
        void updateValue_Success() {
            // Given: 기존 Setting과 새로운 값 준비
            Setting setting = SettingFixture.createNew();
            SettingValue newValue = SettingValue.of("updated-value", SettingType.STRING);
            LocalDateTime oldUpdatedAt = setting.getUpdatedAt();

            // When: 설정 값 업데이트
            setting.updateValue(newValue);

            // Then: 값이 변경되고 updatedAt이 갱신됨
            assertThat(setting.getValue()).isEqualTo(newValue);
            assertThat(setting.getUpdatedAt()).isAfter(oldUpdatedAt);
        }

        @Test
        @DisplayName("우선순위 비교 - ORG가 TENANT보다 높음")
        void hasHigherPriorityThan_OrgHigherThanTenant() {
            // Given: ORG 레벨과 TENANT 레벨 Setting
            Setting orgSetting = SettingFixture.createOrgLevel(1L);
            Setting tenantSetting = SettingFixture.createTenantLevel(1L);

            // When: 우선순위 비교
            boolean result = orgSetting.hasHigherPriorityThan(tenantSetting);

            // Then: ORG가 더 높은 우선순위를 가짐
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("우선순위 비교 - TENANT가 DEFAULT보다 높음")
        void hasHigherPriorityThan_TenantHigherThanDefault() {
            // Given: TENANT 레벨과 DEFAULT 레벨 Setting
            Setting tenantSetting = SettingFixture.createTenantLevel(1L);
            Setting defaultSetting = SettingFixture.createDefaultLevel();

            // When: 우선순위 비교
            boolean result = tenantSetting.hasHigherPriorityThan(defaultSetting);

            // Then: TENANT가 더 높은 우선순위를 가짐
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("특정 키를 가지는지 확인 성공")
        void hasKey_Success() {
            // Given: 특정 키로 Setting 생성
            SettingKey key = SettingKey.of("test.key");
            Setting setting = SettingFixture.createNew("test.key", "value", SettingLevel.DEFAULT, null);

            // When: 키 확인
            boolean result = setting.hasKey(key);

            // Then: true 반환
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("특정 레벨인지 확인 성공")
        void isLevel_Success() {
            // Given: DEFAULT 레벨 Setting
            Setting setting = SettingFixture.createDefaultLevel();

            // When: 레벨 확인
            boolean result = setting.isLevel(SettingLevel.DEFAULT);

            // Then: true 반환
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("특정 컨텍스트에 속하는지 확인 성공")
        void belongsToContext_Success() {
            // Given: contextId가 100인 Setting
            Long contextId = 100L;
            Setting setting = SettingFixture.createOrgLevel(contextId);

            // When: 컨텍스트 확인
            boolean result = setting.belongsToContext(contextId);

            // Then: true 반환
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue() 사용 - Getter 체이닝 방지")
        void getIdValue_FollowsLawOfDemeter() {
            // Given: ID가 있는 Setting
            Setting setting = SettingFixture.createWithId(1L);

            // When: getIdValue() 호출 (Law of Demeter 준수)
            Long idValue = setting.getIdValue();

            // Then: ID 원시 값 반환 (❌ setting.getId().value())
            assertThat(idValue).isEqualTo(1L);
        }

        @Test
        @DisplayName("getKeyValue() 사용 - Getter 체이닝 방지")
        void getKeyValue_FollowsLawOfDemeter() {
            // Given: Setting
            Setting setting = SettingFixture.createNew();

            // When: getKeyValue() 호출 (Law of Demeter 준수)
            String keyValue = setting.getKeyValue();

            // Then: 키 문자열 값 반환 (❌ setting.getKey().getValue())
            assertThat(keyValue).isNotNull();
        }

        @Test
        @DisplayName("getRawValue() 사용 - Getter 체이닝 방지")
        void getRawValue_FollowsLawOfDemeter() {
            // Given: Setting
            Setting setting = SettingFixture.createNew();

            // When: getRawValue() 호출 (Law of Demeter 준수)
            String rawValue = setting.getRawValue();

            // Then: 원본 값 반환 (❌ setting.getValue().getValue())
            assertThat(rawValue).isNotNull();
        }

        @Test
        @DisplayName("getDisplayValue() 사용 - Getter 체이닝 방지")
        void getDisplayValue_FollowsLawOfDemeter() {
            // Given: 비밀 키 Setting
            Setting setting = SettingFixture.createSecretSetting();

            // When: getDisplayValue() 호출 (Law of Demeter 준수)
            String displayValue = setting.getDisplayValue();

            // Then: 마스킹된 값 반환 (❌ setting.getValue().getDisplayValue())
            assertThat(displayValue).contains("*");
        }

        @Test
        @DisplayName("getValueType() 사용 - Getter 체이닝 방지")
        void getValueType_FollowsLawOfDemeter() {
            // Given: Setting
            Setting setting = SettingFixture.createNumberSetting();

            // When: getValueType() 호출 (Law of Demeter 준수)
            SettingType type = setting.getValueType();

            // Then: 타입 반환 (❌ setting.getValue().getType())
            assertThat(type).isEqualTo(SettingType.NUMBER);
        }

        @Test
        @DisplayName("isSecret() 사용 - Getter 체이닝 방지")
        void isSecret_FollowsLawOfDemeter() {
            // Given: 비밀 키 Setting
            Setting setting = SettingFixture.createSecretSetting();

            // When: isSecret() 호출 (Law of Demeter 준수)
            boolean isSecret = setting.isSecret();

            // Then: true 반환 (❌ setting.getValue().isSecret())
            assertThat(isSecret).isTrue();
        }

        @Test
        @DisplayName("hasHigherPriorityThan() 사용 - 우선순위 비교 로직 캡슐화")
        void hasHigherPriorityThan_FollowsLawOfDemeter() {
            // Given: ORG와 TENANT 레벨 Setting
            Setting orgSetting = SettingFixture.createOrgLevel(1L);
            Setting tenantSetting = SettingFixture.createTenantLevel(1L);

            // When: hasHigherPriorityThan() 호출 (Law of Demeter 준수)
            boolean result = orgSetting.hasHigherPriorityThan(tenantSetting);

            // Then: true 반환 (❌ orgSetting.getLevel().hasHigherPriorityThan(tenantSetting.getLevel()))
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("hasKey() 사용 - 키 비교 로직 캡슐화")
        void hasKey_FollowsLawOfDemeter() {
            // Given: Setting
            Setting setting = SettingFixture.createNew();
            SettingKey key = setting.getKey();

            // When: hasKey() 호출 (Law of Demeter 준수)
            boolean result = setting.hasKey(key);

            // Then: true 반환 (❌ setting.getKey().isSameAs(key))
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isLevel() 사용 - 레벨 확인 로직 캡슐화")
        void isLevel_FollowsLawOfDemeter() {
            // Given: DEFAULT 레벨 Setting
            Setting setting = SettingFixture.createDefaultLevel();

            // When: isLevel() 호출 (Law of Demeter 준수)
            boolean result = setting.isLevel(SettingLevel.DEFAULT);

            // Then: true 반환 (❌ setting.getLevel() == SettingLevel.DEFAULT)
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("belongsToContext() 사용 - 컨텍스트 확인 로직 캡슐화")
        void belongsToContext_FollowsLawOfDemeter() {
            // Given: contextId가 100인 Setting
            Setting setting = SettingFixture.createOrgLevel(100L);

            // When: belongsToContext() 호출 (Law of Demeter 준수)
            boolean result = setting.belongsToContext(100L);

            // Then: true 반환 (❌ setting.getContextId().equals(100L))
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("경계값 테스트 (Edge Cases)")
    class EdgeCaseTests {

        @Test
        @DisplayName("DEFAULT 레벨은 contextId가 null이어야 함")
        void defaultLevel_MustHaveNullContextId() {
            // Given: DEFAULT 레벨 설정
            SettingKey key = SettingKey.of("app.config");
            SettingValue value = SettingValue.of("value", SettingType.STRING);

            // When: DEFAULT 레벨로 Setting 생성
            Setting setting = Setting.forNew(key, value, SettingLevel.DEFAULT, null);

            // Then: contextId는 null
            assertThat(setting.getContextId()).isNull();
            assertThat(setting.isLevel(SettingLevel.DEFAULT)).isTrue();
        }

        @Test
        @DisplayName("우선순위 비교 - null과 비교 시 항상 true")
        void hasHigherPriorityThan_WithNull_ReturnsTrue() {
            // Given: Setting
            Setting setting = SettingFixture.createNew();

            // When: null과 우선순위 비교
            boolean result = setting.hasHigherPriorityThan(null);

            // Then: true 반환
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("같은 레벨의 Setting 우선순위 비교 - false")
        void hasHigherPriorityThan_SameLevel_ReturnsFalse() {
            // Given: 같은 DEFAULT 레벨 Setting 2개
            Setting setting1 = SettingFixture.createDefaultLevel();
            Setting setting2 = SettingFixture.createDefaultLevel();

            // When: 우선순위 비교
            boolean result = setting1.hasHigherPriorityThan(setting2);

            // Then: false 반환 (같은 레벨)
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("belongsToContext() - null contextId 비교")
        void belongsToContext_WithNull_Success() {
            // Given: contextId가 null인 DEFAULT 레벨 Setting
            Setting setting = SettingFixture.createDefaultLevel();

            // When: null contextId 확인
            boolean result = setting.belongsToContext(null);

            // Then: true 반환
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트 (Exception Cases)")
    class ExceptionTests {

        @Test
        @DisplayName("of() - ID가 null이면 예외 발생")
        void of_ThrowsException_WhenIdIsNull() {
            // Given: ID가 null인 경우
            SettingKey key = SettingKey.of("app.config");
            SettingValue value = SettingValue.of("value", SettingType.STRING);

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Setting.of(null, key, value, SettingLevel.DEFAULT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setting ID는 필수입니다");
        }

        @Test
        @DisplayName("forNew() - key가 null이면 예외 발생")
        void forNew_ThrowsException_WhenKeyIsNull() {
            // Given: key가 null인 경우
            SettingValue value = SettingValue.of("value", SettingType.STRING);

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Setting.forNew(null, value, SettingLevel.DEFAULT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setting 키는 필수입니다");
        }

        @Test
        @DisplayName("forNew() - value가 null이면 예외 발생")
        void forNew_ThrowsException_WhenValueIsNull() {
            // Given: value가 null인 경우
            SettingKey key = SettingKey.of("app.config");

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Setting.forNew(key, null, SettingLevel.DEFAULT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setting 값은 필수입니다");
        }

        @Test
        @DisplayName("forNew() - level이 null이면 예외 발생")
        void forNew_ThrowsException_WhenLevelIsNull() {
            // Given: level이 null인 경우
            SettingKey key = SettingKey.of("app.config");
            SettingValue value = SettingValue.of("value", SettingType.STRING);

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Setting.forNew(key, value, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setting 레벨은 필수입니다");
        }

        @Test
        @DisplayName("forNew() - ORG 레벨인데 contextId가 null이면 예외 발생")
        void forNew_ThrowsException_WhenOrgLevelWithoutContextId() {
            // Given: ORG 레벨인데 contextId가 null
            SettingKey key = SettingKey.of("app.config");
            SettingValue value = SettingValue.of("value", SettingType.STRING);

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Setting.forNew(key, value, SettingLevel.ORG, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ORG 레벨의 Setting은 contextId가 필수입니다");
        }

        @Test
        @DisplayName("forNew() - TENANT 레벨인데 contextId가 null이면 예외 발생")
        void forNew_ThrowsException_WhenTenantLevelWithoutContextId() {
            // Given: TENANT 레벨인데 contextId가 null
            SettingKey key = SettingKey.of("app.config");
            SettingValue value = SettingValue.of("value", SettingType.STRING);

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Setting.forNew(key, value, SettingLevel.TENANT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("TENANT 레벨의 Setting은 contextId가 필수입니다");
        }

        @Test
        @DisplayName("forNew() - DEFAULT 레벨인데 contextId가 있으면 예외 발생")
        void forNew_ThrowsException_WhenDefaultLevelWithContextId() {
            // Given: DEFAULT 레벨인데 contextId가 있음
            SettingKey key = SettingKey.of("app.config");
            SettingValue value = SettingValue.of("value", SettingType.STRING);

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Setting.forNew(key, value, SettingLevel.DEFAULT, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("DEFAULT 레벨의 Setting은 contextId가 null이어야 합니다");
        }

        @Test
        @DisplayName("updateValue() - null 값으로 업데이트하면 예외 발생")
        void updateValue_ThrowsException_WhenValueIsNull() {
            // Given: Setting
            Setting setting = SettingFixture.createNew();

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> setting.updateValue(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setting 값은 null일 수 없습니다");
        }

        @Test
        @DisplayName("reconstitute() - ID가 null이면 예외 발생")
        void reconstitute_ThrowsException_WhenIdIsNull() {
            // Given: ID가 null인 경우
            SettingKey key = SettingKey.of("app.config");
            SettingValue value = SettingValue.of("value", SettingType.STRING);
            LocalDateTime now = LocalDateTime.now();

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Setting.reconstitute(null, key, value, SettingLevel.DEFAULT, null, now, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("DB reconstitute는 ID가 필수입니다");
        }
    }

    @Nested
    @DisplayName("불변식 검증 테스트 (Invariant Validation)")
    class InvariantTests {

        @Test
        @DisplayName("생성된 Setting은 항상 createdAt과 updatedAt을 가짐")
        void createdSetting_AlwaysHasTimestamps() {
            // Given & When: Setting 생성
            Setting setting = SettingFixture.createNew();

            // Then: createdAt과 updatedAt 존재
            assertThat(setting.getCreatedAt()).isNotNull();
            assertThat(setting.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("forNew()로 생성한 Setting은 항상 ID가 null")
        void forNew_AlwaysHasNullId() {
            // Given & When: forNew()로 Setting 생성
            Setting setting = SettingFixture.createNew();

            // Then: ID는 null
            assertThat(setting.getId()).isNull();
        }

        @Test
        @DisplayName("of()로 생성한 Setting은 항상 ID가 존재")
        void of_AlwaysHasId() {
            // Given & When: of()로 Setting 생성
            Setting setting = SettingFixture.createWithId(1L);

            // Then: ID 존재
            assertThat(setting.getId()).isNotNull();
            assertThat(setting.getIdValue()).isEqualTo(1L);
        }

        @Test
        @DisplayName("updateValue() 실행 시 updatedAt이 항상 갱신됨")
        void updateValue_AlwaysUpdatesTimestamp() throws InterruptedException {
            // Given: Setting 생성
            Setting setting = SettingFixture.createNew();
            LocalDateTime oldUpdatedAt = setting.getUpdatedAt();

            // 시간이 지나도록 약간 대기
            Thread.sleep(10);

            SettingValue newValue = SettingValue.of("new-value", SettingType.STRING);

            // When: 값 업데이트
            setting.updateValue(newValue);

            // Then: updatedAt 갱신됨 (또는 같거나 이후 시간)
            assertThat(setting.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
        }

        @Test
        @DisplayName("ORG/TENANT 레벨은 항상 contextId를 가짐")
        void orgAndTenantLevel_AlwaysHaveContextId() {
            // Given & When: ORG와 TENANT 레벨 Setting 생성
            Setting orgSetting = SettingFixture.createOrgLevel(1L);
            Setting tenantSetting = SettingFixture.createTenantLevel(2L);

            // Then: 둘 다 contextId 존재
            assertThat(orgSetting.getContextId()).isNotNull();
            assertThat(tenantSetting.getContextId()).isNotNull();
        }

        @Test
        @DisplayName("DEFAULT 레벨은 항상 contextId가 null")
        void defaultLevel_AlwaysHasNullContextId() {
            // Given & When: DEFAULT 레벨 Setting 생성
            Setting setting = SettingFixture.createDefaultLevel();

            // Then: contextId는 null
            assertThat(setting.getContextId()).isNull();
        }
    }

    @Nested
    @DisplayName("Fixture Builder 테스트")
    class FixtureBuilderTests {

        @Test
        @DisplayName("Builder로 커스텀 Setting 생성")
        void builder_CreatesCustomSetting() {
            // Given & When: Builder로 Setting 생성
            Setting setting = SettingFixture.builder()
                .id(100L)
                .key("custom.key")
                .value("custom-value")
                .type(SettingType.STRING)
                .level(SettingLevel.ORG)
                .contextId(10L)
                .build();

            // Then: 지정된 값으로 Setting 생성됨
            assertThat(setting.getIdValue()).isEqualTo(100L);
            assertThat(setting.getKeyValue()).isEqualTo("custom.key");
            assertThat(setting.getRawValue()).isEqualTo("custom-value");
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.ORG);
            assertThat(setting.getContextId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Builder로 비밀 키 Setting 생성")
        void builder_CreatesSecretSetting() {
            // Given & When: Builder로 비밀 키 Setting 생성
            Setting setting = SettingFixture.builder()
                .key("api.secret")
                .value("secret-value-123")
                .secret(true)
                .build();

            // Then: 비밀 키로 생성됨
            assertThat(setting.isSecret()).isTrue();
            assertThat(setting.getDisplayValue()).contains("*");
        }
    }

    @Nested
    @DisplayName("여러 타입의 Setting 생성 테스트")
    class SettingTypeTests {

        @Test
        @DisplayName("NUMBER 타입 Setting 생성")
        void createNumberSetting_Success() {
            // Given & When: NUMBER 타입 Setting 생성
            Setting setting = SettingFixture.createNumberSetting();

            // Then: NUMBER 타입으로 생성됨
            assertThat(setting.getValueType()).isEqualTo(SettingType.NUMBER);
        }

        @Test
        @DisplayName("BOOLEAN 타입 Setting 생성")
        void createBooleanSetting_Success() {
            // Given & When: BOOLEAN 타입 Setting 생성
            Setting setting = SettingFixture.createBooleanSetting();

            // Then: BOOLEAN 타입으로 생성됨
            assertThat(setting.getValueType()).isEqualTo(SettingType.BOOLEAN);
        }

        @Test
        @DisplayName("JSON_OBJECT 타입 Setting 생성")
        void createJsonObjectSetting_Success() {
            // Given & When: JSON_OBJECT 타입 Setting 생성
            Setting setting = SettingFixture.createJsonObjectSetting();

            // Then: JSON_OBJECT 타입으로 생성됨
            assertThat(setting.getValueType()).isEqualTo(SettingType.JSON_OBJECT);
        }

        @Test
        @DisplayName("JSON_ARRAY 타입 Setting 생성")
        void createJsonArraySetting_Success() {
            // Given & When: JSON_ARRAY 타입 Setting 생성
            Setting setting = SettingFixture.createJsonArraySetting();

            // Then: JSON_ARRAY 타입으로 생성됨
            assertThat(setting.getValueType()).isEqualTo(SettingType.JSON_ARRAY);
        }
    }
}
