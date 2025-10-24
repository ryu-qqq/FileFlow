package com.ryuqq.fileflow.domain.iam.permission;

/**
 * 권한의 적용 범위를 나타내는 Enum
 *
 * <p>FileFlow 시스템에서 권한이 적용되는 범위를 정의합니다.
 * 계층적 구조를 가지며, 상위 범위는 하위 범위를 포함합니다.</p>
 *
 * <p><strong>계층 구조:</strong></p>
 * <pre>
 * GLOBAL (최상위)
 *   ↓
 * TENANT
 *   ↓
 * ORGANIZATION
 *   ↓
 * SELF (최하위)
 * </pre>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Enum 타입으로 타입 안전성 보장</li>
 *   <li>✅ 불변성 보장</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public enum Scope {

    /**
     * 자기 자신에게만 적용되는 범위
     *
     * <p>예: 사용자 자신의 프로필 수정</p>
     */
    SELF("SELF", "자기 자신", 1),

    /**
     * 조직 내에서 적용되는 범위
     *
     * <p>예: 같은 조직의 사용자 관리</p>
     */
    ORGANIZATION("ORGANIZATION", "조직", 2),

    /**
     * 테넌트 내에서 적용되는 범위
     *
     * <p>예: 테넌트 내 모든 조직과 사용자 관리</p>
     */
    TENANT("TENANT", "테넌트", 3),

    /**
     * 시스템 전체에 적용되는 범위
     *
     * <p>예: 시스템 설정, 모든 테넌트 관리</p>
     */
    GLOBAL("GLOBAL", "전역", 4);

    private final String code;
    private final String description;
    private final int level;

    /**
     * Scope를 생성합니다.
     *
     * @param code 범위 코드
     * @param description 범위 설명
     * @param level 계층 레벨 (1: 최하위, 4: 최상위)
     */
    Scope(String code, String description, int level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }

    /**
     * 범위 코드를 반환합니다.
     *
     * @return 범위 코드
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getCode() {
        return code;
    }

    /**
     * 범위 설명을 반환합니다.
     *
     * @return 범위 설명
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getDescription() {
        return description;
    }

    /**
     * 계층 레벨을 반환합니다.
     *
     * @return 계층 레벨 (1: 최하위, 4: 최상위)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public int getLevel() {
        return level;
    }

    /**
     * 다른 Scope를 포함하는지 확인합니다.
     *
     * <p>상위 범위는 하위 범위를 포함합니다.
     * 예: TENANT는 ORGANIZATION과 SELF를 포함합니다.</p>
     *
     * @param other 비교할 Scope
     * @return 포함 여부
     * @throws IllegalArgumentException other가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean includes(Scope other) {
        if (other == null) {
            throw new IllegalArgumentException("비교 대상 Scope는 필수입니다");
        }
        return this.level >= other.level;
    }

    /**
     * 코드로부터 Scope를 찾아 반환합니다.
     *
     * @param code 범위 코드
     * @return 해당하는 Scope
     * @throws IllegalArgumentException 일치하는 Scope가 없는 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Scope fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Scope 코드는 필수입니다");
        }

        for (Scope scope : values()) {
            if (scope.code.equalsIgnoreCase(code.trim())) {
                return scope;
            }
        }

        throw new IllegalArgumentException("알 수 없는 Scope 코드: " + code);
    }
}
