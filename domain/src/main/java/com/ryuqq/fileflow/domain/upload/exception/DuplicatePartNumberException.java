package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * 중복된 파트 번호로 업로드 시도 시 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>이미 업로드된 파트 번호를 다시 업로드 시도</li>
 *   <li>MultipartUpload.addPart() 호출 시 중복 검증 실패</li>
 * </ul>
 *
 * <p><strong>HTTP Status:</strong> 409 Conflict</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class DuplicatePartNumberException extends UploadException {

    /**
     * 생성자
     *
     * @param partNumber 중복된 파트 번호
     */
    public DuplicatePartNumberException(int partNumber) {
        super(UploadErrorCode.DUPLICATE_PART_NUMBER,
              Map.of("partNumber", partNumber));
    }
}
