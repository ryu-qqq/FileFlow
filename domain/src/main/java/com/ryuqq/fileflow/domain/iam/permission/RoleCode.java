package com.ryuqq.fileflow.domain.iam.permission;

/**
 * Role Code Value Object
 *
 * <p>Role의 식별 코드를 나타내는 값 객체입니다.
 * 예: "org.uploader", "tenant.admin", "system.viewer"</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ 불변 객체 (Immutable Value Object)</li>
 *   <li>✅ 방어적 복사 및 유효성 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public final class RoleCode {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 100;
    private static final java.util.regex.Pattern CODE_PATTERN =
        java.util.regex.Pattern.compile("^[a-zA-Z0-9._-]+$");

    private final String value;

    /**
     * RoleCode를 생성합니다 (Private 생성자).
     *
     * @param value Role 코드 문자열
     * @throws IllegalArgumentException value가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    private RoleCode(String value) {
        validateValue(value);
        this.value = value.trim();
    }

    /**
     * RoleCode를 생성합니다 (Static Factory Method).
     *
     * @param value Role 코드 문자열
     * @return 생성된 RoleCode
     * @throws IllegalArgumentException value가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static RoleCode of(String value) {
        return new RoleCode(value);
    }

    /**
     * Role 코드 문자열을 검증합니다.
     *
     * @param value 검증할 Role 코드 문자열
     * @throws IllegalArgumentException value가 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    private void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role 코드는 필수입니다");
        }

        String trimmedValue = value.trim();
        if (trimmedValue.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Role 코드는 최소 %d자 이상이어야 합니다", MIN_LENGTH)
            );
        }

        if (trimmedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Role 코드는 최대 %d자를 초과할 수 없습니다", MAX_LENGTH)
            );
        }

        // 영문, 숫자, 점(.), 하이픈(-), 언더스코어(_)만 허용
        if (!CODE_PATTERN.matcher(trimmedValue).matches()) {
            throw new IllegalArgumentException(
                "Role 코드는 영문, 숫자, 점(.), 하이픈(-), 언더스코어(_)만 사용할 수 있습니다"
            );
        }
    }

    /**
     * Role 코드 문자열을 반환합니다.
     *
     * @return Role 코드 문자열
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getValue() {
        return value;
    }

    /**
     * 두 RoleCode가 같은지 확인합니다.
     *
     * @param o 비교 대상 객체
     * @return 같으면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleCode that = (RoleCode) o;
        return value.equals(that.value);
    }

    /**
     * RoleCode의 해시코드를 반환합니다.
     *
     * @return 해시코드
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * RoleCode의 문자열 표현을 반환합니다.
     *
     * @return Role 코드 문자열
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public String toString() {
        return value;
    }
}
