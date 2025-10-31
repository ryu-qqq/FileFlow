package com.ryuqq.fileflow.domain.policy;

/**
 * Priority Value Object
 * 업로드 정책의 우선순위를 나타내는 값 객체
 *
 * <p>우선순위는 여러 정책이 동시에 적용 가능할 때 어떤 정책을 먼저 적용할지 결정합니다.
 * 숫자가 높을수록 우선순위가 높습니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>우선순위는 필수 값입니다</li>
 *   <li>0 이상 100 이하의 값만 허용합니다</li>
 *   <li>100이 가장 높은 우선순위, 0이 가장 낮은 우선순위입니다</li>
 * </ul>
 *
 * @param value 우선순위 (0-100)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record Priority(Integer value) {

    /**
     * 최소 우선순위 값
     */
    public static final int MIN_PRIORITY = 0;

    /**
     * 최대 우선순위 값
     */
    public static final int MAX_PRIORITY = 100;

    /**
     * 기본 우선순위 값 (중간)
     */
    public static final int DEFAULT_PRIORITY = 50;

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException 우선순위가 null이거나 유효 범위를 벗어난 경우
     */
    public Priority {
        if (value == null) {
            throw new IllegalArgumentException("우선순위는 필수입니다");
        }
        if (value < MIN_PRIORITY) {
            throw new IllegalArgumentException(
                    String.format("우선순위는 %d 이상이어야 합니다: %d", MIN_PRIORITY, value)
            );
        }
        if (value > MAX_PRIORITY) {
            throw new IllegalArgumentException(
                    String.format("우선순위는 %d 이하여야 합니다: %d", MAX_PRIORITY, value)
            );
        }
    }

    /**
     * Static Factory Method
     *
     * @param value 우선순위
     * @return Priority 인스턴스
     * @throws IllegalArgumentException 우선순위가 유효하지 않은 경우
     */
    public static Priority of(Integer value) {
        return new Priority(value);
    }

    /**
     * 기본 우선순위로 생성
     *
     * @return 기본 Priority 인스턴스 (50)
     */
    public static Priority defaultPriority() {
        return new Priority(DEFAULT_PRIORITY);
    }

    /**
     * 최고 우선순위로 생성
     *
     * @return 최고 Priority 인스턴스 (100)
     */
    public static Priority highest() {
        return new Priority(MAX_PRIORITY);
    }

    /**
     * 최저 우선순위로 생성
     *
     * @return 최저 Priority 인스턴스 (0)
     */
    public static Priority lowest() {
        return new Priority(MIN_PRIORITY);
    }

    /**
     * 다른 우선순위보다 높은지 확인
     *
     * @param other 비교할 우선순위
     * @return 현재 우선순위가 더 높으면 true
     */
    public boolean isHigherThan(Priority other) {
        if (other == null) {
            return true;
        }
        return this.value > other.value;
    }

    /**
     * 다른 우선순위보다 낮은지 확인
     *
     * @param other 비교할 우선순위
     * @return 현재 우선순위가 더 낮으면 true
     */
    public boolean isLowerThan(Priority other) {
        if (other == null) {
            return false;
        }
        return this.value < other.value;
    }

    /**
     * 최고 우선순위인지 확인
     *
     * @return 최고 우선순위면 true
     */
    public boolean isHighest() {
        return value == MAX_PRIORITY;
    }

    /**
     * 최저 우선순위인지 확인
     *
     * @return 최저 우선순위면 true
     */
    public boolean isLowest() {
        return value == MIN_PRIORITY;
    }
}
