package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * 이미 완료된 Upload Session을 수정하려 할 때 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>COMPLETED 상태에서 fail() 호출</li>
 *   <li>COMPLETED 상태에서 complete() 재호출</li>
 *   <li>COMPLETED 상태에서 파일 크기 업데이트 시도</li>
 *   <li>COMPLETED 상태에서 expire() 호출</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 409 Conflict</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadAlreadyCompletedException extends UploadException {

    /**
     * 생성자
     *
     * @param sessionKey Session Key
     */
    public UploadAlreadyCompletedException(String sessionKey) {
        super(UploadErrorCode.UPLOAD_ALREADY_COMPLETED,
              Map.of("sessionKey", sessionKey));
    }

    /**
     * 생성자 (작업 정보 포함)
     *
     * @param sessionKey Session Key
     * @param operation 수행하려던 작업
     */
    public UploadAlreadyCompletedException(String sessionKey, String operation) {
        super(UploadErrorCode.UPLOAD_ALREADY_COMPLETED,
              Map.of("sessionKey", sessionKey,
                     "operation", operation));
    }
}
