package com.ryuqq.fileflow.domain.iam.permission.fixture;

import com.ryuqq.fileflow.domain.iam.permission.*;

/**
 * Grant Test Fixture
 *
 * <p>테스트에서 Grant 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class GrantFixture {

    private static final String DEFAULT_ROLE_CODE = "org.uploader";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private GrantFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_PERMISSION_CODE = "file.upload";
    private static final Scope DEFAULT_SCOPE = Scope.ORGANIZATION;

    public static Grant create() {
        return new Grant(DEFAULT_ROLE_CODE, DEFAULT_PERMISSION_CODE, DEFAULT_SCOPE, null);
    }

    public static Grant create(String roleCode, String permissionCode, Scope scope) {
        return new Grant(roleCode, permissionCode, scope, null);
    }

    public static Grant createWithCondition(String roleCode, String permissionCode, Scope scope, String condition) {
        return Grant.withCondition(roleCode, permissionCode, scope, condition);
    }

    public static Grant createWithoutCondition(String roleCode, String permissionCode, Scope scope) {
        return Grant.withoutCondition(roleCode, permissionCode, scope);
    }

    public static Grant createFileUploadGrant() {
        return new Grant("org.uploader", "file.upload", Scope.ORGANIZATION, null);
    }

    public static Grant createFileReadGrant() {
        return new Grant("org.viewer", "file.read", Scope.ORGANIZATION, null);
    }

    public static Grant createFileDeleteGrant() {
        return new Grant("org.admin", "file.delete", Scope.ORGANIZATION, null);
    }

    public static Grant createTenantAdminGrant() {
        return new Grant("tenant.admin", "tenant.manage", Scope.TENANT, null);
    }

    public static Grant createSystemAdminGrant() {
        return new Grant("system.admin", "system.manage", Scope.GLOBAL, null);
    }

    public static Grant createConditionalGrant() {
        return Grant.withCondition(
            "org.uploader",
            "file.upload",
            Scope.ORGANIZATION,
            "departmentId == 'IT'"
        );
    }

    public static java.util.List<Grant> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> new Grant(
                "role.code-" + i,
                "permission.code-" + i,
                Scope.ORGANIZATION,
                null
            ))
            .toList();
    }

    public static java.util.List<Grant> createMultipleWithScope(Scope scope, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> new Grant(
                "role.code-" + i,
                "permission.code-" + i,
                scope,
                null
            ))
            .toList();
    }
}
