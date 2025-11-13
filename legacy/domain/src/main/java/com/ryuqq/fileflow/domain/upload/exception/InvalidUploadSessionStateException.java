package com.ryuqq.fileflow.domain.upload.exception;

import com.ryuqq.fileflow.domain.upload.SessionStatus;

import java.util.Map;

/**
 * Upload Session 상태가 유효하지 않을 때 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>PENDING 상태가 아닌데 start() 호출</li>
 *   <li>IN_PROGRESS 상태가 아닌데 complete() 호출</li>
 *   <li>COMPLETED 상태에서 fail() 호출</li>
 *   <li>이미 FAILED 상태인데 다시 fail() 호출</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 409 Conflict</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InvalidUploadSessionStateException extends UploadException {

    /**
     * 생성자
     *
     * @param currentState 현재 상태
     * @param expectedState 기대 상태
     */
    public InvalidUploadSessionStateException(SessionStatus currentState, SessionStatus expectedState) {
        super(UploadErrorCode.INVALID_SESSION_STATE,
              Map.of("currentState", currentState.name(),
                     "expectedState", expectedState.name()));
    }

    /**
     * 생성자 (기대 상태 미지정)
     *
     * @param currentState 현재 상태
     * @param operation 수행하려던 작업
     */
    public InvalidUploadSessionStateException(SessionStatus currentState, String operation) {
        super(UploadErrorCode.INVALID_SESSION_STATE,
              Map.of("currentState", currentState.name(),
                     "operation", operation));
    }
}
