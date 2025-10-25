package com.ryuqq.fileflow.domain.settings;

import com.ryuqq.fileflow.fixtures.SettingFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Setting Aggregate Root 비즈니스 로직 테스트
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("Setting 테스트")
class SettingTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("DEFAULT 레벨 Setting을 생성할 수 있다")
        void createDefaultLevelSetting() {
            // given
            SettingKey key = SettingKey.of("MAX_UPLOAD_SIZE");
            SettingValue value = SettingValue.of("100MB", SettingType.STRING);

            // when
            Setting setting = Setting.of(null, key, value, SettingLevel.DEFAULT, null);

            // then
            assertThat(setting.getId()).isNull();
            assertThat(setting.getKeyValue()).isEqualTo("MAX_UPLOAD_SIZE");
            assertThat(setting.getDisplayValue()).isEqualTo("100MB");
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.DEFAULT);
            assertThat(setting.getContextId()).isNull();
            assertThat(setting.isSecret()).isFalse();
        }

        @Test
        @DisplayName("ORG 레벨 Setting을 생성할 수 있다")
        void createOrgLevelSetting() {
            // given
            SettingKey key = SettingKey.of("MAX_UPLOAD_SIZE");
            SettingValue value = SettingValue.of("200MB", SettingType.STRING);
            Long orgId = 1L;

            // when
            Setting setting = Setting.of(null, key, value, SettingLevel.ORG, orgId);

            // then
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.ORG);
            assertThat(setting.getContextId()).isEqualTo(orgId);
        }

        @Test
        @DisplayName("TENANT 레벨 Setting을 생성할 수 있다")
        void createTenantLevelSetting() {
            // given
            SettingKey key = SettingKey.of("MAX_UPLOAD_SIZE");
            SettingValue value = SettingValue.of("50MB", SettingType.STRING);
            Long tenantId = 100L;

            // when
            Setting setting = Setting.of(null, key, value, SettingLevel.TENANT, tenantId);

            // then
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.TENANT);
            assertThat(setting.getContextId()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("비밀 Setting을 생성할 수 있다")
        void createSecretSetting() {
            // given
            SettingKey key = SettingKey.of("API_KEY");
            SettingValue value = SettingValue.secret("secret-key-123", SettingType.STRING);

            // when
            Setting setting = Setting.of(null, key, value, SettingLevel.DEFAULT, null);

            // then
            assertThat(setting.isSecret()).isTrue();
            assertThat(setting.getDisplayValue()).isEqualTo("********");
            assertThat(setting.getRawValue()).isEqualTo("secret-key-123");
        }
    }

    @Nested
    @DisplayName("Validation 테스트")
    class ValidationTest {

        @Test
        @DisplayName("DEFAULT 레벨은 contextId가 null이어야 한다")
        void defaultLevelMustHaveNullContextId() {
            // given
            SettingKey key = SettingKey.of("MAX_UPLOAD_SIZE");
            SettingValue value = SettingValue.of("100MB", SettingType.STRING);

            // when & then
            assertThatThrownBy(() ->
                Setting.of(null, key, value, SettingLevel.DEFAULT, 1L)
            ).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("DEFAULT");
        }

        @Test
        @DisplayName("ORG 레벨은 contextId가 필수다")
        void orgLevelMustHaveContextId() {
            // given
            SettingKey key = SettingKey.of("MAX_UPLOAD_SIZE");
            SettingValue value = SettingValue.of("200MB", SettingType.STRING);

            // when & then
            assertThatThrownBy(() ->
                Setting.of(null, key, value, SettingLevel.ORG, null)
            ).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("ORG");
        }

        @Test
        @DisplayName("TENANT 레벨은 contextId가 필수다")
        void tenantLevelMustHaveContextId() {
            // given
            SettingKey key = SettingKey.of("MAX_UPLOAD_SIZE");
            SettingValue value = SettingValue.of("50MB", SettingType.STRING);

            // when & then
            assertThatThrownBy(() ->
                Setting.of(null, key, value, SettingLevel.TENANT, null)
            ).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("TENANT");
        }
    }

    @Nested
    @DisplayName("값 수정 테스트")
    class UpdateValueTest {

        @Test
        @DisplayName("Setting 값을 수정할 수 있다")
        void updateValue() {
            // given
            Setting setting = SettingFixtures.createDefaultSetting(); // MAX_UPLOAD_SIZE = 100MB

            // when
            SettingValue newValue = SettingValue.of("200MB", SettingType.STRING);
            setting.updateValue(newValue);

            // then
            assertThat(setting.getDisplayValue()).isEqualTo("200MB");
        }

        @Test
        @DisplayName("비밀 Setting 값을 수정할 수 있다")
        void updateSecretValue() {
            // given
            Setting setting = SettingFixtures.createDefaultSecretSetting(); // API_KEY = secret-key-123

            // when
            SettingValue newValue = SettingValue.secret("new-secret-456", SettingType.STRING);
            setting.updateValue(newValue);

            // then
            assertThat(setting.isSecret()).isTrue();
            assertThat(setting.getDisplayValue()).isEqualTo("********");
            assertThat(setting.getRawValue()).isEqualTo("new-secret-456");
        }

        @Test
        @DisplayName("값 수정 시 updatedAt이 갱신된다")
        void updateValueUpdatesTimestamp() throws InterruptedException {
            // given
            Setting setting = SettingFixtures.reconstituteDefaultSetting(1L);
            var originalUpdatedAt = setting.getUpdatedAt();

            // when
            Thread.sleep(10); // 시간 차이 보장
            SettingValue newValue = SettingValue.of("200MB", SettingType.STRING);
            setting.updateValue(newValue);

            // then
            assertThat(setting.getUpdatedAt()).isAfter(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTest {

        @Test
        @DisplayName("getValueType() 메서드로 타입을 조회할 수 있다")
        void getValueType() {
            // given
            Setting setting = SettingFixtures.createDefaultNumberSetting();

            // when
            SettingType type = setting.getValueType();

            // then
            assertThat(type).isEqualTo(SettingType.NUMBER);
        }

        @Test
        @DisplayName("isSecret() 메서드로 비밀 여부를 조회할 수 있다")
        void isSecret() {
            // given
            Setting secretSetting = SettingFixtures.createDefaultSecretSetting();
            Setting normalSetting = SettingFixtures.createDefaultSetting();

            // when & then
            assertThat(secretSetting.isSecret()).isTrue();
            assertThat(normalSetting.isSecret()).isFalse();
        }
    }

    @Nested
    @DisplayName("재구성 테스트")
    class ReconstituteTest {

        @Test
        @DisplayName("ID를 포함하여 Setting을 재구성할 수 있다")
        void reconstructWithId() {
            // given & when
            Setting setting = SettingFixtures.reconstituteDefaultSetting(1L);

            // then
            assertThat(setting.getId()).isEqualTo(1L);
            assertThat(setting.getKeyValue()).isEqualTo("MAX_UPLOAD_SIZE");
            assertThat(setting.getDisplayValue()).isEqualTo("100MB");
            assertThat(setting.getCreatedAt()).isNotNull();
            assertThat(setting.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("ORG Setting을 ID와 함께 재구성할 수 있다")
        void reconstructOrgSetting() {
            // given & when
            Setting setting = SettingFixtures.reconstituteOrgSetting(10L, 1L);

            // then
            assertThat(setting.getId()).isEqualTo(10L);
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.ORG);
            assertThat(setting.getContextId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("TENANT Setting을 ID와 함께 재구성할 수 있다")
        void reconstructTenantSetting() {
            // given & when
            Setting setting = SettingFixtures.reconstituteTenantSetting(20L, 100L);

            // then
            assertThat(setting.getId()).isEqualTo(20L);
            assertThat(setting.getLevel()).isEqualTo(SettingLevel.TENANT);
            assertThat(setting.getContextId()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("다양한 타입 테스트")
    class VariousTypesTest {

        @Test
        @DisplayName("STRING 타입 Setting을 생성할 수 있다")
        void createStringSetting() {
            // given & when
            Setting setting = SettingFixtures.createDefaultSetting();

            // then
            assertThat(setting.getValueType()).isEqualTo(SettingType.STRING);
            assertThat(setting.getDisplayValue()).isEqualTo("100MB");
        }

        @Test
        @DisplayName("NUMBER 타입 Setting을 생성할 수 있다")
        void createNumberSetting() {
            // given & when
            Setting setting = SettingFixtures.createDefaultNumberSetting();

            // then
            assertThat(setting.getValueType()).isEqualTo(SettingType.NUMBER);
            assertThat(setting.getDisplayValue()).isEqualTo("30");
        }

        @Test
        @DisplayName("BOOLEAN 타입 Setting을 생성할 수 있다")
        void createBooleanSetting() {
            // given & when
            Setting setting = SettingFixtures.createDefaultBooleanSetting();

            // then
            assertThat(setting.getValueType()).isEqualTo(SettingType.BOOLEAN);
            assertThat(setting.getDisplayValue()).isEqualTo("true");
        }

        @Test
        @DisplayName("JSON 타입 Setting을 생성할 수 있다")
        void createJsonSetting() {
            // given & when
            Setting setting = SettingFixtures.createDefaultJsonSetting();

            // then
            assertThat(setting.getValueType()).isEqualTo(SettingType.JSON_OBJECT);
            assertThat(setting.getDisplayValue()).contains("host", "localhost");
        }
    }
}
