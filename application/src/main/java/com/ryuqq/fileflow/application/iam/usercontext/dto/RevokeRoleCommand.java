package com.ryuqq.fileflow.application.iam.usercontext.dto;

/**
 * Role 철회 Command DTO
 *
 * <p>사용자의 Role(Membership)을 철회하기 위한 Command 객체입니다.</p>
 * <p>Application Layer의 UseCase 입력으로 사용됩니다.</p>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>직원이 퇴사하여 조직 권한 회수 시</li>
 *   <li>관리자에서 일반 직원으로 강등 시 (기존 MANAGER 역할 철회)</li>
 *   <li>외부 파트너와의 계약 종료로 접근 권한 회수 시</li>
 * </ul>
 *
 * <p><strong>Command → Domain 변환:</strong></p>
 * <pre>
 * RevokeRoleCommand command = ...;
 *
 * // UseCase에서 Domain 객체로 변환
 * userContext.revokeMembership(
 *     TenantId.of(command.tenantId()),
 *     OrganizationId.of(command.organizationId())
 * );
 * </pre>
 *
 * <p><strong>참고:</strong></p>
 * <ul>
 *   <li>Membership 철회는 tenantId + organizationId 조합으로 식별됩니다</li>
 *   <li>membershipType은 철회 시 필요하지 않습니다 (이미 DB에 존재)</li>
 *   <li>철회 성공 시 해당 사용자의 캐시가 무효화됩니다</li>
 * </ul>
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
 * @param tenantId 테넌트 ID (Long - Tenant PK 타입과 일치)
 * @param organizationId 조직 ID (원시 Long)
 * @author ryu-qqq
 * @since 2025-10-26
 */
public record RevokeRoleCommand(
    Long userId,
    Long tenantId,
    Long organizationId
) {

    /**
     * Compact Constructor - null 및 유효성 검증
     *
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public RevokeRoleCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId는 필수이며 0보다 큰 양수여야 합니다");
        }
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("tenantId는 필수이며 0보다 큰 양수여야 합니다");
        }
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId는 필수이며 0보다 큰 양수여야 합니다");
        }
    }

    /**
     * Static Factory Method
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID (Long - Tenant PK 타입과 일치)
     * @param organizationId 조직 ID
     * @return RevokeRoleCommand 인스턴스
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static RevokeRoleCommand of(
        Long userId,
        Long tenantId,
        Long organizationId
    ) {
        return new RevokeRoleCommand(userId, tenantId, organizationId);
    }
}
