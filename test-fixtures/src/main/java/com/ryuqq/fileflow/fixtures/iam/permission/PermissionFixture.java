package com.ryuqq.fileflow.fixtures.iam.permission;

import com.ryuqq.fileflow.domain.iam.permission.Permission;
import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;
import com.ryuqq.fileflow.domain.iam.permission.Scope;

import java.time.Clock;

/**
 * Permission Test Fixture (Object Mother + Builder Pattern)
 *
 * <p>테스트에서 Permission 객체를 쉽게 생성하기 위한 Fixture입니다.
 * Object Mother 패턴으로 자주 사용되는 Permission을 미리 정의하고,
 * Builder 패턴으로 커스터마이징이 가능합니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * // 기본 FILE_UPLOAD Permission
 * Permission permission = PermissionFixture.fileUpload();
 *
 * // 커스터마이징
 * Permission permission = PermissionFixture.builder()
 *     .code("custom.permission")
 *     .description("Custom Permission")
 *     .scope(Scope.TENANT)
 *     .build();
 * </pre>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public class PermissionFixture {

    // ==================== Object Mother 패턴 ====================

    /**
     * file.upload Permission (ORGANIZATION 범위)
     *
     * @return file.upload Permission
     */
    public static Permission fileUpload() {
        return Permission.of(
            PermissionCode.of("file.upload"),
            "파일 업로드 권한",
            Scope.ORGANIZATION
        );
    }

    /**
     * file.read Permission (SELF 범위)
     *
     * @return file.read Permission
     */
    public static Permission fileRead() {
        return Permission.of(
            PermissionCode.of("file.read"),
            "파일 조회 권한",
            Scope.SELF
        );
    }

    /**
     * file.delete Permission (ORGANIZATION 범위)
     *
     * @return file.delete Permission
     */
    public static Permission fileDelete() {
        return Permission.of(
            PermissionCode.of("file.delete"),
            "파일 삭제 권한",
            Scope.ORGANIZATION
        );
    }

    /**
     * user.read Permission (ORGANIZATION 범위)
     *
     * @return user.read Permission
     */
    public static Permission userRead() {
        return Permission.of(
            PermissionCode.of("user.read"),
            "사용자 조회 권한",
            Scope.ORGANIZATION
        );
    }

    /**
     * user.write Permission (ORGANIZATION 범위)
     *
     * @return user.write Permission
     */
    public static Permission userWrite() {
        return Permission.of(
            PermissionCode.of("user.write"),
            "사용자 수정 권한",
            Scope.ORGANIZATION
        );
    }

    /**
     * tenant.admin Permission (TENANT 범위)
     *
     * @return tenant.admin Permission
     */
    public static Permission tenantAdmin() {
        return Permission.of(
            PermissionCode.of("tenant.admin"),
            "테넌트 관리자 권한",
            Scope.TENANT
        );
    }

    /**
     * system.admin Permission (GLOBAL 범위)
     *
     * @return system.admin Permission
     */
    public static Permission systemAdmin() {
        return Permission.of(
            PermissionCode.of("system.admin"),
            "시스템 관리자 권한",
            Scope.GLOBAL
        );
    }

    // ==================== Builder 패턴 ====================

    /**
     * Permission Builder를 생성합니다.
     *
     * @return PermissionBuilder
     */
    public static PermissionBuilder builder() {
        return new PermissionBuilder();
    }

    /**
     * Permission Builder
     */
    public static class PermissionBuilder {
        private String code = "test.permission";
        private String description = "테스트 권한";
        private Scope scope = Scope.ORGANIZATION;
        private Clock clock = Clock.systemDefaultZone();

        /**
         * Permission 코드를 설정합니다.
         *
         * @param code Permission 코드
         * @return PermissionBuilder
         */
        public PermissionBuilder code(String code) {
            this.code = code;
            return this;
        }

        /**
         * Permission 설명을 설정합니다.
         *
         * @param description Permission 설명
         * @return PermissionBuilder
         */
        public PermissionBuilder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Permission 범위를 설정합니다.
         *
         * @param scope Permission 범위
         * @return PermissionBuilder
         */
        public PermissionBuilder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Clock을 설정합니다 (테스트 시간 제어용).
         *
         * @param clock Clock
         * @return PermissionBuilder
         */
        public PermissionBuilder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Permission을 생성합니다.
         *
         * @return Permission
         */
        public Permission build() {
            return Permission.of(
                PermissionCode.of(code),
                description,
                scope
            );
        }
    }
}
