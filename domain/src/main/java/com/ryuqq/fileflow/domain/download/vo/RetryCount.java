package com.ryuqq.fileflow.domain.download.vo;

/**
 * 재시도 횟수 Value Object.
 *
 * <p><strong>책임</strong>: 재시도 횟수 관리 및 재시도 가능 여부 판단
 *
 * <p><strong>규칙</strong>:
 *
 * <ul>
 *   <li>최대 재시도 횟수: 2회
 *   <li>retryCount < MAX_RETRY_COUNT 이면 재시도 가능
 *   <li>불변 객체 (increment 시 새 객체 반환)
 * </ul>
 */
public record RetryCount(int value) {

    private static final int MAX_RETRY_COUNT = 2;

    /** Compact Constructor - 유효성 검증. */
    public RetryCount {
        if (value < 0) {
            throw new IllegalArgumentException("retryCount는 음수일 수 없습니다: " + value);
        }
    }

    /**
     * 초기값(0)으로 생성.
     *
     * @return 초기 RetryCount
     */
    public static RetryCount initial() {
        return new RetryCount(0);
    }

    /**
     * 특정 값으로 생성 (재구성용).
     *
     * @param value 재시도 횟수
     * @return RetryCount
     */
    public static RetryCount of(int value) {
        return new RetryCount(value);
    }

    /**
     * 재시도 가능 여부 확인.
     *
     * @return 재시도 가능하면 true
     */
    public boolean canRetry() {
        return value < MAX_RETRY_COUNT;
    }

    /**
     * 재시도 횟수 증가.
     *
     * @return 증가된 새 RetryCount (불변)
     */
    public RetryCount increment() {
        return new RetryCount(value + 1);
    }
}
