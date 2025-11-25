package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/**
 * 중복된 Part 번호 예외.
 *
 * <p>Multipart 업로드에서 이미 완료된 Part 번호로 다시 업로드를 시도한 경우 발생합니다.
 *
 * <p><strong>에러 코드</strong>: DUPLICATE_PART_NUMBER
 *
 * <p><strong>HTTP 상태</strong>: 409 Conflict
 */
public class DuplicatePartNumberException extends DomainException {

    /**
     * DuplicatePartNumberException 생성자
     *
     * @param partNumber 중복된 Part 번호
     */
    public DuplicatePartNumberException(int partNumber) {
        super(
                SessionErrorCode.DUPLICATE_PART_NUMBER.getCode(),
                String.format("Part %d는 이미 완료되었습니다.", partNumber));
    }
}
