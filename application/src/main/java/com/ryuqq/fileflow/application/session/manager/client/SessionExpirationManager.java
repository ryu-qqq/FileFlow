package com.ryuqq.fileflow.application.session.manager.client;

import com.ryuqq.fileflow.application.session.port.out.client.SessionExpirationClient;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import org.springframework.stereotype.Component;

@Component
public class SessionExpirationManager {

    private final SessionExpirationClient sessionExpirationClient;

    public SessionExpirationManager(SessionExpirationClient sessionExpirationClient) {
        this.sessionExpirationClient = sessionExpirationClient;
    }

    public void registerExpiration(SessionExpiration expiration) {
        sessionExpirationClient.registerExpiration(expiration);
    }

    public void removeExpiration(String sessionType, String sessionId) {
        sessionExpirationClient.removeExpiration(sessionType, sessionId);
    }
}
