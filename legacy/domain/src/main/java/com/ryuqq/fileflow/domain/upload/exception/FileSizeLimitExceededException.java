package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * 파일 크기 제한 초과 시 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>단일 업로드: 파일 크기 > 100MB</li>
 *   <li>Multipart 업로드: 파일 크기 > 5TB</li>
 *   <li>파트 크기: 5MB ~ 5GB 범위 벗어남</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 413 Payload Too Large</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileSizeLimitExceededException extends UploadException {

    /**
     * 생성자
     *
     * @param actualSize 실제 파일 크기 (bytes)
     * @param maxSize 최대 허용 크기 (bytes)
     */
    public FileSizeLimitExceededException(long actualSize, long maxSize) {
        super(UploadErrorCode.FILE_SIZE_LIMIT_EXCEEDED,
              Map.of("actualSize", actualSize,
                     "maxSize", maxSize));
    }

    /**
     * 생성자 (상세 정보 포함)
     *
     * @param actualSize 실제 파일 크기 (bytes)
     * @param maxSize 최대 허용 크기 (bytes)
     * @param uploadType Upload 타입 (SINGLE or MULTIPART)
     */
    public FileSizeLimitExceededException(long actualSize, long maxSize, String uploadType) {
        super(UploadErrorCode.FILE_SIZE_LIMIT_EXCEEDED,
              Map.of("actualSize", actualSize,
                     "maxSize", maxSize,
                     "uploadType", uploadType));
    }
}
