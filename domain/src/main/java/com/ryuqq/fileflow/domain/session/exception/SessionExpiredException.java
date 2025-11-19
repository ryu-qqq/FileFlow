package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

import java.time.LocalDateTime;

/**
 * 세션 만료 예외.
 */
public class SessionExpiredException extends DomainException {

    public SessionExpiredException(LocalDateTime expiresAt) {
        super(
            SessionErrorCode.SESSION_EXPIRED,
            String.format(
                "세션이 만료되었습니다. (만료 시각: %s)",
                expiresAt
            )
        );
    }
}

