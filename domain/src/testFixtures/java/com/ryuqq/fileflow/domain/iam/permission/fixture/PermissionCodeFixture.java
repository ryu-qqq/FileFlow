package com.ryuqq.fileflow.domain.iam.permission.fixture;

import com.ryuqq.fileflow.domain.iam.permission.*;

/**
 * PermissionCode Test Fixture
 *
 * <p>테스트에서 PermissionCode 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class PermissionCodeFixture {

    private static final String DEFAULT_CODE = "file.upload";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private PermissionCodeFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }


    public static PermissionCode create() {
        return PermissionCode.of(DEFAULT_CODE);
    }

    public static PermissionCode create(String value) {
        return PermissionCode.of(value);
    }

    public static PermissionCode createFileUpload() {
        return PermissionCode.of("file.upload");
    }

    public static PermissionCode createFileRead() {
        return PermissionCode.of("file.read");
    }

    public static PermissionCode createFileDelete() {
        return PermissionCode.of("file.delete");
    }

    public static PermissionCode createUserRead() {
        return PermissionCode.of("user.read");
    }

    public static PermissionCode createUserWrite() {
        return PermissionCode.of("user.write");
    }

    public static PermissionCode createTenantAdmin() {
        return PermissionCode.of("tenant.admin");
    }

    public static PermissionCode createOrgAdmin() {
        return PermissionCode.of("org.admin");
    }

    public static java.util.List<PermissionCode> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> PermissionCode.of("permission.code-" + i))
            .toList();
    }

    public static java.util.List<PermissionCode> createMultiple(String prefix, int count) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("prefix는 필수입니다");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> PermissionCode.of(prefix + "." + i))
            .toList();
    }

    public static String invalidCodeTooShort() {
        return "ab";
    }

    public static String invalidCodeTooLong() {
        return "a".repeat(101);
    }

    public static String invalidCodeSpecialChars() {
        return "file@upload";
    }
}
