package com.ryuqq.fileflow.domain.settings;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * SettingMerger 유틸리티 단위 테스트
 *
 * <p>3단계 우선순위 병합 전략 테스트: ORG > TENANT > DEFAULT</p>
 * <p>Fixture 사용: {@link SettingFixture}를 활용하여 테스트 데이터 생성</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@DisplayName("SettingMerger 유틸리티 단위 테스트")
class SettingMergerTest {

    @Nested
    @DisplayName("3단계 우선순위 병합 테스트 (Happy Path)")
    class MergePriorityTests {

        @Test
        @DisplayName("ORG > TENANT > DEFAULT 우선순위로 병합")
        void merge_AppliesPriorityCorrectly() {
            // Given: 같은 키로 3단계 레벨 Setting 준비
            String keyName = "app.config";
            Setting defaultSetting = SettingFixture.createNew(keyName, "default-value", SettingLevel.DEFAULT, null);
            Setting tenantSetting = SettingFixture.createNew(keyName, "tenant-value", SettingLevel.TENANT, 1L);
            Setting orgSetting = SettingFixture.createNew(keyName, "org-value", SettingLevel.ORG, 10L);

            // When: 3단계 병합
            Map<SettingKey, Setting> merged = SettingMerger.merge(
                List.of(orgSetting),
                List.of(tenantSetting),
                List.of(defaultSetting)
            );

            // Then: ORG 레벨 값이 최종 선택됨
            Setting result = merged.get(SettingKey.of(keyName));
            assertThat(result.getRawValue()).isEqualTo("org-value");
            assertThat(result.getLevel()).isEqualTo(SettingLevel.ORG);
        }

        @Test
        @DisplayName("ORG 없이 TENANT > DEFAULT 병합")
        void merge_TenantOverridesDefault_WhenNoOrg() {
            // Given: TENANT와 DEFAULT 레벨만 존재
            String keyName = "app.config";
            Setting defaultSetting = SettingFixture.createNew(keyName, "default-value", SettingLevel.DEFAULT, null);
            Setting tenantSetting = SettingFixture.createNew(keyName, "tenant-value", SettingLevel.TENANT, 1L);

            // When: TENANT와 DEFAULT만 병합
            Map<SettingKey, Setting> merged = SettingMerger.merge(
                null,
                List.of(tenantSetting),
                List.of(defaultSetting)
            );

            // Then: TENANT 레벨 값이 선택됨
            Setting result = merged.get(SettingKey.of(keyName));
            assertThat(result.getRawValue()).isEqualTo("tenant-value");
            assertThat(result.getLevel()).isEqualTo(SettingLevel.TENANT);
        }

        @Test
        @DisplayName("DEFAULT만 있을 때 DEFAULT 값 사용")
        void merge_UsesDefaultOnly_WhenNoOrgOrTenant() {
            // Given: DEFAULT 레벨만 존재
            String keyName = "app.config";
            Setting defaultSetting = SettingFixture.createNew(keyName, "default-value", SettingLevel.DEFAULT, null);

            // When: DEFAULT만 병합
            Map<SettingKey, Setting> merged = SettingMerger.merge(
                null,
                null,
                List.of(defaultSetting)
            );

            // Then: DEFAULT 레벨 값이 선택됨
            Setting result = merged.get(SettingKey.of(keyName));
            assertThat(result.getRawValue()).isEqualTo("default-value");
            assertThat(result.getLevel()).isEqualTo(SettingLevel.DEFAULT);
        }

