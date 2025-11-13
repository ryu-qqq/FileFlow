package com.ryuqq.fileflow.domain.upload.exception;

import com.ryuqq.fileflow.domain.upload.UploadType;

import java.util.Map;

/**
 * Upload 타입 불일치 시 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>SINGLE 타입인데 Multipart 메서드 호출</li>
 *   <li>MULTIPART 타입인데 Single 메서드 호출</li>
 *   <li>attachMultipart() 호출 시 uploadType이 MULTIPART가 아닌 경우</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 400 Bad Request</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadTypeMismatchException extends UploadException {

    /**
     * 생성자
     *
     * @param actualType 실제 Upload 타입
     * @param expectedType 기대 Upload 타입
     */
    public UploadTypeMismatchException(UploadType actualType, UploadType expectedType) {
        super(UploadErrorCode.UPLOAD_TYPE_MISMATCH,
              Map.of("actualType", actualType.name(),
                     "expectedType", expectedType.name()));
    }

    /**
     * 생성자 (기대 타입 미지정)
     *
     * @param actualType 실제 Upload 타입
     * @param operation 수행하려던 작업
     */
    public UploadTypeMismatchException(UploadType actualType, String operation) {
        super(UploadErrorCode.UPLOAD_TYPE_MISMATCH,
              Map.of("actualType", actualType.name(),
                     "operation", operation));
    }
}
