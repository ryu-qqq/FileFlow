package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * Upload Session을 찾을 수 없을 때 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>존재하지 않는 Session Key로 조회 시</li>
 *   <li>Redis TTL 만료로 Session이 삭제된 경우</li>
 *   <li>잘못된 Session Key 입력</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 404 Not Found</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadSessionNotFoundException extends UploadException {

    /**
     * 생성자 - Session Key
     *
     * @param sessionKey 찾을 수 없는 Session Key
     */
    public UploadSessionNotFoundException(String sessionKey) {
        super(UploadErrorCode.UPLOAD_SESSION_NOT_FOUND,
              Map.of("sessionKey", sessionKey));
    }

    /**
     * 생성자 - Upload Session ID
     *
     * @param uploadSessionId 찾을 수 없는 Upload Session ID
     */
    public UploadSessionNotFoundException(Long uploadSessionId) {
        super(UploadErrorCode.UPLOAD_SESSION_NOT_FOUND,
              Map.of("uploadSessionId", uploadSessionId.toString()));
    }
}
