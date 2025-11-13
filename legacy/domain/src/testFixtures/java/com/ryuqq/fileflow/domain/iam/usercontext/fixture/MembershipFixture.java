package com.ryuqq.fileflow.domain.iam.usercontext.fixture;

import com.ryuqq.fileflow.domain.iam.usercontext.*;

import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

/**
 * Membership Test Fixture
 *
 * <p>테스트에서 Membership 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 기본 Membership (Tenant 1, Organization 1, EMPLOYEE)
 * Membership membership = MembershipFixture.create();
 *
 * // 특정 Membership
 * Membership membership = MembershipFixture.create(1L, 1L, MembershipType.SYSTEM);
 *
 * // 여러 Membership 생성
 * List<Membership> memberships = MembershipFixture.createMultiple(5);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class MembershipFixture {

    private static final Long DEFAULT_TENANT_ID = 1L;
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private MembershipFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final Long DEFAULT_ORGANIZATION_ID = 1L;
    private static final MembershipType DEFAULT_TYPE = MembershipType.EMPLOYEE;

    /**
     * 기본 Membership을 생성합니다.
     *
     * <p>기본값:</p>
     * <ul>
     *   <li>tenantId: 1L</li>
     *   <li>organizationId: 1L</li>
     *   <li>type: EMPLOYEE</li>
     * </ul>
     *
     * @return Membership 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Membership create() {
        return Membership.of(
            TenantId.of(DEFAULT_TENANT_ID),
            OrganizationId.of(DEFAULT_ORGANIZATION_ID),
            DEFAULT_TYPE
        );
    }

    /**
     * 특정 값으로 Membership을 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @param type Membership 타입
     * @return Membership 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Membership create(Long tenantId, Long organizationId, MembershipType type) {
        return Membership.of(
            TenantId.of(tenantId),
            OrganizationId.of(organizationId),
            type
        );
    }

    /**
     * EMPLOYEE 타입의 Membership을 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return Membership 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Membership createEmployee(Long tenantId, Long organizationId) {
        return Membership.of(
            TenantId.of(tenantId),
            OrganizationId.of(organizationId),
            MembershipType.EMPLOYEE
        );
    }

    /**
     * SELLER_MEMBER 타입의 Membership을 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return Membership 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Membership createSellerMember(Long tenantId, Long organizationId) {
        return Membership.of(
            TenantId.of(tenantId),
            OrganizationId.of(organizationId),
            MembershipType.SELLER_MEMBER
        );
    }

    /**
     * GUEST 타입의 Membership을 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return Membership 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Membership createGuest(Long tenantId, Long organizationId) {
        return Membership.of(
            TenantId.of(tenantId),
            OrganizationId.of(organizationId),
            MembershipType.GUEST
        );
    }

    /**
     * SYSTEM 타입의 Membership을 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return Membership 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Membership createSystem(Long tenantId, Long organizationId) {
        return Membership.of(
            TenantId.of(tenantId),
            OrganizationId.of(organizationId),
            MembershipType.SYSTEM
        );
    }

    /**
     * 여러 개의 Membership을 생성합니다.
     *
     * <p>같은 Tenant의 여러 Organization에 대한 EMPLOYEE 멤버십을 생성합니다.</p>
     * <p>organizationId는 1부터 시작하는 연속된 값을 사용합니다.</p>
     *
     * @param count 생성할 Membership 개수
     * @return Membership 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<Membership> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Membership.of(
                TenantId.of(DEFAULT_TENANT_ID),
                OrganizationId.of((long) i),
                DEFAULT_TYPE
            ))
            .toList();
    }

    /**
     * 특정 Tenant의 여러 Organization에 대한 Membership을 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param count 생성할 Membership 개수
     * @return Membership 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<Membership> createMultiple(Long tenantId, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Membership.of(
                TenantId.of(tenantId),
                OrganizationId.of((long) i),
                DEFAULT_TYPE
            ))
            .toList();
    }

    /**
     * 특정 Tenant와 타입으로 여러 Organization에 대한 Membership을 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param count 생성할 Membership 개수
     * @param type Membership 타입
     * @return Membership 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<Membership> createMultiple(Long tenantId, int count, MembershipType type) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Membership.of(
                TenantId.of(tenantId),
                OrganizationId.of((long) i),
                type
            ))
            .toList();
    }
}
