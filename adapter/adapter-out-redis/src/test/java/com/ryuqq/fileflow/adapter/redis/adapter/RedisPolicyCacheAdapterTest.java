package com.ryuqq.fileflow.adapter.redis.adapter;

import com.ryuqq.fileflow.adapter.redis.config.RedisConfig;
import com.ryuqq.fileflow.adapter.redis.dto.UploadPolicyDto;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/**
 * RedisPolicyCacheAdapter 통합 테스트
 *
 * Testcontainers Redis를 사용하여 실제 Redis 환경에서 테스트합니다.
 * - 캐시 저장/조회/삭제 검증
 * - TTL(1시간) 만료 검증
 * - 캐시 무효화 검증
 * - 커버리지 70% 이상 달성 목표
 *
 * @author sangwon-ryu
 */
@SpringBootTest(classes = {
        RedisConfig.class,
        RedisPolicyCacheAdapter.class,
        RedisPolicyCacheAdapterTest.TestRedisConfig.class
})
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
        value = "UwF",
        justification = "testPolicyKey와 testPolicy 필드는 @BeforeEach setUp()에서 초기화됩니다."
)
class RedisPolicyCacheAdapterTest {

    @TestConfiguration
    static class TestRedisConfig {
        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            LettuceConnectionFactory factory = new LettuceConnectionFactory(
                    REDIS_CONTAINER.getHost(),
                    REDIS_CONTAINER.getFirstMappedPort()
            );
            factory.afterPropertiesSet();
            return factory;
        }
    }

    private static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @BeforeAll
    static void startContainer() {
        REDIS_CONTAINER.start();
    }

    @AfterAll
    static void stopContainer() {
        REDIS_CONTAINER.stop();
    }

    @Autowired
    private RedisPolicyCacheAdapter adapter;

    @Autowired
    private RedisTemplate<String, UploadPolicyDto> redisTemplate;

    private PolicyKey testPolicyKey;
    private UploadPolicy testPolicy;

    @BeforeEach
    void setUp() {
        // Redis 전체 캐시 삭제
        java.util.Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        testPolicyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");

        FileTypePolicies fileTypePolicies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null,
                null,
                null
        );

        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        testPolicy = UploadPolicy.create(
                testPolicyKey,
                fileTypePolicies,
                rateLimiting,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );
    }

    @Test
    @DisplayName("정책을 캐시에 저장하고 조회할 수 있다")
    void put_and_get_policy() {
        // when
        adapter.put(testPolicy);

        // then
        Optional<UploadPolicy> cached = adapter.get(testPolicyKey);
        assertThat(cached).isPresent();
        assertThat(cached.get().getPolicyKey()).isEqualTo(testPolicyKey);
        assertThat(cached.get().getVersion()).isEqualTo(testPolicy.getVersion());
    }

    @Test
    @DisplayName("캐시 미스 시 Optional.empty()를 반환한다")
    void get_returns_empty_when_cache_miss() {
        // given
        PolicyKey nonExistentKey = PolicyKey.of("b2b", "MERCHANT", "PRODUCT");

        // when
        Optional<UploadPolicy> cached = adapter.get(nonExistentKey);

        // then
        assertThat(cached).isEmpty();
    }

    @Test
    @DisplayName("캐시된 정책을 무효화할 수 있다")
    void evict_policy() {
        // given
        adapter.put(testPolicy);
        assertThat(adapter.get(testPolicyKey)).isPresent();

        // when
        adapter.evict(testPolicyKey);

        // then
        assertThat(adapter.get(testPolicyKey)).isEmpty();
    }

    @Test
    @DisplayName("모든 정책 캐시를 무효화할 수 있다")
    void evict_all_policies() {
        // given
        PolicyKey key1 = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        PolicyKey key2 = PolicyKey.of("b2b", "MERCHANT", "PRODUCT");

        UploadPolicy policy1 = createTestPolicy(key1);
        UploadPolicy policy2 = createTestPolicy(key2);

        adapter.put(policy1);
        adapter.put(policy2);

        assertThat(adapter.get(key1)).isPresent();
        assertThat(adapter.get(key2)).isPresent();

        // when
        adapter.evictAll();

        // then
        assertThat(adapter.get(key1)).isEmpty();
        assertThat(adapter.get(key2)).isEmpty();
    }

    @Test
    @DisplayName("캐시는 1시간(3600초) TTL을 가진다")
    void cache_has_ttl_of_one_hour() {
        // when
        adapter.put(testPolicy);

        // then
        String redisKey = "policy:" + testPolicyKey.getValue();
        Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

        assertThat(ttl).isNotNull();
        assertThat(ttl).isBetween(3590L, 3600L); // TTL은 약간의 지연 허용
    }

    @Test
    @DisplayName("TTL 만료 시 캐시가 자동으로 삭제된다")
    void cache_expires_after_ttl() {
        // given
        adapter.put(testPolicy);
        assertThat(adapter.get(testPolicyKey)).isPresent();

        // 테스트를 위해 TTL을 1초로 재설정
        String redisKey = "policy:" + testPolicyKey.getValue();
        redisTemplate.expire(redisKey, 1, TimeUnit.SECONDS);

        // when & then
        await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(adapter.get(testPolicyKey)).isEmpty()
                );
    }

    @Test
    @DisplayName("null PolicyKey로 조회 시 IllegalArgumentException을 던진다")
    void get_throws_exception_when_null_policy_key() {
        assertThatThrownBy(() -> adapter.get(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PolicyKey cannot be null");
    }

    @Test
    @DisplayName("null UploadPolicy 저장 시 IllegalArgumentException을 던진다")
    void put_throws_exception_when_null_upload_policy() {
        assertThatThrownBy(() -> adapter.put(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UploadPolicy cannot be null");
    }

    @Test
    @DisplayName("null PolicyKey로 무효화 시 IllegalArgumentException을 던진다")
    void evict_throws_exception_when_null_policy_key() {
        assertThatThrownBy(() -> adapter.evict(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PolicyKey cannot be null");
    }

    private UploadPolicy createTestPolicy(PolicyKey policyKey) {
        FileTypePolicies fileTypePolicies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null,
                null,
                null
        );

        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        return UploadPolicy.create(
                policyKey,
                fileTypePolicies,
                rateLimiting,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );
    }
}
