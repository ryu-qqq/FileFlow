package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * Upload Session이 만료된 경우 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>Session 생성 후 24시간 경과</li>
 *   <li>expire() 메서드 호출로 명시적 만료</li>
 *   <li>만료된 Session으로 작업 시도</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 410 Gone</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadSessionExpiredException extends UploadException {

    /**
     * 생성자
     *
     * @param sessionKey Session Key
     */
    public UploadSessionExpiredException(String sessionKey) {
        super(UploadErrorCode.UPLOAD_SESSION_EXPIRED,
              Map.of("sessionKey", sessionKey));
    }

    /**
     * 생성자 (만료 시각 포함)
     *
     * @param sessionKey Session Key
     * @param expiredAt 만료 시각
     */
    public UploadSessionExpiredException(String sessionKey, String expiredAt) {
        super(UploadErrorCode.UPLOAD_SESSION_EXPIRED,
              Map.of("sessionKey", sessionKey,
                     "expiredAt", expiredAt));
    }
}
