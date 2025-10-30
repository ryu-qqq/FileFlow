package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserOrgMembershipJpaEntity;

/**
 * UserOrgMembershipJpaEntity Test Fixture
 *
 * <p>테스트에서 UserOrgMembershipJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성 (ID 없음)
 * UserOrgMembershipJpaEntity membership = UserOrgMembershipJpaEntityFixture.create();
 *
 * // 커스텀 생성
 * UserOrgMembershipJpaEntity membership = UserOrgMembershipJpaEntityFixture.create(1L, 1L, 1L, "ADMIN");
 *
 * // ID 포함 생성
 * UserOrgMembershipJpaEntity membership = UserOrgMembershipJpaEntityFixture.createWithId(1L, 1L, 1L, 1L, "MEMBER");
 * }</pre>
 *
 * @author windsurf
 * @since 1.0.0
 */
public class UserOrgMembershipJpaEntityFixture {

    private static final Long DEFAULT_USER_CONTEXT_ID = 1L;
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private UserOrgMembershipJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final Long DEFAULT_ORGANIZATION_ID = 1L;
    private static final String DEFAULT_MEMBERSHIP_TYPE = "MEMBER";

    /**
     * 기본 UserOrgMembershipJpaEntity 생성 (ID 없음)
     *
     * @return 새로운 UserOrgMembershipJpaEntity
     */
    public static UserOrgMembershipJpaEntity create() {
        return UserOrgMembershipJpaEntity.create(
            DEFAULT_USER_CONTEXT_ID,
            DEFAULT_TENANT_ID,
            DEFAULT_ORGANIZATION_ID,
            DEFAULT_MEMBERSHIP_TYPE
        );
    }

    /**
     * 커스텀 UserOrgMembershipJpaEntity 생성 (ID 없음)
     *
     * @param userContextId UserContext ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @param membershipType Membership 타입
     * @return 새로운 UserOrgMembershipJpaEntity
     */
    public static UserOrgMembershipJpaEntity create(
        Long userContextId,
        Long tenantId,
        Long organizationId,
        String membershipType
    ) {
        return UserOrgMembershipJpaEntity.create(
            userContextId,
            tenantId,
            organizationId,
            membershipType
        );
    }

    /**
     * ID를 포함한 UserOrgMembershipJpaEntity 생성 (재구성)
     *
     * @param id Membership ID
     * @param userContextId UserContext ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @param membershipType Membership 타입
     * @return 재구성된 UserOrgMembershipJpaEntity
     */
    public static UserOrgMembershipJpaEntity createWithId(
        Long id,
        Long userContextId,
        Long tenantId,
        Long organizationId,
        String membershipType
    ) {
        return UserOrgMembershipJpaEntity.reconstitute(
            id,
            userContextId,
            tenantId,
            organizationId,
            membershipType
        );
    }

    /**
     * OWNER 타입의 UserOrgMembershipJpaEntity 생성 (ID 없음)
     *
     * @param userContextId UserContext ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return OWNER 타입의 UserOrgMembershipJpaEntity
     */
    public static UserOrgMembershipJpaEntity createOwner(
        Long userContextId,
        Long tenantId,
        Long organizationId
    ) {
        return UserOrgMembershipJpaEntity.create(
            userContextId,
            tenantId,
            organizationId,
            "OWNER"
        );
    }

    /**
     * ADMIN 타입의 UserOrgMembershipJpaEntity 생성 (ID 없음)
     *
     * @param userContextId UserContext ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return ADMIN 타입의 UserOrgMembershipJpaEntity
     */
    public static UserOrgMembershipJpaEntity createAdmin(
        Long userContextId,
        Long tenantId,
        Long organizationId
    ) {
        return UserOrgMembershipJpaEntity.create(
            userContextId,
            tenantId,
            organizationId,
            "ADMIN"
        );
    }

    /**
     * 특정 UserContext에 대한 여러 Organization 멤버십 생성 (ID 없음)
     *
     * @param userContextId UserContext ID
     * @param tenantId Tenant ID
     * @param organizationIds Organization ID 배열
     * @return UserOrgMembershipJpaEntity 배열
     */
    public static UserOrgMembershipJpaEntity[] createMultiple(
        Long userContextId,
        Long tenantId,
        Long... organizationIds
    ) {
        UserOrgMembershipJpaEntity[] entities = new UserOrgMembershipJpaEntity[organizationIds.length];
        for (int i = 0; i < organizationIds.length; i++) {
            entities[i] = create(userContextId, tenantId, organizationIds[i], DEFAULT_MEMBERSHIP_TYPE);
        }
        return entities;
    }

    /**
     * 여러 개의 UserOrgMembershipJpaEntity 생성 (ID 포함)
     *
     * @param count 생성할 개수
     * @return UserOrgMembershipJpaEntity 배열
     */
    public static UserOrgMembershipJpaEntity[] createMultipleWithId(int count) {
        UserOrgMembershipJpaEntity[] entities = new UserOrgMembershipJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId(
                (long) (i + 1),
                DEFAULT_USER_CONTEXT_ID,
                DEFAULT_TENANT_ID,
                (long) (i + 1),
                DEFAULT_MEMBERSHIP_TYPE
            );
        }
        return entities;
    }
}
