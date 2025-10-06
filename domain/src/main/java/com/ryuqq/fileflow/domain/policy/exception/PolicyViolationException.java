package com.ryuqq.fileflow.domain.policy.exception;

import java.util.Objects;

/**
 * Domain Exception: 업로드 파일이 정책 위반 시 발생
 *
 * 발생 조건:
 * - FILE_SIZE_EXCEEDED: 파일 크기가 maxFileSize 초과
 * - FILE_COUNT_EXCEEDED: 파일 개수가 maxFileCount 초과
 * - INVALID_FORMAT: 허용되지 않은 파일 형식
 * - DIMENSION_EXCEEDED: 이미지 크기(가로/세로)가 제한 초과
 *
 * 사용 시나리오:
 * - File Upload 검증 실패 시 사용자에게 구체적인 오류 메시지 반환
 * - 업로드 전 클라이언트 측 정책 위반 검증
 * - 정책 위반 로그 기록 및 모니터링
 */
public class PolicyViolationException extends RuntimeException {

    private final ViolationType violationType;
    private final String details;

    public PolicyViolationException(ViolationType violationType, String details) {
        super(formatMessage(violationType, details));
        this.violationType = Objects.requireNonNull(violationType, "violationType must not be null");
        this.details = Objects.requireNonNull(details, "details must not be null");
    }

    public PolicyViolationException(ViolationType violationType, String details, Throwable cause) {
        super(formatMessage(violationType, details), cause);
        this.violationType = Objects.requireNonNull(violationType, "violationType must not be null");
        this.details = Objects.requireNonNull(details, "details must not be null");
    }

    private static String formatMessage(ViolationType violationType, String details) {
        return String.format("Policy violation: %s - %s", violationType, details);
    }

    public ViolationType getViolationType() {
        return violationType;
    }

    public String getDetails() {
        return details;
    }

    /**
     * 정책 위반 유형
     */
    public enum ViolationType {
        /**
         * 파일 크기 초과
         * 예: "File size 15MB exceeds limit of 10MB"
         */
        FILE_SIZE_EXCEEDED,

        /**
         * 파일 개수 초과
         * 예: "File count 6 exceeds limit of 5"
         */
        FILE_COUNT_EXCEEDED,

        /**
         * 유효하지 않은 파일 형식
         * 예: "File format .exe is not allowed. Allowed: [jpg, png, gif]"
         */
        INVALID_FORMAT,

        /**
         * 이미지 크기(dimension) 초과
         * 예: "Image dimension 5000x5000 exceeds limit of 4096x4096"
         */
        DIMENSION_EXCEEDED,

        /**
         * Rate Limit 초과
         * 예: "Rate limit exceeded: 150 requests/hour (limit: 100)"
         */
        RATE_LIMIT_EXCEEDED
    }
}
