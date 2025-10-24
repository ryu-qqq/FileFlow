package com.ryuqq.fileflow.domain.iam.usercontext;

/**
 * UserContext 식별자
 *
 * <p>UserContext의 고유 식별자를 나타내는 Value Object입니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용</li>
 *   <li>✅ Long 타입 ID (Long FK 전략)</li>
 *   <li>✅ 불변 객체 (Immutable)</li>
 *   <li>✅ null 및 음수 검증</li>
 *   <li>❌ Lombok 사용 안함</li>
 * </ul>
 *
 * @param value UserContext ID 값
 * @author ryu-qqq
 * @since 2025-10-24
 */
public record UserContextId(Long value) {

    /**
     * UserContextId의 Compact Constructor
     *
     * <p>null 및 음수 값을 검증합니다.</p>
     *
     * @throws IllegalArgumentException value가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public UserContextId {
        if (value == null) {
            throw new IllegalArgumentException("UserContext ID는 필수입니다");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("UserContext ID는 양수여야 합니다");
        }
    }

    /**
     * UserContextId 생성 - Static Factory Method
     *
     * @param value UserContext ID 값
     * @return UserContextId 인스턴스
     * @throws IllegalArgumentException value가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static UserContextId of(Long value) {
        return new UserContextId(value);
    }
}
