package com.ryuqq.fileflow.domain.iam.permission.fixture;

import com.ryuqq.fileflow.domain.iam.permission.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Permission Test Fixture
 *
 * <p>테스트에서 Permission 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class PermissionFixture {

    private static final String DEFAULT_CODE = "file.upload";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private PermissionFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_DESCRIPTION = "파일 업로드 권한";
    private static final Scope DEFAULT_SCOPE = Scope.ORGANIZATION;

    public static Permission create() {
        return Permission.of(
            PermissionCode.of(DEFAULT_CODE),
            DEFAULT_DESCRIPTION,
            DEFAULT_SCOPE
        );
    }

    public static Permission create(String code, String description, Scope scope) {
        return Permission.of(
            PermissionCode.of(code),
            description,
            scope
        );
    }

    public static Permission createFileUpload() {
        return Permission.of(
            PermissionCode.of("file.upload"),
            "파일 업로드 권한",
            Scope.ORGANIZATION
        );
    }

    public static Permission createFileRead() {
        return Permission.of(
            PermissionCode.of("file.read"),
            "파일 조회 권한",
            Scope.ORGANIZATION
        );
    }

    public static Permission createFileDelete() {
        return Permission.of(
            PermissionCode.of("file.delete"),
            "파일 삭제 권한",
            Scope.ORGANIZATION
        );
    }

    public static Permission createUserRead() {
        return Permission.of(
            PermissionCode.of("user.read"),
            "사용자 조회 권한",
            Scope.TENANT
        );
    }

    public static Permission createUserWrite() {
        return Permission.of(
            PermissionCode.of("user.write"),
            "사용자 수정 권한",
            Scope.TENANT
        );
    }

    public static Permission createTenantAdmin() {
        return Permission.of(
            PermissionCode.of("tenant.admin"),
            "테넌트 관리 권한",
            Scope.TENANT
        );
    }

    public static Permission createSystemAdmin() {
        return Permission.of(
            PermissionCode.of("system.admin"),
            "시스템 관리 권한",
            Scope.GLOBAL
        );
    }

    public static java.util.List<Permission> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Permission.of(
                PermissionCode.of("permission.code-" + i),
                "Permission Description " + i,
                Scope.ORGANIZATION
            ))
            .toList();
    }

    public static Permission reconstitute(
        String code,
        String description,
        Scope scope,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        return Permission.reconstitute(
            PermissionCode.of(code),
            description,
            scope,
            createdAt,
            updatedAt,
            deletedAt
        );
    }

    public static Permission createDeleted() {
        LocalDateTime now = LocalDateTime.now();
        return Permission.reconstitute(
            PermissionCode.of(DEFAULT_CODE),
            DEFAULT_DESCRIPTION,
            DEFAULT_SCOPE,
            now,
            now,
            now
        );
    }

    public static PermissionBuilder builder() {
        return new PermissionBuilder();
    }

    public static class PermissionBuilder {
        private String code = DEFAULT_CODE;
        private String description = DEFAULT_DESCRIPTION;
        private Scope scope = DEFAULT_SCOPE;
        private Clock clock = Clock.systemDefaultZone();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        public PermissionBuilder code(String code) {
            this.code = code;
            return this;
        }

        public PermissionBuilder description(String description) {
            this.description = description;
            return this;
        }

        public PermissionBuilder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public PermissionBuilder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public PermissionBuilder fixedClock(Instant instant) {
            this.clock = Clock.fixed(instant, ZoneId.systemDefault());
            return this;
        }

        public PermissionBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PermissionBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PermissionBuilder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public Permission build() {
            if (createdAt == null && updatedAt == null && deletedAt == null) {
                return Permission.of(
                    PermissionCode.of(code),
                    description,
                    scope
                );
            }

            LocalDateTime now = LocalDateTime.now(clock);
            return Permission.reconstitute(
                PermissionCode.of(code),
                description,
                scope,
                createdAt != null ? createdAt : now,
                updatedAt != null ? updatedAt : now,
                deletedAt
            );
        }
    }
}
