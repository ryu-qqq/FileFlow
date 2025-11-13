package com.ryuqq.fileflow.adapter.out.persistence.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Redis Integration Test Base Class
 *
 * <p><strong>역할</strong>: TestContainers 기반 Redis 통합 테스트 기본 클래스</p>
 *
 * <h3>제공 기능</h3>
 * <ul>
 *   <li>✅ TestContainers Redis 7.2 자동 시작</li>
 *   <li>✅ Spring Data Redis 자동 설정</li>
 *   <li>✅ RedisTemplate 자동 주입</li>
 *   <li>✅ 테스트 간 데이터 격리 (각 테스트 후 Redis FLUSHALL)</li>
 * </ul>
 *
 * <h3>사용 방법</h3>
 * <pre>{@code
 * class EffectiveGrantsCacheAdapterTest extends RedisIntegrationTestBase {
 *     // 테스트 작성
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = {RedisIntegrationTestBase.TestRedisConfiguration.class})
public abstract class RedisIntegrationTestBase {

    /**
     * Redis 7.2 TestContainer
     *
     * <p>모든 테스트 클래스에서 공유되는 싱글톤 컨테이너입니다.</p>
     */
    @Container
    protected static final RedisContainer REDIS_CONTAINER = new RedisContainer(
        DockerImageName.parse("redis:7.2-alpine")
    ).withReuse(true);

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    /**
     * TestContainers Redis 설정을 Spring에 동적으로 주입
     *
     * @param registry Spring Dynamic Property Registry
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
    }

    /**
     * 각 테스트 후 Redis 초기화
     *
     * <p>테스트 간 데이터 격리를 보장합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @AfterEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    /**
     * Test Redis Configuration
     *
     * <p>테스트용 RedisTemplate Bean 설정</p>
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Configuration
    static class TestRedisConfiguration {

        /**
         * RedisConnectionFactory Bean 생성
         *
         * @return RedisConnectionFactory (Lettuce 기반)
         * @author ryu-qqq
         * @since 2025-10-26
         */
        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory factory =
                new org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory(
                    REDIS_CONTAINER.getHost(),
                    REDIS_CONTAINER.getMappedPort(6379)
                );
            factory.afterPropertiesSet();
            return factory;
        }

        /**
         * RedisTemplate Bean 생성
         *
         * @param connectionFactory Redis 연결 팩토리
         * @return RedisTemplate&lt;String, Object&gt;
         * @author ryu-qqq
         * @since 2025-10-26
         */
        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);

            // Key Serializer: 문자열 (UTF-8)
            StringRedisSerializer keySerializer = new StringRedisSerializer();
            template.setKeySerializer(keySerializer);
            template.setHashKeySerializer(keySerializer);

            // Value Serializer: JSON (Jackson) with custom ObjectMapper + type hints
            // Must configure ObjectMapper with JavaTimeModule AND activateDefaultTyping
            GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper());
            template.setValueSerializer(valueSerializer);
            template.setHashValueSerializer(valueSerializer);

            template.afterPropertiesSet();
            return template;
        }

        /**
         * ObjectMapper Bean 생성 (JSON 직렬화용)
         *
         * <p><strong>⚠️ IMPORTANT: Type Hints for Redis</strong></p>
         * <ul>
         *   <li>activateDefaultTyping() 필수 - @class 타입 힌트 포함</li>
         *   <li>타입 힌트 없으면 역직렬화 시 LinkedHashMap으로 변환됨</li>
         * </ul>
         *
         * @return ObjectMapper (Java 8 Time 지원 + 타입 힌트)
         * @author ryu-qqq
         * @since 2025-10-26
         */
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();

            // Java 8 Time API 지원
            mapper.registerModule(new JavaTimeModule());

            // 날짜를 ISO-8601 문자열로 저장 (타임스탬프 숫자 대신)
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Clock 필드 무시 (직렬화 불가능한 타입)
            mapper.addMixIn(java.time.Clock.class, IgnoreTypeMixin.class);

            // 역직렬화 실패 무시 - DTO 패턴 사용으로 Domain 객체는 직접 역직렬화하지 않음
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // ⚠️ Redis 타입 힌트 활성화 - CRITICAL for GenericJackson2JsonRedisSerializer
            // NON_FINAL: All non-final types get @class type hints
            // - Required for DTO deserialization (CachedSetting, CachedSettingsForMerge)
            // - List<String> also gets type hints but Jackson handles it correctly
            mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
            );

            return mapper;
        }

        /**
         * Clock 타입을 무시하기 위한 MixIn 인터페이스
         */
        @com.fasterxml.jackson.annotation.JsonIgnoreType
        interface IgnoreTypeMixin {
        }
    }
}
