package com.ryuqq.fileflow.application.iam.usercontext.dto;

/**
 * Role 할당 Command DTO
 *
 * <p>사용자에게 새로운 Role(Membership)을 할당하기 위한 Command 객체입니다.</p>
 * <p>Application Layer의 UseCase 입력으로 사용됩니다.</p>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>신규 직원이 조직에 입사 시 (EMPLOYEE 역할 할당)</li>
 *   <li>기존 직원이 관리자로 승진 시 (MANAGER 역할 할당)</li>
 *   <li>외부 파트너에게 접근 권한 부여 시 (PARTNER 역할 할당)</li>
 * </ul>
 *
 * <p><strong>Command → Domain 변환:</strong></p>
 * <pre>
 * AssignRoleCommand command = ...;
 *
 * // UseCase에서 Domain 객체로 변환
 * Membership membership = Membership.of(
 *     TenantId.of(command.tenantId()),
 *     OrganizationId.of(command.organizationId()),
 *     MembershipType.valueOf(command.membershipType())
 * );
 * </pre>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용</li>
 *   <li>✅ 불변 객체 (Immutable)</li>
 *   <li>✅ null 검증</li>
 *   <li>❌ Lombok 사용 안함</li>
 *   <li>❌ Domain 객체 직접 포함 금지 (원시 타입 + String만)</li>
 * </ul>
 *
 * @param userId 사용자 ID
 * @param tenantId 테넌트 ID (원시 문자열)
 * @param organizationId 조직 ID (원시 Long)
 * @param membershipType Membership 타입 문자열 (EMPLOYEE, MANAGER, ADMIN 등)
 * @author ryu-qqq
 * @since 2025-10-26
 */
public record AssignRoleCommand(
    Long userId,
    String tenantId,
    Long organizationId,
    String membershipType
) {

    /**
     * Compact Constructor - null 및 유효성 검증
     *
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public AssignRoleCommand {
        if (userId == null || userId < 0) {
            throw new IllegalArgumentException("userId는 필수이며 0 이상이어야 합니다");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId는 필수입니다");
        }
        if (organizationId == null || organizationId < 0) {
            throw new IllegalArgumentException("organizationId는 필수이며 0 이상이어야 합니다");
        }
        if (membershipType == null || membershipType.isBlank()) {
            throw new IllegalArgumentException("membershipType은 필수입니다");
        }
    }

    /**
     * Static Factory Method
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param membershipType Membership 타입
     * @return AssignRoleCommand 인스턴스
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static AssignRoleCommand of(
        Long userId,
        String tenantId,
        Long organizationId,
        String membershipType
    ) {
        return new AssignRoleCommand(userId, tenantId, organizationId, membershipType);
    }
}
