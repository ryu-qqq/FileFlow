package com.ryuqq.fileflow.domain.iam.permission.fixture;

import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;
import com.ryuqq.fileflow.domain.iam.permission.Role;
import com.ryuqq.fileflow.domain.iam.permission.RoleCode;

import java.util.HashSet;
import java.util.Set;

/**
 * RoleDomain 테스트 Fixture
 *
 * <p>테스트에서 Role Domain 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 * <p>Object Mother 패턴을 사용하여 자주 사용되는 Role을 미리 정의합니다.</p>
 *
 * <h3>네이밍 규칙:</h3>
 * <ul>
 *   <li>클래스명: {@code *Fixture} 접미사 필수</li>
 *   <li>기본 생성 메서드: {@code create*()} - 기본값으로 객체 생성</li>
 *   <li>커스터마이징 메서드: {@code create*With*()} - 특정 값 지정하여 생성</li>
 * </ul>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * // 기본 ORG_UPLOADER Role
 * Role role = RoleDomainFixture.createOrgUploader();
 *
 * // 커스텀 Role
 * Role role = RoleDomainFixture.createWithCode("custom.role", Set.of("file.upload"));
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-29
 * @see Role
 */
public class RoleDomainFixture {

    // ==================== Object Mother 패턴 ====================

    /**
     * org.uploader Role (file.upload, file.read 권한 포함)
     *
     * @return org.uploader Role
     */
    public static Role createOrgUploader() {
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
    public static Role createOrgViewer() {
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
    public static Role createOrgAdmin() {
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
    public static Role createTenantAdmin() {
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
    public static Role createSystemAdmin() {
        Set<PermissionCode> permissions = new HashSet<>();
        permissions.add(PermissionCode.of("system.admin"));

        return Role.of(
            RoleCode.of("system.admin"),
            "시스템 관리자 역할",
            permissions
        );
    }

    // ==================== 커스터마이징 메서드 ====================

    /**
     * 특정 코드로 Role을 생성합니다 (최소 1개 Permission 자동 포함).
     *
     * @param code Role 코드
     * @return Role
     */
    public static Role createWithCode(String code) {
        Set<PermissionCode> permissions = new HashSet<>();
        permissions.add(PermissionCode.of("test.permission"));

        return Role.of(
            RoleCode.of(code),
            "테스트 역할: " + code,
            permissions
        );
    }

    /**
     * 특정 코드와 Permission 목록으로 Role을 생성합니다.
     *
     * @param code Role 코드
     * @param permissionCodes Permission 코드 문자열 Set
     * @return Role
     */
    public static Role createWithCodeAndPermissions(String code, Set<String> permissionCodes) {
        Set<PermissionCode> permissions = new HashSet<>();
        for (String permCode : permissionCodes) {
            permissions.add(PermissionCode.of(permCode));
        }

        return Role.of(
            RoleCode.of(code),
            "테스트 역할: " + code,
            permissions
        );
    }

    /**
     * 완전히 커스터마이징된 Role을 생성합니다.
     *
     * @param code Role 코드
     * @param description Role 설명
     * @param permissionCodes Permission 코드 Set
     * @return Role
     */
    public static Role create(String code, String description, Set<PermissionCode> permissionCodes) {
        return Role.of(
            RoleCode.of(code),
            description,
            permissionCodes
        );
    }

    /**
     * 여러 개의 Role을 생성합니다 (목록 테스트용).
     *
     * @param count 생성할 개수
     * @return Role 배열
     */
    public static Role[] createMultiple(int count) {
        Role[] roles = new Role[count];
        for (int i = 0; i < count; i++) {
            roles[i] = createWithCode("test.role." + (i + 1));
        }
        return roles;
    }

    // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
    private RoleDomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
