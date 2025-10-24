package com.ryuqq.fileflow.fixtures.iam.permission;

import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;
import com.ryuqq.fileflow.domain.iam.permission.Role;
import com.ryuqq.fileflow.domain.iam.permission.RoleCode;

import java.time.Clock;
import java.util.HashSet;
import java.util.Set;

/**
 * Role Test Fixture (Object Mother + Builder Pattern)
 *
 * <p>테스트에서 Role 객체를 쉽게 생성하기 위한 Fixture입니다.
 * Object Mother 패턴으로 자주 사용되는 Role을 미리 정의하고,
 * Builder 패턴으로 커스터마이징이 가능합니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * // 기본 ORG_UPLOADER Role
 * Role role = RoleFixture.orgUploader();
 *
 * // 커스터마이징
 * Role role = RoleFixture.builder()
 *     .code("custom.role")
 *     .description("Custom Role")
 *     .addPermission("file.upload")
 *     .addPermission("file.read")
 *     .build();
 * </pre>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public class RoleFixture {

    // ==================== Object Mother 패턴 ====================

    /**
     * org.uploader Role (file.upload, file.read 권한 포함)
     *
     * @return org.uploader Role
     */
    public static Role orgUploader() {
        Set<PermissionCode> permissions = new HashSet<>();
        permissions.add(PermissionCode.of("file.upload"));
        permissions.add(PermissionCode.of("file.read"));

        return Role.of(
            RoleCode.of("org.uploader"),
            "조직 내 업로더 역할",
            permissions
        );
    }

    /**
     * org.viewer Role (file.read, user.read 권한 포함)
     *
     * @return org.viewer Role
     */
    public static Role orgViewer() {
        Set<PermissionCode> permissions = new HashSet<>();
        permissions.add(PermissionCode.of("file.read"));
        permissions.add(PermissionCode.of("user.read"));

        return Role.of(
            RoleCode.of("org.viewer"),
            "조직 내 조회자 역할",
            permissions
        );
    }

    /**
     * org.admin Role (file.*, user.* 모든 권한 포함)
     *
     * @return org.admin Role
     */
    public static Role orgAdmin() {
        Set<PermissionCode> permissions = new HashSet<>();
        permissions.add(PermissionCode.of("file.upload"));
        permissions.add(PermissionCode.of("file.read"));
        permissions.add(PermissionCode.of("file.delete"));
        permissions.add(PermissionCode.of("user.read"));
        permissions.add(PermissionCode.of("user.write"));

        return Role.of(
            RoleCode.of("org.admin"),
            "조직 관리자 역할",
            permissions
        );
    }

    /**
     * tenant.admin Role (tenant.admin 권한 포함)
     *
     * @return tenant.admin Role
     */
    public static Role tenantAdmin() {
        Set<PermissionCode> permissions = new HashSet<>();
        permissions.add(PermissionCode.of("tenant.admin"));

        return Role.of(
            RoleCode.of("tenant.admin"),
            "테넌트 관리자 역할",
            permissions
        );
    }

    /**
     * system.admin Role (system.admin 권한 포함)
     *
     * @return system.admin Role
     */
    public static Role systemAdmin() {
        Set<PermissionCode> permissions = new HashSet<>();
        permissions.add(PermissionCode.of("system.admin"));

        return Role.of(
            RoleCode.of("system.admin"),
            "시스템 관리자 역할",
            permissions
        );
    }

    // ==================== Builder 패턴 ====================

    /**
     * Role Builder를 생성합니다.
     *
     * @return RoleBuilder
     */
    public static RoleBuilder builder() {
        return new RoleBuilder();
    }

    /**
     * Role Builder
     */
    public static class RoleBuilder {
        private String code = "test.role";
        private String description = "테스트 역할";
        private Set<PermissionCode> permissionCodes = new HashSet<>();
        private Clock clock = Clock.systemDefaultZone();

        /**
         * 기본 생성자 (최소 1개 Permission 보장)
         */
        public RoleBuilder() {
            // Role은 최소 1개 이상의 Permission 필요
            permissionCodes.add(PermissionCode.of("test.permission"));
        }

        /**
         * Role 코드를 설정합니다.
         *
         * @param code Role 코드
         * @return RoleBuilder
         */
        public RoleBuilder code(String code) {
            this.code = code;
            return this;
        }

        /**
         * Role 설명을 설정합니다.
         *
         * @param description Role 설명
         * @return RoleBuilder
         */
        public RoleBuilder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Permission을 추가합니다.
         *
         * @param permissionCode Permission 코드 문자열
         * @return RoleBuilder
         */
        public RoleBuilder addPermission(String permissionCode) {
            this.permissionCodes.add(PermissionCode.of(permissionCode));
            return this;
        }

        /**
         * Permission을 추가합니다.
         *
         * @param permissionCode PermissionCode 객체
         * @return RoleBuilder
         */
        public RoleBuilder addPermission(PermissionCode permissionCode) {
            this.permissionCodes.add(permissionCode);
            return this;
        }

        /**
         * Permission 목록을 설정합니다 (기존 Permission은 모두 제거됨).
         *
         * @param permissionCodes Permission 코드 Set
         * @return RoleBuilder
         */
        public RoleBuilder permissions(Set<PermissionCode> permissionCodes) {
            if (permissionCodes == null || permissionCodes.isEmpty()) {
                throw new IllegalArgumentException("Permission 코드는 최소 1개 이상 필요합니다");
            }
            this.permissionCodes = new HashSet<>(permissionCodes);
            return this;
        }

        /**
         * Clock을 설정합니다 (테스트 시간 제어용).
         *
         * @param clock Clock
         * @return RoleBuilder
         */
        public RoleBuilder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Role을 생성합니다.
         *
         * @return Role
         */
        public Role build() {
            return Role.of(
                RoleCode.of(code),
                description,
                permissionCodes
            );
        }
    }
}
