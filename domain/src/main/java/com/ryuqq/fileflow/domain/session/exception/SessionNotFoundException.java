package com.ryuqq.fileflow.domain.session.exception;

import java.util.Map;

public class SessionNotFoundException extends SessionException {

    public SessionNotFoundException(String sessionId) {
        super(
                SessionErrorCode.SESSION_NOT_FOUND,
                "세션을 찾을 수 없습니다. sessionId: " + sessionId,
                Map.of("sessionId", sessionId != null ? sessionId : "null"));
    }
}
