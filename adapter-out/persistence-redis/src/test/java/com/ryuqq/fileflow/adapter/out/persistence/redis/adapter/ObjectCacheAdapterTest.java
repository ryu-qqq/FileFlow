package com.ryuqq.fileflow.adapter.out.persistence.redis.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.redis.common.CacheTestSupport;
import com.ryuqq.fileflow.adapter.out.persistence.redis.common.TestCacheKey;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ObjectCacheAdapter 통합 테스트
 *
 * <p>Redis를 사용한 Object 캐시 기능을 검증합니다.
 */
@DisplayName("ObjectCacheAdapter 통합 테스트")
class ObjectCacheAdapterTest extends CacheTestSupport {

    @Autowired private ObjectCacheAdapter cacheAdapter;

    @Nested
    @DisplayName("set 메서드")
    class SetMethod {

        @Test
        @DisplayName("성공 - 기본 TTL로 캐시 저장")
        void set_withDefaultTtl_success() {
            // Given
            TestCacheKey key = new TestCacheKey("object-1");
            TestObject value = new TestObject("test-name", 100);

            // When
            cacheAdapter.set(key, value);

            // Then
            assertCacheExists(key.value());
            assertTtlSet(key.value(), 1800, 60); // 기본 30분 TTL
        }

        @Test
        @DisplayName("성공 - 지정된 TTL로 캐시 저장")
        void set_withCustomTtl_success() {
            // Given
            TestCacheKey key = new TestCacheKey("object-2");
            TestObject value = new TestObject("test-name", 200);
            Duration ttl = Duration.ofMinutes(10);

            // When
            cacheAdapter.set(key, value, ttl);

            // Then
            assertCacheExists(key.value());
            assertTtlSet(key.value(), 600, 10); // 10분 TTL, 10초 오차
        }

        @Test
        @DisplayName("성공 - Map 타입 캐시 저장")
        void set_mapType_success() {
            // Given
            TestCacheKey key = new TestCacheKey("map-1");
            Map<String, Object> value = Map.of("name", "test", "count", 10);

            // When
            cacheAdapter.set(key, value, Duration.ofMinutes(5));

            // Then
            assertCacheExists(key.value());
        }
    }

    @Nested
    @DisplayName("get 메서드")
    class GetMethod {

        @Test
        @DisplayName("성공 - 캐시 조회")
        void get_existingKey_success() {
            // Given
            TestCacheKey key = new TestCacheKey("get-1");
            TestObject value = new TestObject("retrieved", 300);
            cacheAdapter.set(key, value, Duration.ofMinutes(5));

            // When
            Optional<Object> result = cacheAdapter.get(key);

            // Then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 키 조회 시 빈 Optional 반환")
        void get_nonExistingKey_returnsEmpty() {
            // Given
            TestCacheKey key = new TestCacheKey("non-existing");

            // When
            Optional<Object> result = cacheAdapter.get(key);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAs 메서드")
    class GetAsMethod {

        @Test
        @DisplayName("성공 - 타입 변환 조회")
        void getAs_withType_success() {
            // Given
            TestCacheKey key = new TestCacheKey("typed-1");
            TestObject value = new TestObject("typed", 400);
            cacheAdapter.set(key, value, Duration.ofMinutes(5));

            // When
            Optional<TestObject> result = cacheAdapter.getAs(key, TestObject.class);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("typed");
            assertThat(result.get().count()).isEqualTo(400);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 키 조회 시 빈 Optional 반환")
        void getAs_nonExistingKey_returnsEmpty() {
            // Given
            TestCacheKey key = new TestCacheKey("typed-non-existing");

            // When
            Optional<TestObject> result = cacheAdapter.getAs(key, TestObject.class);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - 문자열 타입 변환 조회")
        void getAs_stringType_success() {
            // Given
            TestCacheKey key = new TestCacheKey("string-1");
            String value = "plain string value";
            cacheAdapter.set(key, value, Duration.ofMinutes(5));

            // When
            Optional<String> result = cacheAdapter.getAs(key, String.class);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("plain string value");
        }
    }

    @Nested
    @DisplayName("evict 메서드")
    class EvictMethod {

        @Test
        @DisplayName("성공 - 캐시 삭제")
        void evict_existingKey_success() {
            // Given
            TestCacheKey key = new TestCacheKey("evict-1");
            cacheAdapter.set(key, "to-delete", Duration.ofMinutes(5));
            assertCacheExists(key.value());

            // When
            cacheAdapter.evict(key);

            // Then
            assertCacheNotExists(key.value());
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 키 삭제 시 예외 없음")
        void evict_nonExistingKey_noException() {
            // Given
            TestCacheKey key = new TestCacheKey("non-existing-evict");

            // When & Then - 예외 발생하지 않음
            cacheAdapter.evict(key);
            assertCacheNotExists(key.value());
        }
    }

    @Nested
    @DisplayName("evictByPattern 메서드")
    class EvictByPatternMethod {

        @Test
        @DisplayName("성공 - 패턴으로 여러 캐시 삭제")
        void evictByPattern_multipleKeys_success() {
            // Given
            cacheAdapter.set(new TestCacheKey("pattern-1"), "value1", Duration.ofMinutes(5));
            cacheAdapter.set(new TestCacheKey("pattern-2"), "value2", Duration.ofMinutes(5));
            cacheAdapter.set(new TestCacheKey("other-1"), "value3", Duration.ofMinutes(5));

            assertAllCachesExist(
                    "test::cache::pattern-1", "test::cache::pattern-2", "test::cache::other-1");

            // When
            cacheAdapter.evictByPattern("test::cache::pattern-*");

            // Then
            assertNoCachesExist("test::cache::pattern-1", "test::cache::pattern-2");
            assertCacheExists("test::cache::other-1");
        }

        @Test
        @DisplayName("성공 - 매칭 키가 없어도 예외 없음")
        void evictByPattern_noMatch_noException() {
            // Given & When & Then
            cacheAdapter.evictByPattern("non-existing-pattern::*");
        }
    }

    @Nested
    @DisplayName("exists 메서드")
    class ExistsMethod {

        @Test
        @DisplayName("성공 - 존재하는 키 확인")
        void exists_existingKey_returnsTrue() {
            // Given
            TestCacheKey key = new TestCacheKey("exists-1");
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
            TestCacheKey key = new TestCacheKey("non-exists");

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
        @DisplayName("성공 - TTL 조회")
        void getTtl_existingKey_returnsTtl() {
            // Given
            TestCacheKey key = new TestCacheKey("ttl-1");
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
            TestCacheKey key = new TestCacheKey("non-existing-ttl");

            // When
            Duration ttl = cacheAdapter.getTtl(key);

            // Then
            assertThat(ttl).isNull();
        }
    }

    /** 테스트용 객체 */
    public record TestObject(String name, int count) {}
}
