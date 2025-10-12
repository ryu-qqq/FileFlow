package com.ryuqq.fileflow.adapter.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryuqq.fileflow.adapter.redis.dto.UploadPolicyDto;
import com.ryuqq.fileflow.adapter.redis.dto.UploadSessionDto;
import com.ryuqq.fileflow.adapter.redis.serializer.DimensionDeserializer;
import com.ryuqq.fileflow.adapter.redis.serializer.FileTypePoliciesDeserializer;
import com.ryuqq.fileflow.domain.policy.vo.Dimension;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import jakarta.annotation.PostConstruct;

/**
 * Redis Configuration
 *
 * RedisTemplate 설정을 제공합니다.
 * - UploadPolicyDto: 정책 캐시용
 * - UploadSessionDto: 세션 TTL 관리용
 *
 * 직렬화 전략:
 * - Key Serializer: StringRedisSerializer (UTF-8)
 * - Value Serializer: Jackson2JsonRedisSerializer (JSON)
 *
 * @author sangwon-ryu
 */
@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * Constructor Injection
     */
    public RedisConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    /**
     * Redis KeyspaceNotification 활성화
     * 'Ex' 설정: 만료 이벤트(expired)와 제거 이벤트(evicted) 활성화
     *
     * 설정 실패 시:
     * - TTL 기반 세션 만료 처리가 동작하지 않음
     * - Fallback 배치 스케줄러가 1시간마다 보상 처리 수행
     */
    @PostConstruct
    public void enableKeyspaceNotifications() {
        try (var connection = redisConnectionFactory.getConnection()) {
            connection.serverCommands()
                    .setConfig("notify-keyspace-events", "Ex");
            log.info("✅ Redis keyspace notifications enabled: Ex");
        } catch (Exception e) {
            log.error("""

                    ⚠️  CRITICAL: Failed to enable Redis keyspace notifications
                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    Reason: {}

                    Impact:
                      • TTL-based session expiration will NOT work
                      • Real-time session processing will be disabled
                      • Fallback batch scheduler will process expired sessions (1-hour delay)

                    Solution:
                      1. Add 'notify-keyspace-events Ex' to redis.conf
                      2. Or execute: redis-cli CONFIG SET notify-keyspace-events Ex
                      3. Ensure Redis user has CONFIG SET permission

                    Current Status: Using fallback batch processing only
                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    """, e.getMessage(), e);
        }
    }

    /**
     * RedisMessageListenerContainer Bean
     * KeyExpiredEvent를 수신하기 위한 컨테이너
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }

    /**
     * 기본 ObjectMapper 생성 헬퍼 메서드
     * JavaTimeModule과 기본 설정을 포함합니다.
     */
    private ObjectMapper createBaseObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    /**
     * RedisTemplate 생성 헬퍼 메서드
     * 제네릭을 사용하여 중복 코드를 제거합니다.
     */
    private <T> RedisTemplate<String, T> createRedisTemplate(
            RedisConnectionFactory connectionFactory,
            Class<T> valueType,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer: String (UTF-8)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value Serializer: JSON
        Jackson2JsonRedisSerializer<T> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, valueType);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * UploadPolicyDto용 RedisTemplate
     * 정책 캐시 용도로 사용됩니다.
     */
    @Bean
    public RedisTemplate<String, UploadPolicyDto> redisTemplate(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = createBaseObjectMapper();

        // 커스텀 디시리얼라이저 등록
        SimpleModule customModule = new SimpleModule();
        customModule.addDeserializer(FileTypePolicies.class, new FileTypePoliciesDeserializer());
        customModule.addDeserializer(Dimension.class, new DimensionDeserializer());
        objectMapper.registerModule(customModule);

        return createRedisTemplate(connectionFactory, UploadPolicyDto.class, objectMapper);
    }

    /**
     * UploadSessionDto용 RedisTemplate
     * TTL 기반 세션 만료 감지 용도로 사용됩니다.
     */
    @Bean
    public RedisTemplate<String, UploadSessionDto> uploadSessionRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        ObjectMapper objectMapper = createBaseObjectMapper();
        return createRedisTemplate(connectionFactory, UploadSessionDto.class, objectMapper);
    }
}