        @Test
        @DisplayName("여러 키의 Setting을 병합")
        void merge_MultipleKeys_MergesCorrectly() {
            // Given: 여러 키로 Setting 준비
            Setting defaultSetting1 = SettingFixture.createNew("key1", "default1", SettingLevel.DEFAULT, null);
            Setting defaultSetting2 = SettingFixture.createNew("key2", "default2", SettingLevel.DEFAULT, null);
            Setting tenantSetting1 = SettingFixture.createNew("key1", "tenant1", SettingLevel.TENANT, 1L);
            Setting orgSetting2 = SettingFixture.createNew("key2", "org2", SettingLevel.ORG, 10L);

            // When: 병합
            Map<SettingKey, Setting> merged = SettingMerger.merge(
                List.of(orgSetting2),
                List.of(tenantSetting1),
                List.of(defaultSetting1, defaultSetting2)
            );

            // Then: key1은 TENANT, key2는 ORG 값 선택
            assertThat(merged).hasSize(2);
            assertThat(merged.get(SettingKey.of("key1")).getRawValue()).isEqualTo("tenant1");
            assertThat(merged.get(SettingKey.of("key2")).getRawValue()).isEqualTo("org2");
        }
    }

    @Nested
    @DisplayName("키별 병합 테스트 (mergeByKey)")
    class MergeByKeyTests {

        @Test
        @DisplayName("특정 키의 ORG 레벨 Setting 반환")
        void mergeByKey_ReturnsOrgSetting_WhenPresent() {
            // Given: 특정 키로 3단계 Setting 준비
            String keyName = "app.config";
            SettingKey key = SettingKey.of(keyName);
            Setting defaultSetting = SettingFixture.createNew(keyName, "default", SettingLevel.DEFAULT, null);
            Setting tenantSetting = SettingFixture.createNew(keyName, "tenant", SettingLevel.TENANT, 1L);
            Setting orgSetting = SettingFixture.createNew(keyName, "org", SettingLevel.ORG, 10L);

            // When: 키로 병합
            Optional<Setting> result = SettingMerger.mergeByKey(
                key,
                List.of(orgSetting),
                List.of(tenantSetting),
                List.of(defaultSetting)
            );

            // Then: ORG 레벨 Setting 반환
            assertThat(result).isPresent();
            assertThat(result.get().getRawValue()).isEqualTo("org");
        }

        @Test
        @DisplayName("ORG 없을 때 TENANT 레벨 Setting 반환")
        void mergeByKey_ReturnsTenantSetting_WhenNoOrg() {
            // Given: TENANT와 DEFAULT만 준비
            String keyName = "app.config";
            SettingKey key = SettingKey.of(keyName);
            Setting defaultSetting = SettingFixture.createNew(keyName, "default", SettingLevel.DEFAULT, null);
            Setting tenantSetting = SettingFixture.createNew(keyName, "tenant", SettingLevel.TENANT, 1L);

            // When: 키로 병합
            Optional<Setting> result = SettingMerger.mergeByKey(
                key,
                null,
                List.of(tenantSetting),
                List.of(defaultSetting)
            );

            // Then: TENANT 레벨 Setting 반환
            assertThat(result).isPresent();
            assertThat(result.get().getRawValue()).isEqualTo("tenant");
        }

