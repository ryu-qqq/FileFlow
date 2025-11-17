package com.ryuqq.fileflow.domain.vo;

import java.util.Objects;

/**
 * RetryCount Value Object
 * <p>
 * 재시도 로직을 캡슐화하여 중복 코드를 제거합니다.
 * </p>
 *
 * <p>
 * 재시도 전략:
 * - File: 최대 3회 재시도
 * - FileProcessingJob: 최대 2회 재시도
 * - MessageOutbox: 최대 3회 재시도
 * </p>
 *
 * <p>
 * 불변 객체 (Immutable):
 * - 재시도 횟수 증가 시 새로운 인스턴스 반환
 * - Thread-safe
 * </p>
 */
public final class RetryCount {

    private final int current; // 현재 재시도 횟수
    private final int max; // 최대 재시도 횟수

    /**
     * Private Constructor
     */
    private RetryCount(int current, int max) {
        validateCurrent(current);
        validateMax(max);
        validateCurrentNotExceedMax(current, max);

        this.current = current;
        this.max = max;
    }

    /**
     * File용 RetryCount 생성
     * <p>
     * File 재시도 전략: 최대 3회
     * </p>
     *
     * @return 새로운 RetryCount (current=0, max=3)
     */
    public static RetryCount forFile() {
        return new RetryCount(0, 3);
    }

    /**
     * FileProcessingJob용 RetryCount 생성
     * <p>
     * Job 재시도 전략: 최대 2회
     * </p>
     *
     * @return 새로운 RetryCount (current=0, max=2)
     */
    public static RetryCount forJob() {
        return new RetryCount(0, 2);
    }

    /**
     * MessageOutbox용 RetryCount 생성
     * <p>
     * Outbox 재시도 전략: 최대 3회
     * </p>
     *
     * @return 새로운 RetryCount (current=0, max=3)
     */
    public static RetryCount forOutbox() {
        return new RetryCount(0, 3);
    }

    /**
     * 재시도 가능 여부 확인
     *
     * @return 재시도 가능하면 true
     */
    public boolean canRetry() {
        return current < max;
    }

    /**
     * 재시도 횟수 증가 (새 인스턴스 반환)
     * <p>
     * 불변 객체 패턴: 상태 변경 시 새 인스턴스 생성
     * </p>
     *
     * @return 재시도 횟수가 증가된 새로운 RetryCount
     * @throws IllegalStateException 최대 재시도 횟수 초과 시
     */
    public RetryCount increment() {
        if (!canRetry()) {
            throw new IllegalStateException(
                    String.format("최대 재시도 횟수를 초과했습니다 (현재: %d, 최대: %d)", current, max)
            );
        }
        return new RetryCount(current + 1, max);
    }

    /**
     * 재시도 잔여 횟수 조회
     *
     * @return 남은 재시도 횟수 (max - current)
     */
    public int remaining() {
        return max - current;
    }

    /**
     * 현재 재시도 횟수 조회
     *
     * @return 현재 재시도 횟수
     */
    public int current() {
        return current;
    }

    /**
     * 최대 재시도 횟수 조회
     *
     * @return 최대 재시도 횟수
     */
    public int max() {
        return max;
    }

    /**
     * 현재 재시도 횟수 검증
     */
    private static void validateCurrent(int current) {
        if (current < 0) {
            throw new IllegalArgumentException(
                    String.format("현재 재시도 횟수는 0 이상이어야 합니다 (현재: %d)", current)
            );
        }
    }

    /**
     * 최대 재시도 횟수 검증
     */
    private static void validateMax(int max) {
        if (max < 0) {
            throw new IllegalArgumentException(
                    String.format("최대 재시도 횟수는 0 이상이어야 합니다 (현재: %d)", max)
            );
        }
    }

    /**
     * 현재 값이 최대값을 초과하지 않는지 검증
     */
    private static void validateCurrentNotExceedMax(int current, int max) {
        if (current > max) {
            throw new IllegalArgumentException(
                    String.format("현재 재시도 횟수가 최대값을 초과합니다 (현재: %d, 최대: %d)", current, max)
            );
        }
    }

    // equals, hashCode, toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetryCount that = (RetryCount) o;
        return current == that.current && max == that.max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(current, max);
    }

    @Override
    public String toString() {
        return String.format("RetryCount{current=%d, max=%d, remaining=%d}", current, max, remaining());
    }
}
