package com.ryuqq.fileflow.domain.session.vo;

import java.time.Duration;

public class SessionExpirationFixture {

    public static SessionExpiration aSingleSessionExpiration() {
        return SessionExpiration.of("single-session-001", "SINGLE", Duration.ofHours(1));
    }

    public static SessionExpiration aMultipartSessionExpiration() {
        return SessionExpiration.of("multipart-session-001", "MULTIPART", Duration.ofHours(1));
    }

    public static SessionExpiration aSessionExpiration(
            String sessionId, String sessionType, Duration ttl) {
        return SessionExpiration.of(sessionId, sessionType, ttl);
    }
}
