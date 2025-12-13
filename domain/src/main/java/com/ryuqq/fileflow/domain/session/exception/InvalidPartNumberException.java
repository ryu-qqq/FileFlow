package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/**
 * 유효하지 않은 Part 번호 예외.
 *
 * <p>Multipart 업로드에서 Part 번호가 유효 범위(1 ~ totalParts)를 벗어난 경우 발생합니다.
 *
 * <p><strong>에러 코드</strong>: INVALID_PART_NUMBER
 *
 * <p><strong>HTTP 상태</strong>: 400 Bad Request
 */
public class InvalidPartNumberException extends DomainException {

    /**
     * InvalidPartNumberException 생성자
     *
     * @param partNumber 유효하지 않은 Part 번호
     * @param totalParts 전체 Part 개수
     */
    public InvalidPartNumberException(int partNumber, int totalParts) {
        super(
                SessionErrorCode.INVALID_PART_NUMBER,
                String.format("Part 번호가 유효하지 않습니다: %d (전체: %d)", partNumber, totalParts));
    }
}
