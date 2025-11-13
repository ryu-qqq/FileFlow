package com.ryuqq.fileflow.domain.iam.organization;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 조직 코드 Value Object
 *
 * <p>Organization의 고유 코드를 나타내는 불변 객체입니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>최소 길이: 2자</li>
 *   <li>최대 길이: 20자</li>
 *   <li>허용 문자: 영문 대문자, 숫자, 하이픈(-), 언더스코어(_)</li>
 *   <li>자동 대문자 변환</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ 불변 객체 (final 필드)</li>
 *   <li>✅ 캡슐화 (유효성 검증)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record OrgCode(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 20;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Z0-9_-]+$");

    /**
     * OrgCode를 생성합니다.
     *
     * <p>입력값을 대문자로 변환하고 유효성을 검증합니다.</p>
     *
     * @param value 조직 코드 값
     * @throws IllegalArgumentException value가 null, 빈 문자열이거나 비즈니스 규칙을 위반하는 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public OrgCode(String value) {
        if (value
            == null
            || value.isBlank()) {
            throw new IllegalArgumentException("조직 코드는 필수입니다");
        }

        String normalized = value.trim().toUpperCase();

        if (normalized.length()
            < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("조직 코드는 최소 %d자 이상이어야 합니다", MIN_LENGTH)
            );
        }

        if (normalized.length()
            > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("조직 코드는 최대 %d자까지 허용됩니다", MAX_LENGTH)
            );
        }

        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "조직 코드는 영문 대문자, 숫자, 하이픈(-), 언더스코어(_)만 허용됩니다"
            );
        }

        this.value = normalized;
    }

    /**
     * OrgCode 생성 - Static Factory Method
     *
     * @param value 조직 코드 값
     * @return OrgCode 인스턴스
     * @throws IllegalArgumentException value가 null, 빈 문자열이거나 비즈니스 규칙을 위반하는 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static OrgCode of(String value) {
        return new OrgCode(value);
    }

    /**
     * 조직 코드 값을 반환합니다.
     *
     * @return 조직 코드 값
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public String value() {
        return value;
    }

    /**
     * 동등성을 비교합니다.
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public boolean equals(Object o) {
        if (this
            == o) {
            return true;
        }
        if (o
            == null
            || getClass()
            != o.getClass()) {
            return false;
        }
        OrgCode orgCode = (OrgCode) o;
        return Objects.equals(value, orgCode.value);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return 조직 코드 값
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public String toString() {
        return value;
    }
}
