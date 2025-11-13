package com.ryuqq.fileflow.domain.iam.tenant;

/**
 * Tenant 식별자
 *
 * <p>Tenant의 고유 식별자를 나타내는 Value Object입니다.
 * Java 21 Record를 사용하여 불변성을 보장합니다.</p>
 *
 * <p><strong>PK 타입 변경 (Option B):</strong></p>
 * <ul>
 *   <li>변경 전: String (UUID)</li>
 *   <li>변경 후: Long (AUTO_INCREMENT)</li>
 *   <li>이유: Settings.contextId (BIGINT)와 타입 일관성 확보</li>
 * </ul>
 *
 * @param value Tenant ID 값 (Long - AUTO_INCREMENT)
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record TenantId(Long value) {

    /**
     * Compact 생성자 - 유효성 검증
     *
     * <p><strong>Option B 변경:</strong></p>
     * <ul>
     *   <li>null 허용: 새로운 엔티티 생성 시 (AUTO_INCREMENT 전에)</li>
     *   <li>양수만 허용: 기존 엔티티 (save 후)</li>
     * </ul>
     *
     * @throws IllegalArgumentException value가 0 이하인 경우 (null은 허용)
     */
    public TenantId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Tenant ID는 양수여야 합니다");
        }
        // null은 허용: 새로운 엔티티를 의미 (save 전)
    }

    /**
     * TenantId 생성 - Static Factory Method
     *
     * @param value Tenant ID 값 (Long - AUTO_INCREMENT)
     * @return TenantId 인스턴스
     * @throws IllegalArgumentException value가 null이거나 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static TenantId of(Long value) {
        return new TenantId(value);
    }
}
