package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;

/**
 * 세션을 찾을 수 없는 경우 발생하는 예외.
 *
 * <p>세션 ID로 세션을 조회했으나 존재하지 않는 경우 발생합니다.
 */
public class SessionNotFoundException extends DomainException {

    /**
     * 세션 ID를 포함한 예외를 생성합니다.
     *
     * @param sessionId 찾을 수 없는 세션 ID
     */
    public SessionNotFoundException(UploadSessionId sessionId) {
        super(
                SessionErrorCode.SESSION_NOT_FOUND,
                String.format("세션을 찾을 수 없습니다: %s", sessionId.value()));
    }

    /**
     * 세션 ID 문자열을 포함한 예외를 생성합니다.
     *
     * @param sessionIdString 찾을 수 없는 세션 ID 문자열
     */
    public SessionNotFoundException(String sessionIdString) {
        super(
                SessionErrorCode.SESSION_NOT_FOUND,
                String.format("세션을 찾을 수 없습니다: %s", sessionIdString));
    }
}
