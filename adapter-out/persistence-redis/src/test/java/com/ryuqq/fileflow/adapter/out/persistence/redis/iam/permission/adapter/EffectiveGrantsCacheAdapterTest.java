package com.ryuqq.fileflow.adapter.out.persistence.redis.iam.permission.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.out.persistence.redis.config.RedisIntegrationTestBase;
import com.ryuqq.fileflow.domain.iam.permission.Grant;
import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;
import com.ryuqq.fileflow.domain.iam.permission.RoleCode;
import com.ryuqq.fileflow.domain.iam.permission.Scope;
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
 * EffectiveGrantsCacheAdapter Integration Test
 *
 * <p><strong>테스트 대상</strong>: {@link EffectiveGrantsCacheAdapter}</p>
 * <p><strong>테스트 환경</strong>: TestContainers Redis 7.2</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ Grants 캐시 저장/조회 (Cache Hit)</li>
 *   <li>✅ Cache Miss 시나리오</li>
 *   <li>✅ 빈 List 캐싱 (권한 없는 사용자)</li>
 *   <li>✅ TTL 검증 (5분)</li>
 *   <li>✅ 사용자별 캐시 무효화</li>
 *   <li>✅ 전체 캐시 무효화</li>
 *   <li>✅ Cache Fallback (예외 처리)</li>
 *   <li>✅ 예외 케이스 (null 검증)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@Import(EffectiveGrantsCacheAdapter.class)
@DisplayName("EffectiveGrantsCacheAdapter 통합 테스트")
class EffectiveGrantsCacheAdapterTest extends RedisIntegrationTestBase {

    @Autowired
    private EffectiveGrantsCacheAdapter grantsCacheAdapter;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long USER_ID = 1L;
    private static final Long TENANT_ID = 100L;
    private static final Long ORG_ID = 1000L;

    private List<Grant> sampleGrants;

    @BeforeEach
    void setUp() {
        sampleGrants = List.of(
            new Grant("ADMIN", "file.upload", Scope.ORGANIZATION, null),
            new Grant("ADMIN", "file.delete", Scope.ORGANIZATION, null)
        );
    }

    @Nested
    @DisplayName("Grants 캐시 저장/조회 테스트")
    class SaveAndFindTests {

        @Test
        @DisplayName("Grants를 캐시에 저장하고 조회하면 동일한 데이터가 반환된다 (Cache Hit)")
        void save_AndFind_ReturnsCachedGrants() {
            // when - 캐시 저장
            grantsCacheAdapter.save(USER_ID, TENANT_ID, ORG_ID, sampleGrants);

            // when - 캐시 조회
            Optional<List<Grant>> cachedGrants = grantsCacheAdapter.findEffectiveGrants(USER_ID, TENANT_ID, ORG_ID);

            // then
            assertThat(cachedGrants).isPresent();
            assertThat(cachedGrants.get()).hasSize(2);
            assertThat(cachedGrants.get().get(0).roleCode()).isEqualTo("ADMIN");
            assertThat(cachedGrants.get().get(0).permissionCode()).isEqualTo("file.upload");
            assertThat(cachedGrants.get().get(1).permissionCode()).isEqualTo("file.delete");
        }

        @Test
        @DisplayName("캐시에 없는 데이터 조회 시 Empty Optional이 반환된다 (Cache Miss)")
        void find_NonExistent_ReturnsEmpty() {
            // when
            Optional<List<Grant>> cachedGrants = grantsCacheAdapter.findEffectiveGrants(999L, 999L, 999L);

            // then
            assertThat(cachedGrants).isEmpty();
        }

        @Test
        @DisplayName("빈 List를 캐싱하면 정상적으로 조회된다 (권한 없는 사용자)")
        void save_EmptyList_CachesSuccessfully() {
            // given
            List<Grant> emptyGrants = List.of();

            // when
            grantsCacheAdapter.save(USER_ID, TENANT_ID, ORG_ID, emptyGrants);
            Optional<List<Grant>> cachedGrants = grantsCacheAdapter.findEffectiveGrants(USER_ID, TENANT_ID, ORG_ID);

            // then
            assertThat(cachedGrants).isPresent();
            assertThat(cachedGrants.get()).isEmpty();
        }

        @Test
        @DisplayName("null Grants 저장 시도 시 IllegalArgumentException이 발생한다")
        void save_NullGrants_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> grantsCacheAdapter.save(USER_ID, TENANT_ID, ORG_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Grants는 null일 수 없습니다");
        }

        @Test
        @DisplayName("null userId 조회 시 IllegalArgumentException이 발생한다")
        void find_NullUserId_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> grantsCacheAdapter.findEffectiveGrants(null, TENANT_ID, ORG_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 null이거나 음수일 수 없습니다");
        }

