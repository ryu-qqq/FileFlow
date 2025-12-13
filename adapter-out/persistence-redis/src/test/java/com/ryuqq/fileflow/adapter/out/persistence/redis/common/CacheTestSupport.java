package com.ryuqq.fileflow.adapter.out.persistence.redis.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Cache Adapter 테스트 지원 추상 클래스
 *
 * <p>Cache Adapter 테스트에 특화된 유틸리티를 제공합니다. TTL 검증, 캐시 존재 여부 확인 등의 기능을 포함합니다.
 *
 * <p>제공 기능:
 *
 * <ul>
 *   <li>TTL 검증 유틸리티
 *   <li>캐시 히트/미스 검증
 *   <li>직렬화 검증 헬퍼
 * </ul>
 *
 * <h2>사용 예시:</h2>
 *
 * <pre>{@code
 * @DisplayName("ObjectCacheAdapter 통합 테스트")
 * class ObjectCacheAdapterTest extends CacheTestSupport {
 *
 *     @Autowired
 *     private ObjectCacheAdapter cacheAdapter;
 *
 *     @Test
 *     @DisplayName("성공 - TTL 설정 검증")
 *     void setWithTtl() {
 *         // Given
 *         OrderCacheKey key = new OrderCacheKey(100L);
 *         Order order = createTestOrder();
 *
 *         // When
 *         cacheAdapter.set(key, order, Duration.ofMinutes(10));
 *
 *         // Then
 *         assertTtlSet(key.value(), 600, 10);  // 10분, 10초 오차 허용
 *     }
 * }
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 * @see RedisTestSupport 기본 Redis 테스트 지원
 */
public abstract class CacheTestSupport extends RedisTestSupport {

    /**
     * TTL이 설정되었는지 검증
     *
     * <p>지정된 키에 TTL이 설정되어 있고, 예상 TTL 범위 내에 있는지 검증합니다.
     *
     * @param key 캐시 키
     * @param expectedTtlSeconds 예상 TTL (초)
     * @param toleranceSeconds 허용 오차 (초)
     */
    protected void assertTtlSet(String key, long expectedTtlSeconds, long toleranceSeconds) {
        Long actualTtl = getTtl(key);
        assertThat(actualTtl)
                .as("TTL should be set for key: %s", key)
                .isGreaterThan(0)
                .isLessThanOrEqualTo(expectedTtlSeconds)
                .isGreaterThanOrEqualTo(expectedTtlSeconds - toleranceSeconds);
    }

    /**
     * TTL이 설정되었는지 검증 (Duration 사용)
     *
     * @param key 캐시 키
     * @param expectedTtl 예상 TTL
     * @param tolerance 허용 오차
     */
    protected void assertTtlSet(String key, Duration expectedTtl, Duration tolerance) {
        assertTtlSet(key, expectedTtl.toSeconds(), tolerance.toSeconds());
    }

    /**
     * TTL이 설정되지 않았는지 검증
     *
     * @param key 캐시 키
     */
    protected void assertNoTtl(String key) {
        Long ttl = getTtl(key);
        assertThat(ttl)
                .as("TTL should not be set for key: %s (TTL=-1 means no expiration)", key)
                .isEqualTo(-1L);
    }

    /**
     * 캐시가 존재하는지 검증 (CacheKey 버전)
     *
     * @param key CacheKey 값
     */
    protected void assertCacheExists(String key) {
        assertKeyExists(key);
    }

    /**
     * 캐시가 존재하지 않는지 검증 (CacheKey 버전)
     *
     * @param key CacheKey 값
     */
    protected void assertCacheNotExists(String key) {
        assertKeyNotExists(key);
    }

    /**
     * 캐시 값이 예상과 일치하는지 검증
     *
     * @param key 캐시 키
     * @param expectedValue 예상 값
     */
    protected void assertCacheValueEquals(String key, Object expectedValue) {
        Object actualValue = getDirectly(key);
        assertThat(actualValue)
                .as("Cache value for key '%s' should match", key)
                .isEqualTo(expectedValue);
    }

    /**
     * 캐시 만료 대기 후 미존재 검증
     *
     * <p>TTL 만료를 기다린 후 캐시가 삭제되었는지 확인합니다. 테스트 시간이 길어질 수 있으므로 짧은 TTL로 테스트하세요.
     *
     * @param key 캐시 키
     * @param waitSeconds 대기 시간 (초)
     * @throws InterruptedException 대기 중 인터럽트 발생 시
     */
    protected void assertCacheExpiredAfter(String key, long waitSeconds)
            throws InterruptedException {
        TimeUnit.SECONDS.sleep(waitSeconds);
        assertCacheNotExists(key);
    }

    /**
     * 캐시 히트 검증 (값이 존재하고 null이 아님)
     *
     * @param key 캐시 키
     */
    protected void assertCacheHit(String key) {
        assertCacheExists(key);
        Object value = getDirectly(key);
        assertThat(value).as("Cache hit expected for key: %s", key).isNotNull();
    }

    /**
     * 캐시 미스 검증 (키가 존재하지 않음)
     *
     * @param key 캐시 키
     */
    protected void assertCacheMiss(String key) {
        assertCacheNotExists(key);
    }

    /**
     * 캐시 키 패턴 검증
     *
     * <p>캐시 키가 예상된 접두사로 시작하는지 확인합니다.
     *
     * @param actualKey 실제 캐시 키
     * @param expectedPrefix 예상 접두사
     */
    protected void assertKeyHasPrefix(String actualKey, String expectedPrefix) {
        assertThat(actualKey)
                .as("Cache key should start with prefix: %s", expectedPrefix)
                .startsWith(expectedPrefix);
    }

    /**
     * 여러 캐시가 모두 존재하는지 검증
     *
     * @param keys 캐시 키 목록
     */
    protected void assertAllCachesExist(String... keys) {
        for (String key : keys) {
            assertCacheExists(key);
        }
    }

    /**
     * 여러 캐시가 모두 존재하지 않는지 검증
     *
     * @param keys 캐시 키 목록
     */
    protected void assertNoCachesExist(String... keys) {
        for (String key : keys) {
            assertCacheNotExists(key);
        }
    }
}
