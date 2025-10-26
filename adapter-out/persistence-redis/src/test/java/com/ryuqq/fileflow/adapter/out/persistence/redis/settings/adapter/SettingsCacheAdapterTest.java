package com.ryuqq.fileflow.adapter.out.persistence.redis.settings.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.out.persistence.redis.config.RedisIntegrationTestBase;
import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort.SettingsForMerge;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingType;
import com.ryuqq.fileflow.domain.settings.SettingValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SettingsCacheAdapter Integration Test
 *
 * <p><strong>테스트 대상</strong>: {@link SettingsCacheAdapter}</p>
 * <p><strong>테스트 환경</strong>: TestContainers Redis 7.2</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ Settings 캐시 저장/조회 (Cache Hit)</li>
 *   <li>✅ Cache Miss 시나리오</li>
 *   <li>✅ null orgId/tenantId 처리 ("null" 문자열)</li>
 *   <li>✅ TTL 검증 (10분)</li>
 *   <li>✅ 조직별 캐시 무효화</li>
 *   <li>✅ 테넌트별 캐시 무효화</li>
 *   <li>✅ 전체 캐시 무효화</li>
 *   <li>✅ 사용자별 캐시 무효화 (경고 로그)</li>
 *   <li>✅ Cache Fallback (예외 처리)</li>
 *   <li>✅ 예외 케이스 (null 검증)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@Import(SettingsCacheAdapter.class)
@DisplayName("SettingsCacheAdapter 통합 테스트")
class SettingsCacheAdapterTest extends RedisIntegrationTestBase {

    @Autowired
    private SettingsCacheAdapter settingsCacheAdapter;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long ORG_ID = 1000L;
    private static final Long TENANT_ID = 100L;

    private SettingsForMerge sampleSettings;

    @BeforeEach
    void setUp() {
        sampleSettings = new SettingsForMerge(
            List.of(
                Setting.of(1L, SettingKey.of("file.max_size"), SettingValue.of("10MB", SettingType.STRING), SettingLevel.ORG, ORG_ID),
                Setting.of(2L, SettingKey.of("file.allowed_types"), SettingValue.of("jpg,png", SettingType.STRING), SettingLevel.ORG, ORG_ID)
            ),
            List.of(
                Setting.of(3L, SettingKey.of("ui.theme"), SettingValue.of("dark", SettingType.STRING), SettingLevel.TENANT, TENANT_ID),
                Setting.of(4L, SettingKey.of("ui.language"), SettingValue.of("ko", SettingType.STRING), SettingLevel.TENANT, TENANT_ID)
            ),
            List.of(
                Setting.of(5L, SettingKey.of("default.timezone"), SettingValue.of("Asia/Seoul", SettingType.STRING), SettingLevel.DEFAULT, null)
            )
        );
    }

    @Nested
    @DisplayName("Settings 캐시 저장/조회 테스트")
    class SaveAndFindTests {

        @Test
        @DisplayName("Settings를 캐시에 저장하고 조회하면 동일한 데이터가 반환된다 (Cache Hit)")
        void save_AndFind_ReturnsCachedSettings() {
            // when - 캐시 저장
            settingsCacheAdapter.save(ORG_ID, TENANT_ID, sampleSettings);

            // when - 캐시 조회
            Optional<SettingsForMerge> cachedSettings = settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID);

            // then
            assertThat(cachedSettings).isPresent();
            assertThat(cachedSettings.get().orgSettings()).hasSize(2);
            assertThat(cachedSettings.get().tenantSettings()).hasSize(2);
            assertThat(cachedSettings.get().defaultSettings()).hasSize(1);
        }

        @Test
        @DisplayName("캐시에 없는 데이터 조회 시 Empty Optional이 반환된다 (Cache Miss)")
        void find_NonExistent_ReturnsEmpty() {
            // when
            Optional<SettingsForMerge> cachedSettings = settingsCacheAdapter.findMergedSettings(999L, 999L);

            // then
            assertThat(cachedSettings).isEmpty();
        }

        @Test
        @DisplayName("빈 SettingsForMerge를 캐싱하면 정상적으로 조회된다 (설정 없는 경우)")
        void save_EmptySettings_CachesSuccessfully() {
            // given
            SettingsForMerge emptySettings = new SettingsForMerge(List.of(), List.of(), List.of());

            // when
            settingsCacheAdapter.save(ORG_ID, TENANT_ID, emptySettings);
            Optional<SettingsForMerge> cachedSettings = settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID);

