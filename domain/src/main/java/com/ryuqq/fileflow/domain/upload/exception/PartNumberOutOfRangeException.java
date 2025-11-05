package com.ryuqq.fileflow.domain.upload.exception;

import java.util.Map;

/**
 * Part 번호가 유효 범위를 벗어난 경우 발생하는 예외
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>Part 번호가 1보다 작음</li>
 *   <li>Part 번호가 10,000을 초과</li>
 *   <li>S3 Multipart Upload 제약사항 위반</li>
 * </ul>
 *
 * <p><strong>유효 범위:</strong> 1 ~ 10,000</p>
 * <p><strong>HTTP Status:</strong> 400 Bad Request</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class PartNumberOutOfRangeException extends UploadException {

    private static final int MIN_PART_NUMBER = 1;
    private static final int MAX_PART_NUMBER = 10000;

    /**
     * 생성자
     *
     * @param partNumber 잘못된 파트 번호
     */
    public PartNumberOutOfRangeException(int partNumber) {
        super(UploadErrorCode.PART_NUMBER_OUT_OF_RANGE,
              Map.of("partNumber", partNumber,
                     "minPart", MIN_PART_NUMBER,
                     "maxPart", MAX_PART_NUMBER));
    }
}
