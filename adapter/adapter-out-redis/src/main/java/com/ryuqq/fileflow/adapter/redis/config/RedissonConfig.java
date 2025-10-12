package com.ryuqq.fileflow.adapter.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * Redisson Configuration
 *
 * 분산 락을 위한 Redisson Client 설정을 제공합니다.
 * 다중 서버 환경에서 Redis KeyExpiredEvent 중복 처리를 방지하기 위해 사용됩니다.
 *
 * RedissonProperties를 통해 커넥션 풀 및 타임아웃 설정을 외부화하여 관리합니다.
 *
 * @author sangwon-ryu
 */
@Configuration
public class RedissonConfig {

    private static final Logger log = LoggerFactory.getLogger(RedissonConfig.class);

    private final RedissonProperties redissonProperties;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    /**
     * Constructor Injection
     *
     * @param redissonProperties Redisson 설정 Properties
     */
    public RedissonConfig(RedissonProperties redissonProperties) {
        this.redissonProperties = Objects.requireNonNull(
                redissonProperties,
                "RedissonProperties must not be null"
        );
    }

    /**
     * RedissonClient Bean 생성
     *
     * RedissonProperties에서 주입받은 설정값을 사용하여 클라이언트를 구성합니다.
     *
     * @return RedissonClient
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // Single Server Configuration
        String address = String.format("redis://%s:%d", redisHost, redisPort);
        config.useSingleServer()
                .setAddress(address)
                .setPassword(redisPassword.isEmpty() ? null : redisPassword)
                .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize())
                .setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout())
                .setConnectTimeout(redissonProperties.getConnectTimeout())
                .setTimeout(redissonProperties.getTimeout())
                .setRetryAttempts(redissonProperties.getRetryAttempts())
                .setRetryInterval(redissonProperties.getRetryInterval());

        log.info("Redisson client configured with address: {}, poolSize: {}, timeout: {}ms",
                address,
                redissonProperties.getConnectionPoolSize(),
                redissonProperties.getTimeout());

        return Redisson.create(config);
    }
}
