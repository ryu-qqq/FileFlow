package com.ryuqq.fileflow.domain.iam.permission.fixture;

import com.ryuqq.fileflow.domain.iam.permission.*;

/**
 * RoleCode Test Fixture
 *
 * <p>테스트에서 RoleCode 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class RoleCodeFixture {

    private static final String DEFAULT_CODE = "org.uploader";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private RoleCodeFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }


    public static RoleCode create() {
        return RoleCode.of(DEFAULT_CODE);
    }

    public static RoleCode create(String value) {
        return RoleCode.of(value);
    }

    public static RoleCode createOrgUploader() {
        return RoleCode.of("org.uploader");
    }

    public static RoleCode createOrgAdmin() {
        return RoleCode.of("org.admin");
    }

    public static RoleCode createTenantAdmin() {
        return RoleCode.of("tenant.admin");
    }

    public static RoleCode createSystemViewer() {
        return RoleCode.of("system.viewer");
    }

    public static RoleCode createSystemAdmin() {
        return RoleCode.of("system.admin");
    }

    public static java.util.List<RoleCode> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> RoleCode.of("role.code-" + i))
            .toList();
    }

    public static java.util.List<RoleCode> createMultiple(String prefix, int count) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("prefix는 필수입니다");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> RoleCode.of(prefix + "." + i))
            .toList();
    }

    public static String invalidCodeTooShort() {
        return "ab";
    }

    public static String invalidCodeTooLong() {
        return "a".repeat(101);
    }

    public static String invalidCodeSpecialChars() {
        return "org@admin";
    }
}
