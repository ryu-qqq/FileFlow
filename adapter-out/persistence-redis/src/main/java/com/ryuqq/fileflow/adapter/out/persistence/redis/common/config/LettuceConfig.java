package com.ryuqq.fileflow.adapter.out.persistence.redis.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import java.time.Duration;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Lettuce Redis 연결 설정
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Redis 연결 설정 (Connection Pool)
 *   <li>RedisTemplate Serializer 설정
 *   <li>ClientResources 관리 (Netty Event Loop)
 * </ul>
 *
 * <p><strong>용도:</strong>
 *
 * <ul>
 *   <li>캐싱 (@Cacheable, @CacheEvict)
 *   <li>세션 관리 (Spring Session)
 *   <li>단순 K-V 저장/조회
 * </ul>
 *
 * <p><strong>주의:</strong> 분산락은 Redisson 사용 (RedissonConfig 참조)
 *
 * @author Development Team
 * @since 1.0.0
 * @see RedissonConfig
 */
@Configuration
@ConditionalOnProperty(
        name = "spring.data.redis.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class LettuceConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Value("${spring.data.redis.timeout:3000ms}")
    private Duration timeout;

    @Value("${spring.data.redis.lettuce.pool.max-active:16}")
    private int maxActive;

    @Value("${spring.data.redis.lettuce.pool.max-idle:8}")
    private int maxIdle;

    @Value("${spring.data.redis.lettuce.pool.min-idle:4}")
    private int minIdle;

    @Value("${spring.data.redis.lettuce.pool.max-wait:2000ms}")
    private Duration maxWait;

    /**
     * ClientResources 설정 (Netty Event Loop 공유)
     *
     * <p>모든 Redis 연결에서 공유하여 리소스 효율성 향상
     *
     * @return ClientResources 인스턴스
     */
    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return DefaultClientResources.builder()
                .ioThreadPoolSize(4)
                .computationThreadPoolSize(4)
                .build();
    }

    /**
     * Connection Pool 설정
     *
     * <p>Pool Size 권장값:
     *
     * <ul>
     *   <li>Local: max-active=8, max-idle=8, min-idle=2
     *   <li>Dev/QA: max-active=12, max-idle=8, min-idle=4
     *   <li>Prod: max-active=16~32, max-idle=8~16, min-idle=4~8
     * </ul>
     *
     * @return GenericObjectPoolConfig 인스턴스
     */
    @SuppressWarnings("rawtypes")
    private GenericObjectPoolConfig poolConfig() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(maxActive);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setMaxWait(maxWait);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        config.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        return config;
    }

    /**
     * Lettuce ClientOptions 설정
     *
     * @return ClientOptions 인스턴스
     */
    private ClientOptions clientOptions() {
        return ClientOptions.builder()
                .socketOptions(
                        SocketOptions.builder()
                                .connectTimeout(Duration.ofMillis(3000))
                                .keepAlive(true)
                                .build())
                .timeoutOptions(TimeoutOptions.enabled(timeout))
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build();
    }

    /**
     * RedisConnectionFactory 생성
     *
     * @param clientResources Netty 리소스
     * @return LettuceConnectionFactory 인스턴스
     */
    @Bean
    @SuppressWarnings("unchecked")
    public RedisConnectionFactory redisConnectionFactory(ClientResources clientResources) {

        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName(host);
        serverConfig.setPort(port);
        serverConfig.setDatabase(database);
        if (password != null && !password.isEmpty()) {
            serverConfig.setPassword(password);
        }

        LettuceClientConfiguration clientConfig =
                LettucePoolingClientConfiguration.builder()
                        .clientResources(clientResources)
                        .clientOptions(clientOptions())
                        .poolConfig(poolConfig())
                        .commandTimeout(timeout)
                        .build();

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    /**
     * RedisTemplate 설정
     *
     * <p>Serializer 설정:
     *
     * <ul>
     *   <li>Key: StringRedisSerializer (가독성)
     *   <li>Value: GenericJackson2JsonRedisSerializer (타입 정보 포함)
     * </ul>
     *
     * @param connectionFactory RedisConnectionFactory
     * @param redisObjectMapper Redis용 ObjectMapper
     * @return RedisTemplate 인스턴스
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value Serializer (JSON with Type Info)
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer(redisObjectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis용 ObjectMapper 설정
     *
     * <p>Java 8 날짜/시간 지원 및 타입 정보를 포함합니다.
     *
     * @return ObjectMapper 인스턴스
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 날짜/시간 모듈
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 타입 정보 포함 (역직렬화 시 정확한 타입 복원)
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        return objectMapper;
    }

    /**
     * JSON Serializer 생성 (타입 정보 포함)
     *
     * @param objectMapper Redis용 ObjectMapper
     * @return GenericJackson2JsonRedisSerializer 인스턴스
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer(ObjectMapper objectMapper) {
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