        @Test
        @DisplayName("모든 레벨에 없으면 Optional.empty() 반환")
        void mergeByKey_ReturnsEmpty_WhenNotPresent() {
            // Given: 다른 키로 Setting 준비
            SettingKey key = SettingKey.of("not-exist");
            Setting defaultSetting = SettingFixture.createNew("other-key", "default", SettingLevel.DEFAULT, null);

            // When: 키로 병합
            Optional<Setting> result = SettingMerger.mergeByKey(
                key,
                null,
                null,
                List.of(defaultSetting)
            );

            // Then: Optional.empty() 반환
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("key가 null이면 Optional.empty() 반환")
        void mergeByKey_ReturnsEmpty_WhenKeyIsNull() {
            // Given: key가 null
            Setting defaultSetting = SettingFixture.createNew("key", "default", SettingLevel.DEFAULT, null);

            // When: null key로 병합
            Optional<Setting> result = SettingMerger.mergeByKey(
                null,
                null,
                null,
                List.of(defaultSetting)
            );

            // Then: Optional.empty() 반환
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("List 형태 병합 테스트 (mergeToList)")
    class MergeToListTests {

        @Test
        @DisplayName("병합된 Setting을 List로 반환")
        void mergeToList_ReturnsListOfMergedSettings() {
            // Given: 여러 Setting 준비
            Setting defaultSetting1 = SettingFixture.createNew("key1", "default1", SettingLevel.DEFAULT, null);
            Setting defaultSetting2 = SettingFixture.createNew("key2", "default2", SettingLevel.DEFAULT, null);
            Setting tenantSetting1 = SettingFixture.createNew("key1", "tenant1", SettingLevel.TENANT, 1L);

            // When: List로 병합
            List<Setting> result = SettingMerger.mergeToList(
                null,
                List.of(tenantSetting1),
                List.of(defaultSetting1, defaultSetting2)
            );

            // Then: 병합된 Setting List 반환
            assertThat(result).hasSize(2);
            assertThat(result).anyMatch(s -> s.getKeyValue().equals("key1") && s.getRawValue().equals("tenant1"));
            assertThat(result).anyMatch(s -> s.getKeyValue().equals("key2") && s.getRawValue().equals("default2"));
        }
    }

    @Nested
    @DisplayName("값 맵 형태 병합 테스트 (mergeToValueMap)")
    class MergeToValueMapTests {

        @Test
        @DisplayName("병합된 Setting을 키-값 맵으로 반환 (표시값)")
        void mergeToValueMap_ReturnsDisplayValueMap() {
            // Given: Setting 준비
            Setting defaultSetting = SettingFixture.createNew("key1", "default1", SettingLevel.DEFAULT, null);
            Setting secretSetting = SettingFixture.builder()
                .key("api.secret")
                .value("secret-key-12345")
                .secret(true)
                .build();

            // When: 값 맵으로 병합
            Map<String, String> result = SettingMerger.mergeToValueMap(
                null,
                null,
                List.of(defaultSetting, secretSetting)
            );

            // Then: 키-표시값 맵 반환 (비밀 키는 마스킹됨)
            assertThat(result).containsKey("key1");
            assertThat(result).containsKey("api.secret");
            assertThat(result.get("key1")).isEqualTo("default1");
            assertThat(result.get("api.secret")).contains("*");
        }
    }

    @Nested
    @DisplayName("원시 값 맵 형태 병합 테스트 (mergeToRawValueMap)")
    class MergeToRawValueMapTests {

        @Test
        @DisplayName("병합된 Setting을 키-값 맵으로 반환 (원본값)")
        void mergeToRawValueMap_ReturnsRawValueMap() {
            // Given: Setting 준비
            Setting defaultSetting = SettingFixture.createNew("key1", "default1", SettingLevel.DEFAULT, null);
            Setting secretSetting = SettingFixture.builder()
                .key("api.secret")
                .value("secret-key-12345")
                .secret(true)
                .build();

            // When: 원시 값 맵으로 병합
            Map<String, String> result = SettingMerger.mergeToRawValueMap(
                null,
                null,
                List.of(defaultSetting, secretSetting)
            );

            // Then: 키-원본값 맵 반환 (비밀 키도 원본값 노출)
            assertThat(result).containsKey("key1");
            assertThat(result).containsKey("api.secret");
            assertThat(result.get("key1")).isEqualTo("default1");
            assertThat(result.get("api.secret")).isEqualTo("secret-key-12345");
        }
    }

    @Nested
    @DisplayName("경계값 테스트 (Edge Cases)")
    class EdgeCaseTests {

        @Test
        @DisplayName("모든 레벨이 null이면 빈 맵 반환")
        void merge_ReturnsEmptyMap_WhenAllLevelsAreNull() {
            // Given & When: 모든 레벨이 null
            Map<SettingKey, Setting> merged = SettingMerger.merge(null, null, null);

            // Then: 빈 맵 반환
            assertThat(merged).isEmpty();
        }

        @Test
        @DisplayName("모든 레벨이 빈 리스트면 빈 맵 반환")
        void merge_ReturnsEmptyMap_WhenAllLevelsAreEmpty() {
            // Given & When: 모든 레벨이 빈 리스트
            Map<SettingKey, Setting> merged = SettingMerger.merge(List.of(), List.of(), List.of());

            // Then: 빈 맵 반환
            assertThat(merged).isEmpty();
        }

        @Test
        @DisplayName("Setting에 null이 포함되어도 무시하고 병합")
        void merge_IgnoresNullSettings() {
            // Given: null Setting 포함
            Setting defaultSetting = SettingFixture.createNew("key1", "default1", SettingLevel.DEFAULT, null);

            // null을 직접 리스트에 넣지 않고, 가변 리스트 생성 후 추가
            java.util.List<Setting> settingsWithNull = new java.util.ArrayList<>();
            settingsWithNull.add(defaultSetting);
            settingsWithNull.add(null);

            // When: null 포함하여 병합
            Map<SettingKey, Setting> merged = SettingMerger.merge(
                null,
                null,
                settingsWithNull
            );

            // Then: null 무시하고 병합
            assertThat(merged).hasSize(1);
            assertThat(merged.get(SettingKey.of("key1"))).isNotNull();
        }

        @Test
        @DisplayName("중복 키가 있을 때 우선순위대로 덮어쓰기")
        void merge_OverwritesDuplicateKeys() {
            // Given: 같은 키로 2개의 DEFAULT Setting
            Setting defaultSetting1 = SettingFixture.createNew("key1", "value1", SettingLevel.DEFAULT, null);
            Setting defaultSetting2 = SettingFixture.createNew("key1", "value2", SettingLevel.DEFAULT, null);

            // When: 병합
            Map<SettingKey, Setting> merged = SettingMerger.merge(
                null,
                null,
                List.of(defaultSetting1, defaultSetting2)
            );

            // Then: 나중에 추가된 값으로 덮어쓰기됨
            assertThat(merged).hasSize(1);
            assertThat(merged.get(SettingKey.of("key1")).getRawValue()).isEqualTo("value2");
        }
    }

    @Nested
    @DisplayName("불변성 테스트 (Immutability)")
    class ImmutabilityTests {

        @Test
        @DisplayName("merge() 결과는 불변 맵")
        void merge_ReturnsUnmodifiableMap() {
            // Given: Setting 준비
            Setting defaultSetting = SettingFixture.createNew("key1", "default1", SettingLevel.DEFAULT, null);

            // When: 병합
            Map<SettingKey, Setting> merged = SettingMerger.merge(null, null, List.of(defaultSetting));

            // Then: 불변 맵 (수정 시 예외 발생)
            assertThatThrownBy(() -> merged.put(SettingKey.of("new-key"), defaultSetting))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("mergeToValueMap() 결과는 불변 맵")
        void mergeToValueMap_ReturnsUnmodifiableMap() {
            // Given: Setting 준비
            Setting defaultSetting = SettingFixture.createNew("key1", "default1", SettingLevel.DEFAULT, null);

            // When: 값 맵으로 병합
            Map<String, String> merged = SettingMerger.mergeToValueMap(null, null, List.of(defaultSetting));

            // Then: 불변 맵 (수정 시 예외 발생)
            assertThatThrownBy(() -> merged.put("new-key", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("mergeToRawValueMap() 결과는 불변 맵")
        void mergeToRawValueMap_ReturnsUnmodifiableMap() {
            // Given: Setting 준비
            Setting defaultSetting = SettingFixture.createNew("key1", "default1", SettingLevel.DEFAULT, null);

            // When: 원시 값 맵으로 병합
            Map<String, String> merged = SettingMerger.mergeToRawValueMap(null, null, List.of(defaultSetting));

            // Then: 불변 맵 (수정 시 예외 발생)
            assertThatThrownBy(() -> merged.put("new-key", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("유틸리티 클래스 불변식 테스트")
    class UtilityClassTests {

        @Test
        @DisplayName("SettingMerger는 인스턴스화 불가")
        void settingMerger_CannotBeInstantiated() {
            // When & Then: 리플렉션으로 private 생성자 접근 시 예외 발생
            assertThatThrownBy(() -> {
                var constructor = SettingMerger.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            })
            .getCause()
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Utility class cannot be instantiated");
        }
    }
}
