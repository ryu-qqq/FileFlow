package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * Multipart Upload가 초기화되지 않은 상태에서 작업 시도 시 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>initMultipart() 호출 없이 addPart() 시도</li>
 *   <li>MultipartUpload 객체가 null인 상태에서 작업</li>
 *   <li>UploadSession의 uploadType이 MULTIPART가 아닌데 Multipart 메서드 호출</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 400 Bad Request</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class MultipartNotInitializedException extends UploadException {

    /**
     * 생성자
     *
     * @param sessionKey Session Key
     */
    public MultipartNotInitializedException(String sessionKey) {
        super(UploadErrorCode.MULTIPART_NOT_INITIALIZED,
              Map.of("sessionKey", sessionKey));
    }

    /**
     * 생성자 (작업 정보 포함)
     *
     * @param sessionKey Session Key
     * @param operation 수행하려던 작업
     */
    public MultipartNotInitializedException(String sessionKey, String operation) {
        super(UploadErrorCode.MULTIPART_NOT_INITIALIZED,
              Map.of("sessionKey", sessionKey,
                     "operation", operation));
    }
}
