package com.ryuqq.fileflow.domain.settings;

import com.ryuqq.fileflow.fixtures.SettingFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SettingMerger 3레벨 병합 로직 테스트
 *
 * <p>3레벨 우선순위 병합(ORG > TENANT > DEFAULT)의 정확성을 검증합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("SettingMerger 테스트")
class SettingMergerTest {

    private SettingMerger settingMerger;

    @BeforeEach
    void setUp() {
        settingMerger = new SettingMerger();
    }

    @Nested
    @DisplayName("3레벨 병합 테스트")
    class MergeSettingsTest {

        @Test
        @DisplayName("ORG > TENANT > DEFAULT 우선순위로 병합된다")
        void mergeWithAllThreeLevels() {
            // given
            Setting orgSetting = SettingFixtures.createOrgSetting(1L);           // MAX_UPLOAD_SIZE = 200MB
            Setting tenantSetting = SettingFixtures.createTenantSetting(100L);   // MAX_UPLOAD_SIZE = 50MB
            Setting defaultSetting = SettingFixtures.createDefaultSetting();     // MAX_UPLOAD_SIZE = 100MB

            List<Setting> orgSettings = List.of(orgSetting);
            List<Setting> tenantSettings = List.of(tenantSetting);
            List<Setting> defaultSettings = List.of(defaultSetting);

            // when
            Map<String, String> merged = settingMerger.mergeToValueMap(orgSettings, tenantSettings, defaultSettings);

            // then
            assertThat(merged).containsEntry("MAX_UPLOAD_SIZE", "200MB"); // ORG 우선
        }

        @Test
        @DisplayName("ORG가 없으면 TENANT > DEFAULT 우선순위로 병합된다")
        void mergeWithoutOrg() {
            // given
            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of(SettingFixtures.createTenantSetting(100L)); // 50MB
            List<Setting> defaultSettings = List.of(SettingFixtures.createDefaultSetting());    // 100MB

            // when
            Map<String, String> merged = settingMerger.mergeToValueMap(orgSettings, tenantSettings, defaultSettings);

            // then
            assertThat(merged).containsEntry("MAX_UPLOAD_SIZE", "50MB"); // TENANT 우선
        }

        @Test
        @DisplayName("ORG와 TENANT가 없으면 DEFAULT만 반환된다")
        void mergeOnlyDefault() {
            // given
            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of();
            List<Setting> defaultSettings = List.of(SettingFixtures.createDefaultSetting()); // 100MB

            // when
            Map<String, String> merged = settingMerger.mergeToValueMap(orgSettings, tenantSettings, defaultSettings);

            // then
            assertThat(merged).containsEntry("MAX_UPLOAD_SIZE", "100MB"); // DEFAULT만
        }

        @Test
        @DisplayName("모든 레벨이 비어있으면 빈 Map을 반환한다")
        void mergeEmptyLevels() {
            // given
            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of();
            List<Setting> defaultSettings = List.of();

            // when
            Map<String, String> merged = settingMerger.mergeToValueMap(orgSettings, tenantSettings, defaultSettings);

            // then
            assertThat(merged).isEmpty();
        }

        @Test
        @DisplayName("서로 다른 키를 가진 설정들은 모두 병합된다")
        void mergeDifferentKeys() {
            // given
            Setting orgUploadSize = SettingFixtures.createOrgSetting(1L);                     // MAX_UPLOAD_SIZE = 200MB
            Setting tenantTimeout = SettingFixtures.createCustomTenantSetting(
                "API_TIMEOUT", "60", SettingType.NUMBER, 100L
            );                                                                                // API_TIMEOUT = 60
            Setting defaultCache = SettingFixtures.createDefaultBooleanSetting();             // ENABLE_CACHE = true

            List<Setting> orgSettings = List.of(orgUploadSize);
            List<Setting> tenantSettings = List.of(tenantTimeout);
            List<Setting> defaultSettings = List.of(defaultCache);

            // when
            Map<String, String> merged = settingMerger.mergeToValueMap(orgSettings, tenantSettings, defaultSettings);

            // then
            assertThat(merged)
                .containsEntry("MAX_UPLOAD_SIZE", "200MB")
                .containsEntry("API_TIMEOUT", "60")
                .containsEntry("ENABLE_CACHE", "true")
                .hasSize(3);
        }

        @Test
        @DisplayName("같은 키를 가진 설정들은 우선순위에 따라 병합된다")
        void mergeSameKeysWithPriority() {
            // given
            Setting org = SettingFixtures.createCustomOrgSetting("CONFIG", "org-value", SettingType.STRING, 1L);
            Setting tenant = SettingFixtures.createCustomTenantSetting("CONFIG", "tenant-value", SettingType.STRING, 100L);
            Setting defaults = SettingFixtures.createCustomDefaultSetting("CONFIG", "default-value", SettingType.STRING);

            List<Setting> orgSettings = List.of(org);
            List<Setting> tenantSettings = List.of(tenant);
            List<Setting> defaultSettings = List.of(defaults);

            // when
            Map<String, String> merged = settingMerger.mergeToValueMap(orgSettings, tenantSettings, defaultSettings);

            // then
            assertThat(merged)
                .containsEntry("CONFIG", "org-value") // ORG 우선
                .hasSize(1);
        }
    }

    @Nested
    @DisplayName("비밀 키 마스킹 테스트")
    class SecretMaskingTest {

