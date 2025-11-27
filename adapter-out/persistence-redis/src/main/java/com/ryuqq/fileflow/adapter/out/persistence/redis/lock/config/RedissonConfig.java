package com.ryuqq.fileflow.adapter.out.persistence.redis.lock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 설정.
 *
 * <p>분산락 처리를 위한 Redisson Client Bean 설정.
 *
 * <p><strong>설정 항목</strong>:
 *
 * <ul>
 *   <li>Redis 서버 주소
 *   <li>커넥션 풀 크기
 *   <li>타임아웃 설정
 * </ul>
 */
@Configuration
public class RedissonConfig {

    private static final Logger log = LoggerFactory.getLogger(RedissonConfig.class);

    /**
     * Redisson Client Bean.
     *
     * @param host Redis 호스트
     * @param port Redis 포트
     * @param connectionPoolSize 커넥션 풀 최소 크기
     * @param connectionMinimumIdleSize 최소 유휴 커넥션 수
     * @param timeout 명령 타임아웃 (밀리초)
     * @param connectTimeout 연결 타임아웃 (밀리초)
     * @return RedissonClient
     */
    @Bean
    public RedissonClient redissonClient(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port,
            @Value("${redisson.connection-pool-size:16}") int connectionPoolSize,
            @Value("${redisson.connection-minimum-idle-size:4}") int connectionMinimumIdleSize,
            @Value("${redisson.timeout:3000}") int timeout,
            @Value("${redisson.connect-timeout:10000}") int connectTimeout) {

        log.info("Creating RedissonClient - host: {}, port: {}, poolSize: {}, minIdle: {}",
                host, port, connectionPoolSize, connectionMinimumIdleSize);

        try {
            Config config = new Config();
            config.useSingleServer()
                    .setAddress("redis://" + host + ":" + port)
                    .setConnectionPoolSize(connectionPoolSize)
                    .setConnectionMinimumIdleSize(connectionMinimumIdleSize)
                    .setTimeout(timeout)
                    .setConnectTimeout(connectTimeout);

            RedissonClient client = Redisson.create(config);
            log.info("RedissonClient created successfully");
            return client;
        } catch (Exception e) {
            log.error("Failed to create RedissonClient: {}", e.getMessage(), e);
            throw e;
        }
    }
}