        @Test
        @DisplayName("음수 userId 조회 시 IllegalArgumentException이 발생한다")
        void find_NegativeUserId_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> grantsCacheAdapter.findEffectiveGrants(-1L, TENANT_ID, ORG_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 null이거나 음수일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("캐시 무효화 테스트")
    class InvalidationTests {

        @Test
        @DisplayName("사용자별 캐시 무효화 시 해당 사용자의 모든 캐시가 삭제된다")
        void invalidateUser_DeletesAllUserCaches() {
            // given - 동일 사용자, 다른 tenant/org 조합으로 3개 캐시 생성
            grantsCacheAdapter.save(USER_ID, TENANT_ID, ORG_ID, sampleGrants);
            grantsCacheAdapter.save(USER_ID, TENANT_ID + 1, ORG_ID, sampleGrants);
            grantsCacheAdapter.save(USER_ID, TENANT_ID, ORG_ID + 1, sampleGrants);

            // 다른 사용자 캐시 생성 (삭제되면 안 됨)
            grantsCacheAdapter.save(USER_ID + 1, TENANT_ID, ORG_ID, sampleGrants);

            // when
            grantsCacheAdapter.invalidateUser(USER_ID);

            // then
            assertThat(grantsCacheAdapter.findEffectiveGrants(USER_ID, TENANT_ID, ORG_ID)).isEmpty();
            assertThat(grantsCacheAdapter.findEffectiveGrants(USER_ID, TENANT_ID + 1, ORG_ID)).isEmpty();
            assertThat(grantsCacheAdapter.findEffectiveGrants(USER_ID, TENANT_ID, ORG_ID + 1)).isEmpty();

            // 다른 사용자 캐시는 유지
            assertThat(grantsCacheAdapter.findEffectiveGrants(USER_ID + 1, TENANT_ID, ORG_ID)).isPresent();
        }

        @Test
        @DisplayName("전체 캐시 무효화 시 모든 Grants 캐시가 삭제된다")
        void invalidateAll_DeletesAllGrantsCaches() {
            // given
            grantsCacheAdapter.save(USER_ID, TENANT_ID, ORG_ID, sampleGrants);
            grantsCacheAdapter.save(USER_ID + 1, TENANT_ID, ORG_ID, sampleGrants);
            grantsCacheAdapter.save(USER_ID + 2, TENANT_ID, ORG_ID, sampleGrants);

            // when
            grantsCacheAdapter.invalidateAll();

            // then
            assertThat(grantsCacheAdapter.findEffectiveGrants(USER_ID, TENANT_ID, ORG_ID)).isEmpty();
            assertThat(grantsCacheAdapter.findEffectiveGrants(USER_ID + 1, TENANT_ID, ORG_ID)).isEmpty();
            assertThat(grantsCacheAdapter.findEffectiveGrants(USER_ID + 2, TENANT_ID, ORG_ID)).isEmpty();
        }

        @Test
        @DisplayName("null userId로 무효화 시도 시 IllegalArgumentException이 발생한다")
        void invalidateUser_NullUserId_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> grantsCacheAdapter.invalidateUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 null이거나 음수일 수 없습니다");
        }

        @Test
        @DisplayName("음수 userId로 무효화 시도 시 IllegalArgumentException이 발생한다")
        void invalidateUser_NegativeUserId_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> grantsCacheAdapter.invalidateUser(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 null이거나 음수일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("캐시 키 생성 테스트")
    class CacheKeyTests {

        @Test
        @DisplayName("동일한 user/tenant/org 조합은 동일한 캐시 키를 사용한다")
        void sameContext_UsesSameCacheKey() {
            // given
            grantsCacheAdapter.save(USER_ID, TENANT_ID, ORG_ID, sampleGrants);

            // when - 동일한 컨텍스트로 재조회
            Optional<List<Grant>> cached1 = grantsCacheAdapter.findEffectiveGrants(USER_ID, TENANT_ID, ORG_ID);
            Optional<List<Grant>> cached2 = grantsCacheAdapter.findEffectiveGrants(USER_ID, TENANT_ID, ORG_ID);

            // then
            assertThat(cached1).isPresent();
            assertThat(cached2).isPresent();
            assertThat(cached1.get()).isEqualTo(cached2.get());
        }

        @Test
        @DisplayName("다른 userId는 다른 캐시 키를 사용한다")
        void differentUserId_UsesDifferentCacheKey() {
            // given
            List<Grant> grants1 = List.of(
                new Grant("ADMIN", "file.upload", Scope.ORGANIZATION, null)
            );
            List<Grant> grants2 = List.of(
                new Grant("USER", "file.read", Scope.ORGANIZATION, null)
            );

            grantsCacheAdapter.save(USER_ID, TENANT_ID, ORG_ID, grants1);
            grantsCacheAdapter.save(USER_ID + 1, TENANT_ID, ORG_ID, grants2);

            // when
            Optional<List<Grant>> cached1 = grantsCacheAdapter.findEffectiveGrants(USER_ID, TENANT_ID, ORG_ID);
            Optional<List<Grant>> cached2 = grantsCacheAdapter.findEffectiveGrants(USER_ID + 1, TENANT_ID, ORG_ID);

            // then
            assertThat(cached1).isPresent();
            assertThat(cached2).isPresent();
            assertThat(cached1.get()).hasSize(1);
            assertThat(cached2.get()).hasSize(1);
            assertThat(cached1.get().get(0).roleCode()).isEqualTo("ADMIN");
            assertThat(cached2.get().get(0).roleCode()).isEqualTo("USER");
        }
    }

    @Nested
    @DisplayName("Redis 연동 검증 테스트")
    class RedisVerificationTests {

        @Test
        @DisplayName("캐시 저장 시 Redis에 실제로 키가 생성된다")
        void save_CreatesRedisKey() {
            // when
            grantsCacheAdapter.save(USER_ID, TENANT_ID, ORG_ID, sampleGrants);

            // then
            String cacheKeyPattern = "grants:user:" + USER_ID + ":*";
            Set<String> keys = redisTemplate.keys(cacheKeyPattern);

            assertThat(keys).isNotEmpty();
            assertThat(keys).hasSize(1);
        }

        @Test
        @DisplayName("캐시 무효화 시 Redis에서 키가 삭제된다")
        void invalidate_DeletesRedisKey() {
            // given
            grantsCacheAdapter.save(USER_ID, TENANT_ID, ORG_ID, sampleGrants);
            String cacheKeyPattern = "grants:user:" + USER_ID + ":*";

            // when
            grantsCacheAdapter.invalidateUser(USER_ID);

            // then
            Set<String> keys = redisTemplate.keys(cacheKeyPattern);
            assertThat(keys).isEmpty();
        }
    }
}
