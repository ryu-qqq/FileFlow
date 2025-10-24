package com.ryuqq.fileflow.domain.iam.permission;

/**
 * Grant Record
 *
 * <p>권한 평가 결과를 나타내는 불변 데이터 구조입니다.
 * Java 21의 Record 패턴을 사용하여 간결하고 명확하게 표현합니다.</p>
 *
 * <p><strong>Grant 구성 요소:</strong></p>
 * <ul>
 *   <li>roleCode - 권한을 부여한 Role 코드</li>
 *   <li>permissionCode - 실제 권한 코드</li>
 *   <li>scope - 권한의 적용 범위</li>
 *   <li>conditionExpr - 권한 조건 표현식 (nullable)</li>
 * </ul>
 *
 * <p><strong>예시:</strong></p>
 * <pre>
 * Grant grant1 = new Grant("org.uploader", "file.upload", Scope.ORGANIZATION, null);
 * Grant grant2 = new Grant("tenant.admin", "file.delete", Scope.TENANT, "departmentId == 'IT'");
 * </pre>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Java 21 Record 패턴 사용</li>
 *   <li>✅ 불변성 보장 (Immutable)</li>
 *   <li>✅ 방어적 복사 및 유효성 검증</li>
 * </ul>
 *
 * @param roleCode Role 코드 (Not null)
 * @param permissionCode Permission 코드 (Not null)
 * @param scope 권한 적용 범위 (Not null)
 * @param conditionExpr 권한 조건 표현식 (Nullable - 조건 없음 가능)
 * @author ryu-qqq
 * @since 2025-10-24
 */
public record Grant(
    String roleCode,
    String permissionCode,
    Scope scope,
    String conditionExpr
) {

    /**
     * Grant Compact Constructor
     *
     * <p>Record 생성 시 자동으로 호출되어 유효성을 검증합니다.
     * Compact Constructor는 Record 패턴의 특징으로, 필드 할당 전에 검증 로직을 실행합니다.</p>
     *
     * @throws IllegalArgumentException roleCode, permissionCode, scope가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public Grant {
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalArgumentException("Role 코드는 필수입니다");
        }

        if (permissionCode == null || permissionCode.isBlank()) {
            throw new IllegalArgumentException("Permission 코드는 필수입니다");
        }

        if (scope == null) {
            throw new IllegalArgumentException("권한 적용 범위는 필수입니다");
        }

        // 문자열 정규화 (trim)
        roleCode = roleCode.trim();
        permissionCode = permissionCode.trim();

        // conditionExpr는 nullable이므로 null이거나 비어있을 때만 null로 정규화
        if (conditionExpr == null || conditionExpr.isBlank()) {
            conditionExpr = null;
        } else {
            conditionExpr = conditionExpr.trim();
        }
    }

    /**
     * 조건이 있는 Grant를 생성하는 정적 팩토리 메서드
     *
     * <p>조건 표현식이 있는 Grant를 명시적으로 생성할 때 사용합니다.</p>
     *
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     * @param scope 권한 적용 범위
     * @param conditionExpr 권한 조건 표현식
     * @return 생성된 Grant
     * @throws IllegalArgumentException roleCode, permissionCode, scope, conditionExpr가 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Grant withCondition(
        String roleCode,
        String permissionCode,
        Scope scope,
        String conditionExpr
    ) {
        if (conditionExpr == null || conditionExpr.trim().isEmpty()) {
            throw new IllegalArgumentException("조건 표현식은 필수입니다");
        }
        return new Grant(roleCode, permissionCode, scope, conditionExpr);
    }

    /**
     * 조건이 없는 Grant를 생성하는 정적 팩토리 메서드
     *
     * <p>조건 표현식이 없는 무조건 권한 Grant를 명시적으로 생성할 때 사용합니다.</p>
     *
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     * @param scope 권한 적용 범위
     * @return 생성된 Grant (conditionExpr = null)
     * @throws IllegalArgumentException roleCode, permissionCode, scope가 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Grant withoutCondition(
        String roleCode,
        String permissionCode,
        Scope scope
    ) {
        return new Grant(roleCode, permissionCode, scope, null);
    }

    /**
     * Grant에 조건 표현식이 있는지 확인합니다.
     *
     * <p>조건이 있는 권한인지 무조건 권한인지 구분할 때 사용합니다.</p>
     * <p>조건이 있으면 평가 로직이 필요하고, 조건이 없으면 무조건 허용됩니다.</p>
     *
     * <p><strong>사용 예시:</strong></p>
     * <pre>
     * if (grant.hasCondition()) {
     *     // 조건 평가 로직 실행
     *     boolean satisfied = evaluateCondition(grant.conditionExpr());
     *     return satisfied;
     * } else {
     *     // 무조건 허용
     *     return true;
     * }
     * </pre>
     *
     * @return 조건 표현식이 있으면 true, 없으면 false
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean hasCondition() {
        return conditionExpr != null && !conditionExpr.isEmpty();
    }

    /**
     * 주어진 Scope가 이 Grant의 scope에 포함되는지 확인합니다.
     *
     * <p>Grant의 적용 범위가 요청된 scope를 포함하는지 검증합니다.
     * 예: TENANT 권한은 ORGANIZATION, SELF 범위도 포함합니다.</p>
     *
     * @param requestedScope 확인할 Scope
     * @return Grant의 scope가 요청된 scope를 포함하면 true
     * @throws IllegalArgumentException requestedScope가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isApplicableToScope(Scope requestedScope) {
        if (requestedScope == null) {
            throw new IllegalArgumentException("확인할 Scope는 필수입니다");
        }
        return this.scope.includes(requestedScope);
    }

    /**
     * 이 Grant가 특정 Permission 코드에 대한 것인지 확인합니다.
     *
     * @param targetPermissionCode 확인할 Permission 코드
     * @return Permission 코드가 일치하면 true
     * @throws IllegalArgumentException targetPermissionCode가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isForPermission(String targetPermissionCode) {
        if (targetPermissionCode == null || targetPermissionCode.trim().isEmpty()) {
            throw new IllegalArgumentException("확인할 Permission 코드는 필수입니다");
        }
        return this.permissionCode.equalsIgnoreCase(targetPermissionCode.trim());
    }

    /**
     * 이 Grant가 특정 Role 코드로부터 부여된 것인지 확인합니다.
     *
     * @param targetRoleCode 확인할 Role 코드
     * @return Role 코드가 일치하면 true
     * @throws IllegalArgumentException targetRoleCode가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isFromRole(String targetRoleCode) {
        if (targetRoleCode == null || targetRoleCode.trim().isEmpty()) {
            throw new IllegalArgumentException("확인할 Role 코드는 필수입니다");
        }
        return this.roleCode.equalsIgnoreCase(targetRoleCode.trim());
    }

    /**
     * Grant의 문자열 표현을 반환합니다 (디버깅 및 로깅용).
     *
     * <p>Record 패턴의 기본 toString()을 Override하여 더 읽기 쉬운 형식으로 제공합니다.</p>
     *
     * @return Grant의 읽기 쉬운 문자열 표현
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public String toString() {
        if (hasCondition()) {
            return String.format(
                "Grant[role=%s, permission=%s, scope=%s, condition='%s']",
                roleCode, permissionCode, scope, conditionExpr
            );
        } else {
            return String.format(
                "Grant[role=%s, permission=%s, scope=%s, unconditional]",
                roleCode, permissionCode, scope
            );
        }
    }
}
