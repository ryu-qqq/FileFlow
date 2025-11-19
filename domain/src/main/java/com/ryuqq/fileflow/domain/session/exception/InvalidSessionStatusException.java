package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;

/**
 * 유효하지 않은 세션 상태 전환 예외.
 */
public class InvalidSessionStatusException extends DomainException {

    public InvalidSessionStatusException(SessionStatus currentStatus, SessionStatus requestedStatus) {
        super(
            SessionErrorCode.INVALID_SESSION_STATUS,
            String.format(
                "세션 상태 전환이 불가능합니다. (현재: %s, 요청: %s)",
                currentStatus,
                requestedStatus
            )
        );
    }
}

