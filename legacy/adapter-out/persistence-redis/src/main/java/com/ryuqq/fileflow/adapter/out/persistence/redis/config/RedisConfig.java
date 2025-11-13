package com.ryuqq.fileflow.adapter.out.persistence.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration - RedisTemplate 설정
 *
 * <p>Redis 연결 및 직렬화 설정을 담당합니다.</p>
 *
 * <p><strong>핵심 설정:</strong></p>
 * <ul>
 *   <li>RedisTemplate: Key-Value 기반 캐시 작업</li>
 *   <li>Key Serializer: StringRedisSerializer (UTF-8 문자열)</li>
 *   <li>Value Serializer: GenericJackson2JsonRedisSerializer (JSON 형식)</li>
 *   <li>ObjectMapper: Java 8 Time API 지원 (LocalDateTime 등)</li>
 * </ul>
 *
 * <p><strong>직렬화 전략:</strong></p>
 * <ul>
 *   <li>✅ Key는 문자열 (간단하고 읽기 쉬움)</li>
 *   <li>✅ Value는 JSON (유연하고 디버깅 쉬움)</li>
 *   <li>✅ Java 8 Time API 지원 (LocalDateTime, Instant 등)</li>
 *   <li>✅ 타입 정보 포함 (@class 필드) → 역직렬화 시 타입 안전성</li>
 * </ul>
 *
 * <p><strong>연결 설정:</strong></p>
 * <ul>
 *   <li>application.yml에서 호스트/포트 설정</li>
 *   <li>spring.data.redis.host (기본값: localhost)</li>
 *   <li>spring.data.redis.port (기본값: 6379)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate Bean 생성
     *
     * <p>Redis 작업을 위한 핵심 Bean입니다.</p>
     *
     * <p><strong>설정 내용:</strong></p>
     * <ul>
     *   <li>Connection Factory: Spring Boot Auto-Configuration에서 주입</li>
     *   <li>Key Serializer: StringRedisSerializer (UTF-8)</li>
     *   <li>Value Serializer: GenericJackson2JsonRedisSerializer (JSON)</li>
     *   <li>Hash Key/Value Serializer: 동일하게 설정 (Hash 자료구조용)</li>
     *   <li>ObjectMapper: Redis 전용 (타입 힌트 활성화)</li>
     * </ul>
     *
     * <p><strong>사용 예시:</strong></p>
     * <pre>{@code
     * @Autowired
     * private RedisTemplate<String, Object> redisTemplate;
     *
     * // 저장
     * redisTemplate.opsForValue().set("key", value, Duration.ofMinutes(5));
     *
     * // 조회
     * Object value = redisTemplate.opsForValue().get("key");
     *
     * // 삭제
     * redisTemplate.delete("key");
     * }</pre>
     *
     * @param connectionFactory Redis 연결 팩토리 (Auto-Configuration)
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

        // Value Serializer: JSON (Jackson) with Redis-specific ObjectMapper + type hints
        // Must configure ObjectMapper with JavaTimeModule AND activateDefaultTyping
        GenericJackson2JsonRedisSerializer valueSerializer =
            new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis 전용 ObjectMapper 생성 (타입 힌트 활성화)
     *
     * <p>Redis Value 직렬화를 위한 Redis 전용 ObjectMapper를 생성합니다.</p>
     * <p>전역 ObjectMapper와 다르게 타입 힌트가 활성화되어 있습니다.</p>
     *
     * <p><strong>전역 ObjectMapper vs Redis ObjectMapper 차이:</strong></p>
     * <ul>
     *   <li>전역 ObjectMapper (CoreConfiguration): 타입 힌트 없음 → REST API, JSON 검증 등</li>
     *   <li>Redis ObjectMapper (이 메서드): 타입 힌트 활성화 → Redis 캐시 직렬화</li>
     * </ul>
     *
     * <p><strong>Redis 전용 설정:</strong></p>
     * <ul>
     *   <li>activateDefaultTyping: NON_FINAL 타입에 @class 타입 힌트 추가</li>
     *   <li>Clock MixIn: Domain 객체의 Clock 필드 무시</li>
     *   <li>필수 이유: GenericJackson2JsonRedisSerializer가 역직렬화 시 타입 정보 필요</li>
     * </ul>
     *
     * <p><strong>타입 힌트 예시:</strong></p>
     * <pre>{@code
     * // Redis에 저장될 JSON (타입 힌트 포함):
     * {
     *   "@class": "com.ryuqq.fileflow.adapter.out.persistence.redis.dto.CachedSetting",
     *   "id": 1,
     *   "key": "theme",
     *   "value": "dark"
     * }
     * }</pre>
     *
     * @return Redis 전용 ObjectMapper (타입 힌트 활성화)
     * @author ryu-qqq
     * @since 2025-10-29
     */
    private ObjectMapper redisObjectMapper() {
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
     *
     * <p>Setting 도메인 객체의 Clock 필드를 직렬화에서 제외합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @com.fasterxml.jackson.annotation.JsonIgnoreType
    interface IgnoreTypeMixin {
    }
}
