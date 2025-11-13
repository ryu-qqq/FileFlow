package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * MIME 타입이 유효하지 않은 경우 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>MIME 타입이 null 또는 빈 문자열</li>
 *   <li>MIME 타입 형식이 잘못됨 (type/subtype 형식 위반)</li>
 *   <li>허용되지 않은 MIME 타입 (화이트리스트 정책)</li>
 *   <li>금지된 MIME 타입 (블랙리스트 정책, 예: application/x-msdownload)</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 400 Bad Request</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InvalidMimeTypeException extends UploadException {

    /**
     * 생성자
     *
     * @param mimeType 잘못된 MIME 타입
     */
    public InvalidMimeTypeException(String mimeType) {
        super(UploadErrorCode.INVALID_MIME_TYPE,
              Map.of("mimeType", mimeType != null ? mimeType : "null"));
    }

    /**
     * 생성자 (허용된 타입 목록 포함)
     *
     * @param mimeType 잘못된 MIME 타입
     * @param allowedTypes 허용된 MIME 타입 목록 (쉼표 구분)
     */
    public InvalidMimeTypeException(String mimeType, String allowedTypes) {
        super(UploadErrorCode.INVALID_MIME_TYPE,
              Map.of("mimeType", mimeType != null ? mimeType : "null",
                     "allowedTypes", allowedTypes));
    }
}