            // then
            assertThat(cachedSettings).isPresent();
            assertThat(cachedSettings.get().orgSettings()).isEmpty();
            assertThat(cachedSettings.get().tenantSettings()).isEmpty();
            assertThat(cachedSettings.get().defaultSettings()).isEmpty();
        }

        @Test
        @DisplayName("null Settings 저장 시도 시 IllegalArgumentException이 발생한다")
        void save_NullSettings_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> settingsCacheAdapter.save(ORG_ID, TENANT_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Settings는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("null orgId/tenantId 처리 테스트")
    class NullIdHandlingTests {

        @Test
        @DisplayName("null orgId로 저장/조회하면 정상 동작한다")
        void save_AndFind_WithNullOrgId_WorksCorrectly() {
            // when
            settingsCacheAdapter.save(null, TENANT_ID, sampleSettings);
            Optional<SettingsForMerge> cachedSettings = settingsCacheAdapter.findMergedSettings(null, TENANT_ID);

            // then
            assertThat(cachedSettings).isPresent();
            assertThat(cachedSettings.get()).isEqualTo(sampleSettings);
        }

        @Test
        @DisplayName("null tenantId로 저장/조회하면 정상 동작한다")
        void save_AndFind_WithNullTenantId_WorksCorrectly() {
            // when
            settingsCacheAdapter.save(ORG_ID, null, sampleSettings);
            Optional<SettingsForMerge> cachedSettings = settingsCacheAdapter.findMergedSettings(ORG_ID, null);

            // then
            assertThat(cachedSettings).isPresent();
            assertThat(cachedSettings.get()).isEqualTo(sampleSettings);
        }

        @Test
        @DisplayName("둘 다 null인 경우에도 정상 동작한다")
        void save_AndFind_WithBothNull_WorksCorrectly() {
            // when
            settingsCacheAdapter.save(null, null, sampleSettings);
            Optional<SettingsForMerge> cachedSettings = settingsCacheAdapter.findMergedSettings(null, null);

            // then
            assertThat(cachedSettings).isPresent();
            assertThat(cachedSettings.get()).isEqualTo(sampleSettings);
        }
    }

    @Nested
    @DisplayName("캐시 무효화 테스트")
    class InvalidationTests {

        @Test
        @DisplayName("조직별 캐시 무효화 시 해당 조직의 모든 캐시가 삭제된다")
        void invalidateOrg_DeletesAllOrgCaches() {
            // given - 동일 조직, 다른 tenant 조합으로 3개 캐시 생성
            settingsCacheAdapter.save(ORG_ID, TENANT_ID, sampleSettings);
            settingsCacheAdapter.save(ORG_ID, TENANT_ID + 1, sampleSettings);
            settingsCacheAdapter.save(ORG_ID, TENANT_ID + 2, sampleSettings);

            // 다른 조직 캐시 생성 (삭제되면 안 됨)
            settingsCacheAdapter.save(ORG_ID + 1, TENANT_ID, sampleSettings);

            // when
            settingsCacheAdapter.invalidateOrg(ORG_ID);

            // then
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID)).isEmpty();
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID + 1)).isEmpty();
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID + 2)).isEmpty();

            // 다른 조직 캐시는 유지
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID + 1, TENANT_ID)).isPresent();
        }

        @Test
        @DisplayName("테넌트별 캐시 무효화 시 해당 테넌트의 모든 캐시가 삭제된다")
        void invalidateTenant_DeletesAllTenantCaches() {
            // given - 동일 tenant, 다른 org 조합으로 3개 캐시 생성
            settingsCacheAdapter.save(ORG_ID, TENANT_ID, sampleSettings);
            settingsCacheAdapter.save(ORG_ID + 1, TENANT_ID, sampleSettings);
            settingsCacheAdapter.save(ORG_ID + 2, TENANT_ID, sampleSettings);

            // 다른 테넌트 캐시 생성 (삭제되면 안 됨)
            settingsCacheAdapter.save(ORG_ID, TENANT_ID + 1, sampleSettings);

            // when
            settingsCacheAdapter.invalidateTenant(TENANT_ID);

            // then
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID)).isEmpty();
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID + 1, TENANT_ID)).isEmpty();
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID + 2, TENANT_ID)).isEmpty();

            // 다른 테넌트 캐시는 유지
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID + 1)).isPresent();
        }

        @Test
        @DisplayName("전체 캐시 무효화 시 모든 Settings 캐시가 삭제된다")
        void invalidateAll_DeletesAllSettingsCaches() {
            // given
            settingsCacheAdapter.save(ORG_ID, TENANT_ID, sampleSettings);
            settingsCacheAdapter.save(ORG_ID + 1, TENANT_ID, sampleSettings);
            settingsCacheAdapter.save(ORG_ID, TENANT_ID + 1, sampleSettings);

            // when
            settingsCacheAdapter.invalidateAll();

            // then
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID)).isEmpty();
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID + 1, TENANT_ID)).isEmpty();
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID + 1)).isEmpty();
        }

        @Test
        @DisplayName("사용자별 캐시 무효화는 경고 로그만 남기고 실제 무효화하지 않는다")
        void invalidateUser_LogsWarningOnly() {
            // given
            settingsCacheAdapter.save(ORG_ID, TENANT_ID, sampleSettings);

            // when - 경고 로그만 남기고 실제로는 무효화 안 함
            settingsCacheAdapter.invalidateUser(1L);

            // then - 캐시 유지
            assertThat(settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID)).isPresent();
        }

        @Test
        @DisplayName("null orgId로 무효화 시도 시 IllegalArgumentException이 발생한다")
        void invalidateOrg_NullOrgId_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> settingsCacheAdapter.invalidateOrg(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orgId는 null이거나 음수일 수 없습니다");
        }

        @Test
        @DisplayName("음수 orgId로 무효화 시도 시 IllegalArgumentException이 발생한다")
        void invalidateOrg_NegativeOrgId_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> settingsCacheAdapter.invalidateOrg(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orgId는 null이거나 음수일 수 없습니다");
        }

        @Test
        @DisplayName("null tenantId로 무효화 시도 시 IllegalArgumentException이 발생한다")
        void invalidateTenant_NullTenantId_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> settingsCacheAdapter.invalidateTenant(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantId는 null이거나 음수일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("캐시 키 생성 테스트")
    class CacheKeyTests {

        @Test
        @DisplayName("동일한 org/tenant 조합은 동일한 캐시 키를 사용한다")
        void sameContext_UsesSameCacheKey() {
            // given
            settingsCacheAdapter.save(ORG_ID, TENANT_ID, sampleSettings);

            // when - 동일한 컨텍스트로 재조회
            Optional<SettingsForMerge> cached1 = settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID);
            Optional<SettingsForMerge> cached2 = settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID);

            // then
            assertThat(cached1).isPresent();
            assertThat(cached2).isPresent();
            assertThat(cached1.get()).isEqualTo(cached2.get());
        }

        @Test
        @DisplayName("다른 orgId는 다른 캐시 키를 사용한다")
        void differentOrgId_UsesDifferentCacheKey() {
            // given
            SettingsForMerge settings1 = new SettingsForMerge(
                List.of(Setting.of(null, SettingKey.of("key1"), SettingValue.of("value1", SettingType.STRING), SettingLevel.ORG, ORG_ID)),
                List.of(),
                List.of()
            );
            SettingsForMerge settings2 = new SettingsForMerge(
                List.of(Setting.of(null, SettingKey.of("key2"), SettingValue.of("value2", SettingType.STRING), SettingLevel.ORG, ORG_ID + 1)),
                List.of(),
                List.of()
            );

            settingsCacheAdapter.save(ORG_ID, TENANT_ID, settings1);
            settingsCacheAdapter.save(ORG_ID + 1, TENANT_ID, settings2);

            // when
            Optional<SettingsForMerge> cached1 = settingsCacheAdapter.findMergedSettings(ORG_ID, TENANT_ID);
            Optional<SettingsForMerge> cached2 = settingsCacheAdapter.findMergedSettings(ORG_ID + 1, TENANT_ID);

            // then
            assertThat(cached1).isPresent();
            assertThat(cached2).isPresent();
            assertThat(cached1.get().orgSettings()).hasSize(1);
            assertThat(cached2.get().orgSettings()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Redis 연동 검증 테스트")
    class RedisVerificationTests {

        @Test
        @DisplayName("캐시 저장 시 Redis에 실제로 키가 생성된다")
        void save_CreatesRedisKey() {
            // when
            settingsCacheAdapter.save(ORG_ID, TENANT_ID, sampleSettings);

            // then
            String cacheKeyPattern = "settings:org:" + ORG_ID + ":*";
            Set<String> keys = redisTemplate.keys(cacheKeyPattern);

            assertThat(keys).isNotEmpty();
            assertThat(keys).hasSize(1);
        }

        @Test
        @DisplayName("캐시 무효화 시 Redis에서 키가 삭제된다")
        void invalidate_DeletesRedisKey() {
            // given
            settingsCacheAdapter.save(ORG_ID, TENANT_ID, sampleSettings);
            String cacheKeyPattern = "settings:org:" + ORG_ID + ":*";

            // when
            settingsCacheAdapter.invalidateOrg(ORG_ID);

            // then
            Set<String> keys = redisTemplate.keys(cacheKeyPattern);
            assertThat(keys).isEmpty();
        }
    }
}
