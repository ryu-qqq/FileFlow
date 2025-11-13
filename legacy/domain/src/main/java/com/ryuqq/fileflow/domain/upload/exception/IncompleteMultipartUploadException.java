package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * Multipart 업로드가 완료되지 않았는데 complete() 호출 시 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>모든 파트가 업로드되지 않은 상태에서 complete() 호출</li>
 *   <li>일부 파트만 업로드된 상태</li>
 *   <li>파트 번호가 연속되지 않음 (예: 1, 2, 4, 5 - 3번 누락)</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 400 Bad Request</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class IncompleteMultipartUploadException extends UploadException {

    /**
     * 생성자
     *
     * @param uploadedParts 업로드된 파트 수
     * @param totalParts 전체 파트 수
     */
    public IncompleteMultipartUploadException(int uploadedParts, int totalParts) {
        super(UploadErrorCode.INCOMPLETE_MULTIPART_UPLOAD,
              Map.of("uploadedParts", uploadedParts,
                     "totalParts", totalParts));
    }

    /**
     * 생성자 (상세 정보 포함)
     *
     * @param uploadedParts 업로드된 파트 수
     * @param totalParts 전체 파트 수
     * @param reason 미완료 사유 (예: "Missing parts: 3, 7, 10")
     */
    public IncompleteMultipartUploadException(int uploadedParts, int totalParts, String reason) {
        super(UploadErrorCode.INCOMPLETE_MULTIPART_UPLOAD,
              Map.of("uploadedParts", uploadedParts,
                     "totalParts", totalParts,
                     "reason", reason));
    }
}
