package com.ryuqq.fileflow.domain.file.extraction;

/**
 * Validation Status Enum
 * 추출 데이터 검증 상태
 *
 * <p><strong>검증 상태:</strong></p>
 * <ul>
 *   <li>PENDING: 검증 대기 중</li>
 *   <li>PASSED: 검증 통과</li>
 *   <li>FAILED: 검증 실패</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum ValidationStatus {
    /**
     * 검증 대기 중
     * 추출 후 아직 검증되지 않음
     */
    PENDING,

    /**
     * 검증 통과
     * 데이터 품질 기준 충족
     */
    PASSED,

    /**
     * 검증 실패
     * 데이터 품질 기준 미충족
     */
    FAILED
}
