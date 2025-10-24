package com.ryuqq.fileflow.domain.iam.permission;

/**
 * Permission Code Value Object
 *
 * <p>Permission의 식별 코드를 나타내는 값 객체입니다.
 * 예: "file.upload", "user.read", "tenant.admin"</p>
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
public final class PermissionCode {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 100;

    private final String value;

    /**
     * PermissionCode를 생성합니다 (Private 생성자).
     *
     * @param value Permission 코드 문자열
     * @throws IllegalArgumentException value가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    private PermissionCode(String value) {
        validateValue(value);
        this.value = value;
    }

    /**
     * PermissionCode를 생성합니다 (Static Factory Method).
     *
     * @param value Permission 코드 문자열
     * @return 생성된 PermissionCode
     * @throws IllegalArgumentException value가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static PermissionCode of(String value) {
        return new PermissionCode(value);
    }

    /**
     * Permission 코드 문자열을 검증합니다.
     *
     * @param value 검증할 Permission 코드 문자열
     * @throws IllegalArgumentException value가 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    private void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission 코드는 필수입니다");
        }

        String trimmedValue = value.trim();
        if (trimmedValue.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Permission 코드는 최소 %d자 이상이어야 합니다", MIN_LENGTH)
            );
        }

        if (trimmedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Permission 코드는 최대 %d자를 초과할 수 없습니다", MAX_LENGTH)
            );
        }

        // 영문, 숫자, 점(.), 하이픈(-), 언더스코어(_)만 허용
        if (!trimmedValue.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException(
                "Permission 코드는 영문, 숫자, 점(.), 하이픈(-), 언더스코어(_)만 사용할 수 있습니다"
            );
        }
    }

    /**
     * Permission 코드 문자열을 반환합니다.
     *
     * @return Permission 코드 문자열
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getValue() {
        return value;
    }

    /**
     * 두 PermissionCode가 같은지 확인합니다.
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
        PermissionCode that = (PermissionCode) o;
        return value.equals(that.value);
    }

    /**
     * PermissionCode의 해시코드를 반환합니다.
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
     * PermissionCode의 문자열 표현을 반환합니다.
     *
     * @return Permission 코드 문자열
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public String toString() {
        return value;
    }
}
