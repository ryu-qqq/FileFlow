package com.ryuqq.fileflow.domain.iam.permission.fixture;

import com.ryuqq.fileflow.domain.iam.permission.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

/**
 * Role Test Fixture
 *
 * <p>테스트에서 Role 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class RoleFixture {

    private static final String DEFAULT_CODE = "org.uploader";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private RoleFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_DESCRIPTION = "조직 업로더 역할";

    public static Role create() {
        return Role.of(
            RoleCode.of(DEFAULT_CODE),
            DEFAULT_DESCRIPTION,
            Set.of(PermissionCode.of("file.upload"), PermissionCode.of("file.read"))
        );
    }

    public static Role create(String code, String description, Set<PermissionCode> permissionCodes) {
        return Role.of(
            RoleCode.of(code),
            description,
            permissionCodes
        );
    }

    public static Role createOrgUploader() {
        return Role.of(
            RoleCode.of("org.uploader"),
            "조직 업로더 역할",
            Set.of(
                PermissionCode.of("file.upload"),
                PermissionCode.of("file.read")
            )
        );
    }

    public static Role createOrgAdmin() {
        return Role.of(
            RoleCode.of("org.admin"),
            "조직 관리자 역할",
            Set.of(
                PermissionCode.of("file.upload"),
                PermissionCode.of("file.read"),
                PermissionCode.of("file.delete"),
                PermissionCode.of("user.read"),
                PermissionCode.of("user.write")
            )
        );
    }

    public static Role createTenantAdmin() {
        return Role.of(
            RoleCode.of("tenant.admin"),
            "테넌트 관리자 역할",
            Set.of(
                PermissionCode.of("tenant.admin"),
                PermissionCode.of("user.read"),
                PermissionCode.of("user.write")
            )
        );
    }

    public static Role createSystemViewer() {
        return Role.of(
            RoleCode.of("system.viewer"),
            "시스템 조회자 역할",
            Set.of(
                PermissionCode.of("system.read"),
                PermissionCode.of("user.read")
            )
        );
    }

    public static Role createSystemAdmin() {
        return Role.of(
            RoleCode.of("system.admin"),
            "시스템 관리자 역할",
            Set.of(
                PermissionCode.of("system.admin"),
                PermissionCode.of("tenant.admin"),
                PermissionCode.of("user.write")
            )
        );
    }

    public static java.util.List<Role> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Role.of(
                RoleCode.of("role.code-" + i),
                "Role Description " + i,
                Set.of(PermissionCode.of("permission.code-" + i))
            ))
            .toList();
    }

    public static Role reconstitute(
        String code,
        String description,
        Set<PermissionCode> permissionCodes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        return Role.reconstitute(
            RoleCode.of(code),
            description,
            permissionCodes,
            createdAt,
            updatedAt,
            deletedAt
        );
    }

    public static Role createDeleted() {
        LocalDateTime now = LocalDateTime.now();
        return Role.reconstitute(
            RoleCode.of(DEFAULT_CODE),
            DEFAULT_DESCRIPTION,
            Set.of(PermissionCode.of("file.upload")),
            now,
            now,
            now
        );
    }

    public static RoleBuilder builder() {
        return new RoleBuilder();
    }

    public static class RoleBuilder {
        private String code = DEFAULT_CODE;
        private String description = DEFAULT_DESCRIPTION;
        private Set<PermissionCode> permissionCodes = Set.of(PermissionCode.of("file.upload"));
        private Clock clock = Clock.systemDefaultZone();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        public RoleBuilder code(String code) {
            this.code = code;
            return this;
        }

        public RoleBuilder description(String description) {
            this.description = description;
            return this;
        }

        public RoleBuilder permissionCodes(Set<PermissionCode> permissionCodes) {
            this.permissionCodes = permissionCodes;
            return this;
        }

        public RoleBuilder addPermissionCode(PermissionCode permissionCode) {
            this.permissionCodes = new java.util.HashSet<>(this.permissionCodes);
            this.permissionCodes.add(permissionCode);
            return this;
        }

        public RoleBuilder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public RoleBuilder fixedClock(Instant instant) {
            this.clock = Clock.fixed(instant, ZoneId.systemDefault());
            return this;
        }

        public RoleBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RoleBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public RoleBuilder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public Role build() {
            if (createdAt == null && updatedAt == null && deletedAt == null) {
                return Role.of(
                    RoleCode.of(code),
                    description,
                    permissionCodes
                );
            }

            LocalDateTime now = LocalDateTime.now(clock);
            return Role.reconstitute(
                RoleCode.of(code),
                description,
                permissionCodes,
                createdAt != null ? createdAt : now,
                updatedAt != null ? updatedAt : now,
                deletedAt
            );
        }
    }
}
