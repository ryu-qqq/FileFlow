package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * 파일명 길이가 최대 허용 길이를 초과한 경우 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>파일명 길이가 255자를 초과</li>
 *   <li>파일 시스템 제약사항 위반 (NTFS, ext4 등)</li>
 *   <li>경로 포함 전체 길이가 4096자를 초과 (일부 시스템)</li>
 * </ul>
 *
 * <p><strong>최대 길이:</strong> 255자 (파일명 단독)</p>
 * <p><strong>HTTP Status:</strong> 400 Bad Request</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileNameTooLongException extends UploadException {

    private static final int MAX_FILE_NAME_LENGTH = 255;

    /**
     * 생성자
     *
     * @param actualLength 실제 파일명 길이
     */
    public FileNameTooLongException(int actualLength) {
        super(UploadErrorCode.FILE_NAME_TOO_LONG,
              Map.of("actualLength", actualLength,
                     "maxLength", MAX_FILE_NAME_LENGTH));
    }

    /**
     * 생성자 (파일명 포함)
     *
     * @param fileName 파일명
     * @param actualLength 실제 파일명 길이
     */
    public FileNameTooLongException(String fileName, int actualLength) {
        super(UploadErrorCode.FILE_NAME_TOO_LONG,
              Map.of("fileName", fileName,
                     "actualLength", actualLength,
                     "maxLength", MAX_FILE_NAME_LENGTH));
    }
}
