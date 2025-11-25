package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/**
 * Part 업로드 미완료 예외.
 *
 * <p>Multipart 업로드에서 모든 Part가 완료되지 않은 상태에서 업로드 완료를 시도한 경우 발생합니다.
 *
 * <p><strong>에러 코드</strong>: INCOMPLETE_PARTS
 *
 * <p><strong>HTTP 상태</strong>: 412 Precondition Failed
 */
public class IncompletePartsException extends DomainException {

    /**
     * IncompletePartsException 생성자
     *
     * @param completedCount 완료된 Part 개수
     * @param totalParts 전체 Part 개수
     */
    public IncompletePartsException(int completedCount, int totalParts) {
        super(
                SessionErrorCode.INCOMPLETE_PARTS.getCode(),
                String.format("모든 Part가 완료되지 않았습니다. (완료: %d/%d)", completedCount, totalParts));
    }
}
