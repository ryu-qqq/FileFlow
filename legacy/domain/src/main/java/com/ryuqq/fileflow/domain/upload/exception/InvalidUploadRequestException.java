package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * 업로드 요청이 유효하지 않은 경우 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>필수 파라미터 누락 (fileName, fileSize 등)</li>
 *   <li>파라미터 값이 비즈니스 규칙 위반</li>
 *   <li>요청 데이터 형식 오류</li>
 *   <li>일반적인 검증 실패 (특정 예외로 분류되지 않는 경우)</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 400 Bad Request</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InvalidUploadRequestException extends UploadException {

    /**
     * 생성자
     *
     * @param reason 검증 실패 사유
     */
    public InvalidUploadRequestException(String reason) {
        super(UploadErrorCode.INVALID_UPLOAD_REQUEST,
              Map.of("reason", reason));
    }

    /**
     * 생성자 (필드 정보 포함)
     *
     * @param field 검증 실패 필드
     * @param reason 검증 실패 사유
     */
    public InvalidUploadRequestException(String field, String reason) {
        super(UploadErrorCode.INVALID_UPLOAD_REQUEST,
              Map.of("field", field,
                     "reason", reason));
    }
}
