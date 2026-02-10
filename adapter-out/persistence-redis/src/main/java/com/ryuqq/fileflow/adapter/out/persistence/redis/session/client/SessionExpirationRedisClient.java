package com.ryuqq.fileflow.adapter.out.persistence.redis.session.client;

import com.ryuqq.fileflow.adapter.out.persistence.redis.session.dto.SessionExpirationRedisData;
import com.ryuqq.fileflow.adapter.out.persistence.redis.session.mapper.SessionExpirationRedisMapper;
import com.ryuqq.fileflow.application.session.port.out.client.SessionExpirationClient;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class SessionExpirationRedisClient implements SessionExpirationClient {

    private static final Logger log = LoggerFactory.getLogger(SessionExpirationRedisClient.class);

    private final StringRedisTemplate redisTemplate;
    private final SessionExpirationRedisMapper mapper;

    public SessionExpirationRedisClient(
            StringRedisTemplate redisTemplate, SessionExpirationRedisMapper mapper) {
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
    }

    @Override
    public void registerExpiration(SessionExpiration expiration) {
        SessionExpirationRedisData data = mapper.toRedisData(expiration);

        log.info(
                "세션 만료 키 등록: key={}, sessionType={}, ttl={}", data.key(), data.value(), data.ttl());

        redisTemplate.opsForValue().set(data.key(), data.value(), data.ttl());
    }

    @Override
    public void removeExpiration(String sessionType, String sessionId) {
        String key = mapper.buildKey(sessionType, sessionId);

        log.info("세션 만료 키 삭제: key={}", key);

        redisTemplate.delete(key);
    }
}
