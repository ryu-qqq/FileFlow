package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;

/**
 * 유효하지 않은 세션 상태 전환 예외.
 *
 * <p>세션 상태 전환이 규칙에 맞지 않는 경우 발생합니다. 허용되는 전환: PREPARING → ACTIVE, ACTIVE → {COMPLETED, EXPIRED,
 * FAILED}
 *
 * <p><strong>에러 코드</strong>: INVALID_SESSION_STATUS
 *
 * <p><strong>HTTP 상태</strong>: 409 Conflict
 */
public class InvalidSessionStatusException extends DomainException {

    /**
     * InvalidSessionStatusException 생성자
     *
     * @param currentStatus 현재 세션 상태
     * @param requestedStatus 요청된 세션 상태
     */
    public InvalidSessionStatusException(
            SessionStatus currentStatus, SessionStatus requestedStatus) {
        super(
                SessionErrorCode.INVALID_SESSION_STATUS,
                String.format(
                        "세션 상태 전환이 불가능합니다. (현재: %s, 요청: %s)", currentStatus, requestedStatus));
    }
}
