package com.ryuqq.fileflow.adapter.in.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis Consumer 설정 프로퍼티.
 *
 * @param sessionExpirationKeyPrefix 세션 만료 키 접두사 (기본값: "session:expiration:")
 */
@ConfigurationProperties(prefix = "fileflow.redis.consumer")
public record RedisConsumerProperties(String sessionExpirationKeyPrefix) {

    public RedisConsumerProperties {
        if (sessionExpirationKeyPrefix == null || sessionExpirationKeyPrefix.isBlank()) {
            sessionExpirationKeyPrefix = "session:expiration:";
        }
    }
}
