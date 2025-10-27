package com.ryuqq.fileflow.domain.iam.usercontext;

import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

/**
 * 조직 멤버십
 *
 * <p>사용자가 특정 테넌트의 조직에 소속된 정보를 나타내는 Value Object입니다.</p>
 * <p>한 사용자는 여러 테넌트의 여러 조직에 소속될 수 있으며, 각 소속마다 다른 역할을 가질 수 있습니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용</li>
 *   <li>✅ Long FK 전략 (TenantId, OrganizationId)</li>
 *   <li>✅ 불변 객체 (Immutable)</li>
 *   <li>✅ null 검증</li>
 *   <li>❌ Lombok 사용 안함</li>
 *   <li>❌ JPA 관계 어노테이션 사용 안함</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * // 사용자가 tenant-1의 organization-1에 EMPLOYEE로 소속
 * Membership membership = Membership.of(
 *     TenantId.of("tenant-1"),
 *     OrganizationId.of(1L),
 *     MembershipType.EMPLOYEE
 * );
 * </pre>
 *
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 * @param type 멤버십 타입
 * @author ryu-qqq
 * @since 2025-10-24
 */
public record Membership(
    TenantId tenantId,
    OrganizationId organizationId,
    MembershipType type
) {

    /**
     * Membership의 Compact Constructor
     *
     * <p>null 값을 검증합니다.</p>
     *
     * @throws IllegalArgumentException tenantId, organizationId, type 중 하나라도 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public Membership {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID는 필수입니다");
        }
        if (type == null) {
            throw new IllegalArgumentException("Membership 타입은 필수입니다");
        }
    }

    /**
     * Membership 생성 - Static Factory Method
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param type 멤버십 타입
     * @return Membership 인스턴스
     * @throws IllegalArgumentException tenantId, organizationId, type 중 하나라도 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Membership of(TenantId tenantId, OrganizationId organizationId, MembershipType type) {
        return new Membership(tenantId, organizationId, type);
    }

    /**
     * 특정 테넌트의 멤버십인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 내부 구조를 노출하지 않고 행위만 제공합니다.</p>
     * <p>❌ Bad: membership.tenantId().equals(targetTenantId)</p>
     * <p>✅ Good: membership.belongsToTenant(targetTenantId)</p>
     *
     * @param targetTenantId 확인할 테넌트 ID
     * @return 해당 테넌트의 멤버십이면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean belongsToTenant(TenantId targetTenantId) {
        return this.tenantId.equals(targetTenantId);
    }

    /**
     * 특정 조직의 멤버십인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 내부 구조를 노출하지 않고 행위만 제공합니다.</p>
     * <p>❌ Bad: membership.organizationId().equals(targetOrgId)</p>
     * <p>✅ Good: membership.belongsToOrganization(targetOrgId)</p>
     *
     * @param targetOrganizationId 확인할 조직 ID
     * @return 해당 조직의 멤버십이면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean belongsToOrganization(OrganizationId targetOrganizationId) {
        return this.organizationId.equals(targetOrganizationId);
    }

    /**
     * 특정 테넌트와 조직의 멤버십인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 복합 조건을 메서드로 캡슐화합니다.</p>
     *
     * @param targetTenantId 확인할 테넌트 ID
     * @param targetOrganizationId 확인할 조직 ID
     * @return 해당 테넌트와 조직의 멤버십이면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean belongsTo(TenantId targetTenantId, OrganizationId targetOrganizationId) {
        return this.tenantId.equals(targetTenantId) && this.organizationId.equals(targetOrganizationId);
    }

    /**
     * 시스템 멤버십인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 타입 확인 로직을 캡슐화합니다.</p>
     * <p>❌ Bad: membership.type().isSystem()</p>
     * <p>✅ Good: membership.isSystem()</p>
     *
     * @return 시스템 멤버십이면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isSystem() {
        return this.type.isSystem();
    }

    /**
     * 게스트 멤버십인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 타입 확인 로직을 캡슐화합니다.</p>
     * <p>❌ Bad: membership.type().isGuest()</p>
     * <p>✅ Good: membership.isGuest()</p>
     *
     * @return 게스트 멤버십이면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isGuest() {
        return this.type.isGuest();
    }

    /**
     * 판매자 멤버십인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 타입 확인 로직을 캡슐화합니다.</p>
     * <p>❌ Bad: membership.type().isSellerMember()</p>
     * <p>✅ Good: membership.isSellerMember()</p>
     *
     * @return 판매자 멤버십이면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isSellerMember() {
        return this.type.isSellerMember();
    }

    /**
     * Tenant ID 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: membership.tenantId().value()</p>
     * <p>✅ Good: membership.getTenantIdValue()</p>
     *
     * @return Tenant ID 원시 값 (Long - Tenant PK 타입과 일치)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public Long getTenantIdValue() {
        return tenantId.value();
    }

    /**
     * Organization ID 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: membership.organizationId().value()</p>
     * <p>✅ Good: membership.getOrganizationIdValue()</p>
     *
     * @return Organization ID 원시 값
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public Long getOrganizationIdValue() {
        return organizationId.value();
    }
}
