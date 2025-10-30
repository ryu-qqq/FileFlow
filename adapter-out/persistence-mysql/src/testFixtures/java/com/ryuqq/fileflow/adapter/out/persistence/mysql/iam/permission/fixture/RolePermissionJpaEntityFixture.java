package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RolePermissionJpaEntity;

/**
 * RolePermissionJpaEntity Test Fixture
 *
 * <p>테스트에서 RolePermissionJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성 (ID 없음)
 * RolePermissionJpaEntity rp = RolePermissionJpaEntityFixture.create();
 *
 * // 커스텀 생성
 * RolePermissionJpaEntity rp = RolePermissionJpaEntityFixture.create("org.uploader", "file.upload");
 *
 * // ID 포함 생성
 * RolePermissionJpaEntity rp = RolePermissionJpaEntityFixture.createWithId(1L, "org.uploader", "file.upload");
 * }</pre>
 *
 * @author windsurf
 * @since 1.0.0
 */
public class RolePermissionJpaEntityFixture {

    private static final String DEFAULT_ROLE_CODE = "org.uploader";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private RolePermissionJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_PERMISSION_CODE = "file.upload";

    /**
     * 기본 RolePermissionJpaEntity 생성 (ID 없음)
     *
     * @return 새로운 RolePermissionJpaEntity
     */
    public static RolePermissionJpaEntity create() {
        return new RolePermissionJpaEntity(
            DEFAULT_ROLE_CODE,
            DEFAULT_PERMISSION_CODE
        );
    }

    /**
     * 커스텀 RolePermissionJpaEntity 생성 (ID 없음)
     *
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     * @return 새로운 RolePermissionJpaEntity
     */
    public static RolePermissionJpaEntity create(String roleCode, String permissionCode) {
        return new RolePermissionJpaEntity(
            roleCode,
            permissionCode
        );
    }

    /**
     * ID를 포함한 RolePermissionJpaEntity 생성
     *
     * @param id RolePermission ID
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     * @return RolePermissionJpaEntity
     */
    public static RolePermissionJpaEntity createWithId(Long id, String roleCode, String permissionCode) {
        return new RolePermissionJpaEntity(
            id,
            roleCode,
            permissionCode
        );
    }

    /**
     * 여러 개의 RolePermissionJpaEntity 생성 (ID 없음)
     *
     * @param roleCode Role 코드
     * @param permissionCodes Permission 코드 배열
     * @return RolePermissionJpaEntity 배열
     */
    public static RolePermissionJpaEntity[] createMultiple(String roleCode, String... permissionCodes) {
        RolePermissionJpaEntity[] entities = new RolePermissionJpaEntity[permissionCodes.length];
        for (int i = 0; i < permissionCodes.length; i++) {
            entities[i] = create(roleCode, permissionCodes[i]);
        }
        return entities;
    }

    /**
     * 여러 개의 RolePermissionJpaEntity 생성 (ID 포함)
     *
     * @param count 생성할 개수
     * @return RolePermissionJpaEntity 배열
     */
    public static RolePermissionJpaEntity[] createMultipleWithId(int count) {
        RolePermissionJpaEntity[] entities = new RolePermissionJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId(
                (long) (i + 1),
                DEFAULT_ROLE_CODE,
                DEFAULT_PERMISSION_CODE + "." + (i + 1)
            );
        }
        return entities;
    }
}
