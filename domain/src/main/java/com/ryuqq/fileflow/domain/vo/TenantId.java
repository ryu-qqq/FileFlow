package com.ryuqq.fileflow.domain.vo;

/**
 * TenantId Value Object
 * <p>
 * 테넌트 식별자를 캡슐화합니다.
 * </p>
 *
 * <p>
 * Long FK 전략:
 * - JPA 관계 어노테이션 금지
 * - Long 타입 ID만 저장
 * - 멀티 테넌시 격리에 사용
 * </p>
 *
 * <p>
 * 향후 확장:
 * - 현재는 Nullable로 사용 (테넌트 기능 비활성화 가능)
 * - 향후 필수값으로 변경 가능
 * </p>
 *
 * @param value 테넌트 ID 값 (null 가능 - forNew()로 생성 시)
 */
public record TenantId(Long value) {

    /**
     * Compact Constructor (Record 검증 패턴)
     * <p>
     * null은 forNew()를 통해서만 허용됩니다.
     * </p>
     */
    public TenantId {
        if (value != null) {
            validateNotNull(value);
            validatePositive(value);
        }
    }

    /**
     * 정적 팩토리 메서드
     *
     * @param value 테넌트 ID (Long)
     * @return TenantId VO
     * @throws IllegalArgumentException value가 null일 때
     */
    public static TenantId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("TenantId는 null일 수 없습니다 (forNew() 사용)");
        }
        return new TenantId(value);
    }

    /**
     * 신규 Entity용 팩토리 메서드
     * <p>
     * 영속화 전 상태를 나타내기 위해 null 값을 가진 ID를 생성합니다.
     * </p>
     *
     * @return null 값을 가진 TenantId
     */
    public static TenantId forNew() {
        return new TenantId(null);
    }

    /**
     * 신규 Entity 여부 확인
     *
     * @return value가 null이면 true (영속화 전), 아니면 false
     */
    public boolean isNew() {
        return value == null;
    }

    /**
     * Null 검증
     */
    private static void validateNotNull(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("TenantId는 null일 수 없습니다");
        }
    }

    /**
     * 양수 검증
     */
    private static void validatePositive(Long value) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                    String.format("TenantId는 0보다 커야 합니다 (현재: %d)", value)
            );
        }
    }
}
