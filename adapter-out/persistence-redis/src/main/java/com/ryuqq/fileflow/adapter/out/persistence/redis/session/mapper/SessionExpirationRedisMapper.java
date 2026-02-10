package com.ryuqq.fileflow.adapter.out.persistence.redis.session.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.redis.session.dto.SessionExpirationRedisData;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import org.springframework.stereotype.Component;

@Component
public class SessionExpirationRedisMapper {

    private static final String KEY_PREFIX = "session:expiration:";

    public SessionExpirationRedisData toRedisData(SessionExpiration expiration) {
        return new SessionExpirationRedisData(
                buildKey(expiration.sessionType(), expiration.sessionId()),
                expiration.sessionType(),
                expiration.ttl());
    }

    public String buildKey(String sessionType, String sessionId) {
        return KEY_PREFIX + sessionType + ":" + sessionId;
    }
}
