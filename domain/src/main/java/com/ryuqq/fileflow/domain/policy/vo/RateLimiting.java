package com.ryuqq.fileflow.domain.policy.vo;

/**
 * RateLimiting Value Object
 * API 요청 및 업로드 횟수 제한을 정의하는 불변 객체
 */
public record RateLimiting(int requestsPerHour, int uploadsPerDay) {
    public RateLimiting {
        if (requestsPerHour
            <= 0) {
            throw new IllegalArgumentException("Requests per hour must be positive: "
                + requestsPerHour);
        }
        if (uploadsPerDay
            <= 0) {
            throw new IllegalArgumentException("Uploads per day must be positive: "
                + uploadsPerDay);
        }
    }

    /**
     * 현재 요청 횟수와 업로드 횟수가 제한 내에 있는지 확인
     *
     * @param currentRequestCount 현재 시간당 요청 횟수
     * @param currentUploadCount  현재 일일 업로드 횟수
     * @return 제한 내에 있으면 true, 초과하면 false
     */
    public boolean isAllowed(int currentRequestCount, int currentUploadCount) {
        return currentRequestCount
            < requestsPerHour
            && currentUploadCount
            < uploadsPerDay;
    }

    /**
     * 업로드 횟수가 제한 내에 있는지 검증
     *
     * @param uploadCount 현재 업로드 횟수
     * @throws IllegalArgumentException 제한을 초과한 경우
     */
    public void validate(int uploadCount) {
        if (uploadCount
            < 0) {
            throw new IllegalArgumentException("Upload count cannot be negative: "
                + uploadCount);
        }
        if (uploadCount
            >= uploadsPerDay) {
            throw new IllegalArgumentException(
                String.format("Upload count %d exceeds daily limit %d", uploadCount, uploadsPerDay)
            );
        }
    }

    @Override
    public String toString() {
        return "RateLimiting{"
            +
            "requestsPerHour="
            + requestsPerHour
            +
            ", uploadsPerDay="
            + uploadsPerDay
            +
            '}';
    }
}
