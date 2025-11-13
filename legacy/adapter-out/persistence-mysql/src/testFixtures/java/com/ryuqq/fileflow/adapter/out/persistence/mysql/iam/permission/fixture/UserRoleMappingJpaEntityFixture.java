package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.UserRoleMappingJpaEntity;

/**
 * UserRoleMappingJpaEntity Test Fixture
 *
 * <p>테스트에서 UserRoleMappingJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성 (ID 없음)
 * UserRoleMappingJpaEntity mapping = UserRoleMappingJpaEntityFixture.create();
 *
 * // 커스텀 생성
 * UserRoleMappingJpaEntity mapping = UserRoleMappingJpaEntityFixture.create(1L, "org.uploader", 1L, 1L);
 *
 * // ID 포함 생성
 * UserRoleMappingJpaEntity mapping = UserRoleMappingJpaEntityFixture.createWithId(1L, 1L, "org.uploader", 1L, 1L);
 * }</pre>
 *
 * @author windsurf
 * @since 1.0.0
 */
public class UserRoleMappingJpaEntityFixture {

    private static final Long DEFAULT_USER_CONTEXT_ID = 1L;
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private UserRoleMappingJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_ROLE_CODE = "org.uploader";
    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final Long DEFAULT_ORGANIZATION_ID = 1L;

    /**
     * 기본 UserRoleMappingJpaEntity 생성 (ID 없음)
     *
     * @return 새로운 UserRoleMappingJpaEntity
     */
    public static UserRoleMappingJpaEntity create() {
        return new UserRoleMappingJpaEntity(
            DEFAULT_USER_CONTEXT_ID,
            DEFAULT_ROLE_CODE,
            DEFAULT_TENANT_ID,
            DEFAULT_ORGANIZATION_ID
        );
    }

    /**
     * 커스텀 UserRoleMappingJpaEntity 생성 (ID 없음)
     *
     * @param userContextId UserContext ID
     * @param roleCode Role 코드
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return 새로운 UserRoleMappingJpaEntity
     */
    public static UserRoleMappingJpaEntity create(
        Long userContextId,
        String roleCode,
        Long tenantId,
        Long organizationId
    ) {
        return new UserRoleMappingJpaEntity(
            userContextId,
            roleCode,
            tenantId,
            organizationId
        );
    }

    /**
     * ID를 포함한 UserRoleMappingJpaEntity 생성
     *
     * @param id UserRoleMapping ID
     * @param userContextId UserContext ID
     * @param roleCode Role 코드
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return UserRoleMappingJpaEntity
     */
    public static UserRoleMappingJpaEntity createWithId(
        Long id,
        Long userContextId,
        String roleCode,
        Long tenantId,
        Long organizationId
    ) {
        return new UserRoleMappingJpaEntity(
            id,
            userContextId,
            roleCode,
            tenantId,
            organizationId
        );
    }

    /**
     * 특정 UserContext에 대한 여러 Role 매핑 생성 (ID 없음)
     *
     * @param userContextId UserContext ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @param roleCodes Role 코드 배열
     * @return UserRoleMappingJpaEntity 배열
     */
    public static UserRoleMappingJpaEntity[] createMultiple(
        Long userContextId,
        Long tenantId,
        Long organizationId,
        String... roleCodes
    ) {
        UserRoleMappingJpaEntity[] entities = new UserRoleMappingJpaEntity[roleCodes.length];
        for (int i = 0; i < roleCodes.length; i++) {
            entities[i] = create(userContextId, roleCodes[i], tenantId, organizationId);
        }
        return entities;
    }

    /**
     * 여러 개의 UserRoleMappingJpaEntity 생성 (ID 포함)
     *
     * @param count 생성할 개수
     * @return UserRoleMappingJpaEntity 배열
     */
    public static UserRoleMappingJpaEntity[] createMultipleWithId(int count) {
        UserRoleMappingJpaEntity[] entities = new UserRoleMappingJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId(
                (long) (i + 1),
                DEFAULT_USER_CONTEXT_ID,
                DEFAULT_ROLE_CODE + "." + (i + 1),
                DEFAULT_TENANT_ID,
                DEFAULT_ORGANIZATION_ID
            );
        }
        return entities;
    }
}
