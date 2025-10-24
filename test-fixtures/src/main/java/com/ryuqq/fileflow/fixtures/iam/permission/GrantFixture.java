package com.ryuqq.fileflow.fixtures.iam.permission;

import com.ryuqq.fileflow.domain.iam.permission.Grant;
import com.ryuqq.fileflow.domain.iam.permission.Scope;

/**
 * Grant Test Fixture (Object Mother + Builder Pattern)
 *
 * <p>테스트에서 Grant 객체를 쉽게 생성하기 위한 Fixture입니다.
 * Object Mother 패턴으로 자주 사용되는 Grant를 미리 정의하고,
 * Builder 패턴으로 커스터마이징이 가능합니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * // 기본 fileUploadGrant
 * Grant grant = GrantFixture.fileUploadGrant();
 *
 * // 커스터마이징 (조건 없음)
 * Grant grant = GrantFixture.builder()
 *     .roleCode("org.uploader")
 *     .permissionCode("file.upload")
 *     .scope(Scope.ORGANIZATION)
 *     .build();
 *
 * // 커스터마이징 (조건 있음)
 * Grant grant = GrantFixture.builder()
 *     .roleCode("tenant.admin")
 *     .permissionCode("file.delete")
 *     .scope(Scope.TENANT)
 *     .condition("departmentId == 'IT'")
 *     .build();
 * </pre>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public class GrantFixture {

    // ==================== Object Mother 패턴 ====================

    /**
     * org.uploader 역할의 file.upload 권한 Grant (조건 없음)
     *
     * @return Grant
     */
    public static Grant fileUploadGrant() {
        return Grant.withoutCondition(
            "org.uploader",
            "file.upload",
            Scope.ORGANIZATION
        );
    }

    /**
     * org.uploader 역할의 file.read 권한 Grant (조건 없음)
     *
     * @return Grant
     */
    public static Grant fileReadGrant() {
        return Grant.withoutCondition(
            "org.uploader",
            "file.read",
            Scope.ORGANIZATION
        );
    }

    /**
     * org.admin 역할의 file.delete 권한 Grant (조건 없음)
     *
     * @return Grant
     */
    public static Grant fileDeleteGrant() {
        return Grant.withoutCondition(
            "org.admin",
            "file.delete",
            Scope.ORGANIZATION
        );
    }

    /**
     * org.admin 역할의 user.write 권한 Grant (조건 있음)
     *
     * @return Grant
     */
    public static Grant userWriteGrantWithCondition() {
        return Grant.withCondition(
            "org.admin",
            "user.write",
            Scope.ORGANIZATION,
            "departmentId == 'HR'"
        );
    }

    /**
     * tenant.admin 역할의 tenant.admin 권한 Grant (조건 없음)
     *
     * @return Grant
     */
    public static Grant tenantAdminGrant() {
        return Grant.withoutCondition(
            "tenant.admin",
            "tenant.admin",
            Scope.TENANT
        );
    }

    /**
     * system.admin 역할의 system.admin 권한 Grant (조건 없음)
     *
     * @return Grant
     */
    public static Grant systemAdminGrant() {
        return Grant.withoutCondition(
            "system.admin",
            "system.admin",
            Scope.GLOBAL
        );
    }

    // ==================== Builder 패턴 ====================

    /**
     * Grant Builder를 생성합니다.
     *
     * @return GrantBuilder
     */
    public static GrantBuilder builder() {
        return new GrantBuilder();
    }

    /**
     * Grant Builder
     */
    public static class GrantBuilder {
        private String roleCode = "test.role";
        private String permissionCode = "test.permission";
        private Scope scope = Scope.ORGANIZATION;
        private String conditionExpr = null;

        /**
         * Role 코드를 설정합니다.
         *
         * @param roleCode Role 코드
         * @return GrantBuilder
         */
        public GrantBuilder roleCode(String roleCode) {
            this.roleCode = roleCode;
            return this;
        }

        /**
         * Permission 코드를 설정합니다.
         *
         * @param permissionCode Permission 코드
         * @return GrantBuilder
         */
        public GrantBuilder permissionCode(String permissionCode) {
            this.permissionCode = permissionCode;
            return this;
        }

        /**
         * Scope를 설정합니다.
         *
         * @param scope Scope
         * @return GrantBuilder
         */
        public GrantBuilder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        /**
         * 조건 표현식을 설정합니다.
         *
         * @param conditionExpr 조건 표현식
         * @return GrantBuilder
         */
        public GrantBuilder condition(String conditionExpr) {
            this.conditionExpr = conditionExpr;
            return this;
        }

        /**
         * 조건 없는 Grant를 명시적으로 설정합니다.
         *
         * @return GrantBuilder
         */
        public GrantBuilder withoutCondition() {
            this.conditionExpr = null;
            return this;
        }

        /**
         * Grant를 생성합니다.
         *
         * @return Grant
         */
        public Grant build() {
            if (conditionExpr != null && !conditionExpr.trim().isEmpty()) {
                return Grant.withCondition(roleCode, permissionCode, scope, conditionExpr);
            } else {
                return Grant.withoutCondition(roleCode, permissionCode, scope);
            }
        }
    }
}
