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
 */
public record TenantId(Long value) {

    /**
     * Compact Constructor (Record 검증 패턴)
     */
    public TenantId {
        validateNotNull(value);
        validatePositive(value);
    }

    /**
     * 정적 팩토리 메서드
     *
     * @param value 테넌트 ID (Long)
     * @return TenantId VO
     */
    public static TenantId of(Long value) {
        return new TenantId(value);
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
