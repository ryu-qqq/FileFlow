package com.ryuqq.fileflow.application.iam.permission.dto.context;

/**
 * Evaluation Context Record
 *
 * <p>권한 평가 시 필요한 사용자 컨텍스트를 나타내는 불변 데이터 구조입니다.</p>
 *
 * <p><strong>사용 목적:</strong></p>
 * <ul>
 *   <li>ABAC 조건 평가 시 {@code ctx.*} 변수로 바인딩</li>
 *   <li>Scope 매칭 시 조직/테넌트 식별</li>
 *   <li>Cache Lookup 키 생성 (user:tenant:org)</li>
 * </ul>
 *
 * <p><strong>예시:</strong></p>
 * <pre>
 * EvaluationContext ctx = new EvaluationContext(
 *     1001L,   // userId
 *     10L,     // tenantId
 *     100L,    // organizationId
 *     "ADMIN"  // roleCode (optional)
 * );
 * // ABAC 평가: "ctx.userId == res.ownerId"
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
 * @param userId 사용자 ID (Not null)
 * @param tenantId 테넌트 ID (Not null)
 * @param organizationId 조직 ID (Not null)
 * @param roleCode 역할 코드 (Nullable - ABAC 평가 시 선택적 사용)
 * @author ryu-qqq
 * @since 2025-10-25
 */
public record EvaluationContext(
    Long userId,
    Long tenantId,
    Long organizationId,
    String roleCode
) {

    /**
     * EvaluationContext Compact Constructor
     *
     * <p>Record 생성 시 자동으로 호출되어 유효성을 검증합니다.</p>
     *
     * @throws IllegalArgumentException userId, tenantId, organizationId가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public EvaluationContext {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("사용자 ID는 필수이며 양수여야 합니다");
        }

        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("테넌트 ID는 필수이며 양수여야 합니다");
        }

        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("조직 ID는 필수이며 양수여야 합니다");
        }

        // roleCode는 nullable이므로 null이거나 비어있을 때만 null로 정규화
        if (roleCode == null || roleCode.isBlank()) {
            roleCode = null;
        } else {
            roleCode = roleCode.trim();
        }
    }

    /**
     * Role 코드가 있는 Context를 생성하는 정적 팩토리 메서드
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param roleCode 역할 코드
     * @return 생성된 EvaluationContext
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static EvaluationContext withRole(
        Long userId,
        Long tenantId,
        Long organizationId,
        String roleCode
    ) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Role 코드는 필수입니다");
        }
        return new EvaluationContext(userId, tenantId, organizationId, roleCode);
    }

    /**
     * Role 코드가 없는 Context를 생성하는 정적 팩토리 메서드
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @return 생성된 EvaluationContext (roleCode = null)
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static EvaluationContext withoutRole(
        Long userId,
        Long tenantId,
        Long organizationId
    ) {
        return new EvaluationContext(userId, tenantId, organizationId, null);
    }

    /**
     * Cache Lookup 키 생성
     *
     * <p>Redis/Caffeine 등 캐시에서 Grants를 조회할 때 사용하는 키입니다.</p>
     *
     * <p><strong>키 포맷:</strong> {@code "user:{userId}:tenant:{tenantId}:org:{organizationId}"}</p>
     *
     * @return Cache 키 문자열
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String toCacheKey() {
        return String.format("user:%d:tenant:%d:org:%d", userId, tenantId, organizationId);
    }

    /**
     * Role 코드가 설정되어 있는지 확인
     *
     * @return Role 코드가 있으면 true, 없으면 false
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean hasRoleCode() {
        return roleCode != null && !roleCode.isEmpty();
    }

    /**
     * EvaluationContext의 문자열 표현을 반환합니다 (디버깅 및 로깅용)
     *
     * @return EvaluationContext의 읽기 쉬운 문자열 표현
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public String toString() {
        if (hasRoleCode()) {
            return String.format(
                "EvaluationContext[userId=%d, tenantId=%d, orgId=%d, role='%s']",
                userId, tenantId, organizationId, roleCode
            );
        } else {
            return String.format(
                "EvaluationContext[userId=%d, tenantId=%d, orgId=%d, no-role]",
                userId, tenantId, organizationId
            );
        }
    }
}
