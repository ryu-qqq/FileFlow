package com.ryuqq.fileflow.domain.session.vo;

public class SessionLockKeyFixture {

    public static SessionLockKey aSessionLockKey() {
        return new SessionLockKey("session-001");
    }

    public static SessionLockKey aSessionLockKey(String sessionId) {
        return new SessionLockKey(sessionId);
    }
}
