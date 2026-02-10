package com.ryuqq.fileflow.domain.session.vo;

import java.time.Duration;
import java.util.Objects;

/**
 * 세션 만료 정보 Value Object.
 *
 * <p>세션 만료 키 등록에 필요한 정보를 묶습니다.
 *
 * @param sessionId 세션 ID
 * @param sessionType 세션 유형 ("SINGLE" 또는 "MULTIPART")
 * @param ttl 만료까지의 기간
 */
public record SessionExpiration(String sessionId, String sessionType, Duration ttl) {

    public SessionExpiration {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(sessionType, "sessionType must not be null");
        Objects.requireNonNull(ttl, "ttl must not be null");
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
    }

    public static SessionExpiration of(String sessionId, String sessionType, Duration ttl) {
        return new SessionExpiration(sessionId, sessionType, ttl);
    }
}