        @Test
        @DisplayName("비밀 설정은 마스킹되어 병합된다")
        void mergeSecretSettings() {
            // given
            Setting defaultSecret = SettingFixtures.createDefaultSecretSetting(); // API_KEY = secret-key-123 (masked)
            Setting defaultNormal = SettingFixtures.createDefaultSetting();       // MAX_UPLOAD_SIZE = 100MB

            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of();
            List<Setting> defaultSettings = List.of(defaultSecret, defaultNormal);

            // when
            Map<String, String> merged = settingMerger.mergeToValueMap(orgSettings, tenantSettings, defaultSettings);

            // then
            assertThat(merged)
                .containsEntry("API_KEY", "********")          // 마스킹됨
                .containsEntry("MAX_UPLOAD_SIZE", "100MB"); // 일반 값
        }

        @Test
        @DisplayName("ORG 레벨의 비밀 설정도 마스킹되어 병합된다")
        void mergeOrgSecretSetting() {
            // given
            Setting orgSecret = SettingFixtures.createOrgSecretSetting(1L); // ORG_API_KEY = org-secret-456 (masked)

            List<Setting> orgSettings = List.of(orgSecret);
            List<Setting> tenantSettings = List.of();
            List<Setting> defaultSettings = List.of();

            // when
            Map<String, String> merged = settingMerger.mergeToValueMap(orgSettings, tenantSettings, defaultSettings);

            // then
            assertThat(merged).containsEntry("ORG_API_KEY", "********");
        }

        @Test
        @DisplayName("TENANT 레벨의 비밀 설정도 마스킹되어 병합된다")
        void mergeTenantSecretSetting() {
            // given
            Setting tenantSecret = SettingFixtures.createTenantSecretSetting(100L); // TENANT_API_KEY = tenant-secret-789 (masked)

            List<Setting> orgSettings = List.of();
            List<Setting> tenantSettings = List.of(tenantSecret);
            List<Setting> defaultSettings = List.of();

            // when
            Map<String, String> merged = settingMerger.mergeToValueMap(orgSettings, tenantSettings, defaultSettings);

            // then
            assertThat(merged).containsEntry("TENANT_API_KEY", "********");
        }
    }

    @Nested
    @DisplayName("복잡한 시나리오 테스트")
    class ComplexScenarioTest {

        @Test
        @DisplayName("다양한 타입의 설정들이 정확히 병합된다")
        void mergeVariousTypes() {
            // given - ORG: MAX_UPLOAD_SIZE override
            Setting orgUpload = SettingFixtures.createOrgSetting(1L); // 200MB

            // given - TENANT: API_TIMEOUT override
            Setting tenantTimeout = SettingFixtures.createCustomTenantSetting(
                "API_TIMEOUT", "60", SettingType.NUMBER, 100L
            );

            // given - DEFAULT: 모든 기본 설정
            Setting defaultUpload = SettingFixtures.createDefaultSetting();          // MAX_UPLOAD_SIZE = 100MB (overridden)
            Setting defaultTimeout = SettingFixtures.createDefaultNumberSetting();   // API_TIMEOUT = 30 (overridden)
            Setting defaultCache = SettingFixtures.createDefaultBooleanSetting();    // ENABLE_CACHE = true
            Setting defaultJson = SettingFixtures.createDefaultJsonSetting();        // DATABASE_CONFIG = {...}

            List<Setting> orgSettings = List.of(orgUpload);
            List<Setting> tenantSettings = List.of(tenantTimeout);
            List<Setting> defaultSettings = List.of(defaultUpload, defaultTimeout, defaultCache, defaultJson);

            // when
            Map<String, String> merged = settingMerger.mergeToValueMap(orgSettings, tenantSettings, defaultSettings);

            // then
            assertThat(merged)
                .containsEntry("MAX_UPLOAD_SIZE", "200MB")                      // ORG override
                .containsEntry("API_TIMEOUT", "60")                             // TENANT override
                .containsEntry("ENABLE_CACHE", "true")                          // DEFAULT only
                .containsEntry("DATABASE_CONFIG", "{\"host\":\"localhost\",\"port\":5432}") // DEFAULT only
                .hasSize(4);
        }

        @Test
        @DisplayName("비밀 키와 일반 키가 혼합된 경우 정확히 병합된다")
        void mergeMixedSecretAndNormal() {
            // given
            Setting orgNormal = SettingFixtures.createOrgSetting(1L);               // MAX_UPLOAD_SIZE = 200MB
            Setting orgSecret = SettingFixtures.createOrgSecretSetting(1L);         // ORG_API_KEY = **** (masked)

            Setting tenantNormal = SettingFixtures.createCustomTenantSetting(
                "FEATURE_FLAG", "enabled", SettingType.STRING, 100L
            );
            Setting tenantSecret = SettingFixtures.createTenantSecretSetting(100L); // TENANT_API_KEY = **** (masked)

            Setting defaultNormal = SettingFixtures.createDefaultBooleanSetting();  // ENABLE_CACHE = true
            Setting defaultSecret = SettingFixtures.createDefaultSecretSetting();   // API_KEY = **** (masked)

            List<Setting> orgSettings = List.of(orgNormal, orgSecret);
            List<Setting> tenantSettings = List.of(tenantNormal, tenantSecret);
            List<Setting> defaultSettings = List.of(defaultNormal, defaultSecret);

            // when
            Map<String, String> merged = settingMerger.mergeToValueMap(orgSettings, tenantSettings, defaultSettings);

            // then
            assertThat(merged)
                .containsEntry("MAX_UPLOAD_SIZE", "200MB")    // 일반
                .containsEntry("ORG_API_KEY", "********")         // 비밀 (ORG)
                .containsEntry("FEATURE_FLAG", "enabled")     // 일반
                .containsEntry("TENANT_API_KEY", "********")      // 비밀 (TENANT)
                .containsEntry("ENABLE_CACHE", "true")        // 일반
                .containsEntry("API_KEY", "********")             // 비밀 (DEFAULT)
                .hasSize(6);
        }
    }
}
