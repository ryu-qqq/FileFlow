package com.ryuqq.fileflow.adapter.out.persistence.redis.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.redis.common.CacheTestSupport;
import com.ryuqq.fileflow.adapter.out.persistence.redis.common.TestCacheKey;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * StringCacheAdapter 통합 테스트
 *
 * <p>Redis를 사용한 String 타입 캐시 기능을 검증합니다.
 */
@DisplayName("StringCacheAdapter 통합 테스트")
class StringCacheAdapterTest extends CacheTestSupport {

    @Autowired private StringCacheAdapter cacheAdapter;

    @Nested
    @DisplayName("set 메서드")
    class SetMethod {

        @Test
        @DisplayName("성공 - 기본 TTL로 문자열 캐시 저장")
        void set_withDefaultTtl_success() {
            // Given
            TestCacheKey key = new TestCacheKey("string-1");
            String value = "test-string-value";

            // When
            cacheAdapter.set(key, value);

            // Then
            assertCacheExists(key.value());
            assertTtlSet(key.value(), 1800, 60); // 기본 30분 TTL
        }

        @Test
        @DisplayName("성공 - 지정된 TTL로 문자열 캐시 저장")
        void set_withCustomTtl_success() {
            // Given
            TestCacheKey key = new TestCacheKey("string-2");
            String value = "custom-ttl-value";
            Duration ttl = Duration.ofMinutes(15);

            // When
            cacheAdapter.set(key, value, ttl);

            // Then
            assertCacheExists(key.value());
            assertTtlSet(key.value(), 900, 10); // 15분 TTL
        }

        @Test
        @DisplayName("성공 - 긴 문자열 캐시 저장")
        void set_longString_success() {
            // Given
            TestCacheKey key = new TestCacheKey("long-string");
            String value = "A".repeat(10000);

            // When
            cacheAdapter.set(key, value, Duration.ofMinutes(5));

            // Then
            assertCacheExists(key.value());
        }

        @Test
        @DisplayName("성공 - JSON 형태 문자열 캐시 저장")
        void set_jsonString_success() {
            // Given
            TestCacheKey key = new TestCacheKey("json-string");
            String value = "{\"name\":\"test\",\"value\":123}";

            // When
            cacheAdapter.set(key, value, Duration.ofMinutes(5));

            // Then
            assertCacheExists(key.value());
            Optional<String> result = cacheAdapter.get(key);
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("{\"name\":\"test\",\"value\":123}");
        }
    }

    @Nested
    @DisplayName("get 메서드")
    class GetMethod {

        @Test
        @DisplayName("성공 - 문자열 캐시 조회")
        void get_existingKey_success() {
            // Given
            TestCacheKey key = new TestCacheKey("get-string-1");
            String value = "retrieved-string";
            cacheAdapter.set(key, value, Duration.ofMinutes(5));

            // When
            Optional<String> result = cacheAdapter.get(key);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("retrieved-string");
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 키 조회 시 빈 Optional 반환")
        void get_nonExistingKey_returnsEmpty() {
            // Given
            TestCacheKey key = new TestCacheKey("non-existing-string");

            // When
            Optional<String> result = cacheAdapter.get(key);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - 클래스 타입으로 조회")
        void get_withClass_success() {
            // Given
            TestCacheKey key = new TestCacheKey("typed-string-1");
            String value = "typed-string-value";
            cacheAdapter.set(key, value, Duration.ofMinutes(5));

            // When
            Optional<String> result = cacheAdapter.get(key, String.class);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("typed-string-value");
        }
    }

    @Nested
    @DisplayName("evict 메서드")
    class EvictMethod {

        @Test
        @DisplayName("성공 - 문자열 캐시 삭제")
        void evict_existingKey_success() {
            // Given
            TestCacheKey key = new TestCacheKey("evict-string-1");
            cacheAdapter.set(key, "to-delete", Duration.ofMinutes(5));
            assertCacheExists(key.value());

            // When
            cacheAdapter.evict(key);

            // Then
            assertCacheNotExists(key.value());
        }
    }

    @Nested
    @DisplayName("evictByPattern 메서드")
    class EvictByPatternMethod {

        @Test
        @DisplayName("성공 - 패턴으로 여러 문자열 캐시 삭제")
        void evictByPattern_multipleKeys_success() {
            // Given
            cacheAdapter.set(new TestCacheKey("str-pattern-1"), "value1", Duration.ofMinutes(5));
            cacheAdapter.set(new TestCacheKey("str-pattern-2"), "value2", Duration.ofMinutes(5));
            cacheAdapter.set(new TestCacheKey("str-other-1"), "value3", Duration.ofMinutes(5));

            assertAllCachesExist(
                    "test::cache::str-pattern-1",
                    "test::cache::str-pattern-2",
                    "test::cache::str-other-1");

            // When
            cacheAdapter.evictByPattern("test::cache::str-pattern-*");

            // Then
            assertNoCachesExist("test::cache::str-pattern-1", "test::cache::str-pattern-2");
            assertCacheExists("test::cache::str-other-1");
        }
    }

    @Nested
    @DisplayName("exists 메서드")
    class ExistsMethod {

        @Test
        @DisplayName("성공 - 존재하는 문자열 키 확인")
        void exists_existingKey_returnsTrue() {
            // Given
            TestCacheKey key = new TestCacheKey("exists-string-1");
            cacheAdapter.set(key, "exists-value", Duration.ofMinutes(5));

            // When
            boolean result = cacheAdapter.exists(key);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 키 확인")
        void exists_nonExistingKey_returnsFalse() {
            // Given
            TestCacheKey key = new TestCacheKey("non-exists-string");

            // When
            boolean result = cacheAdapter.exists(key);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getTtl 메서드")
    class GetTtlMethod {

        @Test
        @DisplayName("성공 - 문자열 캐시 TTL 조회")
        void getTtl_existingKey_returnsTtl() {
            // Given
            TestCacheKey key = new TestCacheKey("ttl-string-1");
            cacheAdapter.set(key, "ttl-value", Duration.ofMinutes(10));

            // When
            Duration ttl = cacheAdapter.getTtl(key);

            // Then
            assertThat(ttl).isNotNull();
            assertThat(ttl.toSeconds()).isLessThanOrEqualTo(600);
            assertThat(ttl.toSeconds()).isGreaterThan(590);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 키의 TTL 조회 시 null 반환")
        void getTtl_nonExistingKey_returnsNull() {
            // Given
            TestCacheKey key = new TestCacheKey("non-existing-ttl-string");

            // When
            Duration ttl = cacheAdapter.getTtl(key);

            // Then
            assertThat(ttl).isNull();
        }
    }
}
