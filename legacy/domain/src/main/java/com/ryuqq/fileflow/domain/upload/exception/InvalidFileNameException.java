package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * 파일명이 유효하지 않은 경우 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>파일명이 null 또는 빈 문자열</li>
 *   <li>파일명에 금지된 문자 포함 (/, \, :, *, ?, ", <, >, |)</li>
 *   <li>파일명이 공백으로만 구성</li>
 *   <li>파일명이 시스템 예약어 (CON, PRN, AUX, NUL 등)</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 400 Bad Request</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InvalidFileNameException extends UploadException {

    /**
     * 생성자
     *
     * @param fileName 잘못된 파일명
     */
    public InvalidFileNameException(String fileName) {
        super(UploadErrorCode.INVALID_FILE_NAME,
              Map.of("fileName", fileName != null ? fileName : "null"));
    }

    /**
     * 생성자 (사유 포함)
     *
     * @param fileName 잘못된 파일명
     * @param reason 검증 실패 사유
     */
    public InvalidFileNameException(String fileName, String reason) {
        super(UploadErrorCode.INVALID_FILE_NAME,
              Map.of("fileName", fileName != null ? fileName : "null",
                     "reason", reason));
    }
}
