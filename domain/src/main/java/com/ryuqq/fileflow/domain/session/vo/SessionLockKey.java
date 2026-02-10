package com.ryuqq.fileflow.domain.session.vo;

import com.ryuqq.fileflow.domain.common.vo.LockKey;

public record SessionLockKey(String sessionId) implements LockKey {

    private static final String PREFIX = "lock:session:expire:";

    public SessionLockKey {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
    }

    @Override
    public String value() {
        return PREFIX + sessionId;
    }
}
