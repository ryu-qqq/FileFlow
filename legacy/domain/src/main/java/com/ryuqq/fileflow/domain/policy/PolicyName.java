package com.ryuqq.fileflow.domain.policy;

/**
 * PolicyName Value Object
 * 업로드 정책의 이름을 나타내는 값 객체
 *
 * <p>정책 이름은 업로드 정책을 사람이 읽기 쉽게 식별하는 레이블입니다.
 * 예: "이미지 업로드 정책", "동영상 업로드 정책"</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>정책 이름은 필수 값입니다</li>
 *   <li>빈 문자열이나 공백만 있는 값은 허용되지 않습니다</li>
 *   <li>최대 길이 100자</li>
 * </ul>
 *
 * @param value 정책 이름
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record PolicyName(String value) {

    /**
     * 정책 이름 최대 길이
     */
    public static final int MAX_LENGTH = 100;

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException 정책 이름이 null이거나 최대 길이를 초과한 경우
     */
    public PolicyName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("정책 이름은 필수입니다");
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("정책 이름은 %d자를 초과할 수 없습니다: %d", MAX_LENGTH, value.length())
            );
        }
    }

    /**
     * Static Factory Method
     *
     * @param value 정책 이름
     * @return PolicyName 인스턴스
     * @throws IllegalArgumentException 정책 이름이 유효하지 않은 경우
     */
    public static PolicyName of(String value) {
        return new PolicyName(value);
    }

    /**
     * 다른 정책 이름과 동일한지 비교 (대소문자 무시)
     *
     * @param other 비교할 정책 이름
     * @return 동일하면 true
     */
    public boolean matchesIgnoreCase(PolicyName other) {
        if (other == null) {
            return false;
        }
        return this.value.equalsIgnoreCase(other.value);
    }

    /**
     * 문자열 값과 직접 비교 (대소문자 무시)
     *
     * @param nameValue 비교할 정책 이름 문자열
     * @return 동일하면 true
     */
    public boolean matchesIgnoreCase(String nameValue) {
        if (nameValue == null || nameValue.isBlank()) {
            return false;
        }
        return this.value.equalsIgnoreCase(nameValue.trim());
    }
}
