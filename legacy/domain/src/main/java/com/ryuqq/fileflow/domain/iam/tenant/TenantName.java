package com.ryuqq.fileflow.domain.iam.tenant;

import java.util.Objects;

/**
 * Tenant 이름 Value Object
 *
 * <p>Tenant의 이름을 나타내는 Value Object로, 비즈니스 규칙을 캡슐화합니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>최소 길이: 2자</li>
 *   <li>최대 길이: 50자</li>
 *   <li>null 및 공백 불가</li>
 *   <li>앞뒤 공백 자동 제거</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record TenantName(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 50;

    /**
     * TenantName을 생성합니다.
     *
     * @param value Tenant 이름
     * @throws IllegalArgumentException value가 null, 공백이거나 길이 제약을 위반하는 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public TenantName(String value) {
        if (value
            == null
            || value.isBlank()) {
            throw new IllegalArgumentException("Tenant 이름은 필수입니다");
        }

        String trimmedValue = value.trim();

        if (trimmedValue.length()
            < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Tenant 이름은 최소 %d자 이상이어야 합니다", MIN_LENGTH)
            );
        }

        if (trimmedValue.length()
            > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Tenant 이름은 최대 %d자를 초과할 수 없습니다", MAX_LENGTH)
            );
        }

        this.value = trimmedValue;
    }

    /**
     * TenantName 생성 - Static Factory Method
     *
     * @param value Tenant 이름
     * @return TenantName 인스턴스
     * @throws IllegalArgumentException value가 null, 공백이거나 길이 제약을 위반하는 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static TenantName of(String value) {
        return new TenantName(value);
    }

    /**
     * Tenant 이름 값을 반환합니다.
     *
     * @return Tenant 이름
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public String value() {
        return value;
    }

    /**
     * TenantName 동등성 비교
     *
     * @param o 비교 대상 객체
     * @return 같은 값이면 true
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public boolean equals(Object o) {
        if (this
            == o) return true;
        if (o
            == null
            || getClass()
            != o.getClass()) return false;
        TenantName that = (TenantName) o;
        return Objects.equals(value, that.value);
    }

    /**
     * TenantName 문자열 표현
     *
     * @return 문자열 표현
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public String toString() {
        return value;
    }
}
