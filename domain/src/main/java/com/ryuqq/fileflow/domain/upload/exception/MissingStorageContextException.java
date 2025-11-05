package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * Storage Context가 누락된 경우 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>UploadSession 생성 시 storageContext가 null</li>
 *   <li>Tenant 정보 누락으로 Storage 경로 결정 불가</li>
 *   <li>필수 Storage 설정 정보 누락</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 400 Bad Request</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class MissingStorageContextException extends UploadException {

    /**
     * 생성자
     *
     * @param sessionKey Session Key
     */
    public MissingStorageContextException(String sessionKey) {
        super(UploadErrorCode.MISSING_STORAGE_CONTEXT,
              Map.of("sessionKey", sessionKey));
    }

    /**
     * 생성자 (누락된 필드 정보 포함)
     *
     * @param sessionKey Session Key
     * @param missingField 누락된 필드명
     */
    public MissingStorageContextException(String sessionKey, String missingField) {
        super(UploadErrorCode.MISSING_STORAGE_CONTEXT,
              Map.of("sessionKey", sessionKey,
                     "missingField", missingField));
    }
}
