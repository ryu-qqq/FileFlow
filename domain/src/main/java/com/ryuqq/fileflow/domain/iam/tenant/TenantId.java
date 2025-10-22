package com.ryuqq.fileflow.domain.iam.tenant;

/**
 * Tenant 식별자
 *
 * <p>Tenant의 고유 식별자를 나타내는 Value Object입니다.
 * Java 21 Record를 사용하여 불변성을 보장합니다.</p>
 *
 * @param value Tenant ID 값
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record TenantId(String value) {

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException value가 null이거나 빈 문자열인 경우
     */
    public TenantId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
    }

    /**
     * TenantId 생성 - Static Factory Method
     *
     * @param value Tenant ID 값
     * @return TenantId 인스턴스
     * @throws IllegalArgumentException value가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static TenantId of(String value) {
        return new TenantId(value);
    }
}
