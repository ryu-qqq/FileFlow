package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.time.LocalDateTime;

/**
 * 세션 만료 예외.
 *
 * <p>업로드 세션이 만료된 후 접근하려는 경우 발생합니다. 세션은 생성 후 15분 후에 만료됩니다.
 *
 * <p><strong>에러 코드</strong>: SESSION_EXPIRED
 *
 * <p><strong>HTTP 상태</strong>: 410 Gone
 */
public class SessionExpiredException extends DomainException {

    /**
     * SessionExpiredException 생성자
     *
     * @param expiresAt 세션 만료 시각
     */
    public SessionExpiredException(LocalDateTime expiresAt) {
        super(
                SessionErrorCode.SESSION_EXPIRED,
                String.format("세션이 만료되었습니다. (만료 시각: %s)", expiresAt));
    }
}
