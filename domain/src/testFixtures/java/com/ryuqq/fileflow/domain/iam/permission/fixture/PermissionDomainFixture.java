package com.ryuqq.fileflow.domain.iam.permission.fixture;

import com.ryuqq.fileflow.domain.iam.permission.Permission;
import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;
import com.ryuqq.fileflow.domain.iam.permission.Scope;

/**
 * PermissionDomain 테스트 Fixture
 *
 * <p>테스트에서 Permission Domain 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 * <p>Object Mother 패턴을 사용하여 자주 사용되는 Permission을 미리 정의합니다.</p>
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
 * // 기본 file.upload Permission
 * Permission permission = PermissionDomainFixture.createFileUpload();
 *
 * // 커스텀 Permission
 * Permission permission = PermissionDomainFixture.createWithCode("custom.permission");
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-29
 * @see Permission
 */
public class PermissionDomainFixture {

    // ==================== Object Mother 패턴 ====================

    /**
     * file.upload Permission (ORGANIZATION 범위)
     *
     * @return file.upload Permission
     */
    public static Permission createFileUpload() {
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
    public static Permission createFileRead() {
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
    public static Permission createFileDelete() {
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
    public static Permission createUserRead() {
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
    public static Permission createUserWrite() {
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
    public static Permission createTenantAdmin() {
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
    public static Permission createSystemAdmin() {
        return Permission.of(
            PermissionCode.of("system.admin"),
            "시스템 관리자 권한",
            Scope.GLOBAL
        );
    }

    // ==================== 커스터마이징 메서드 ====================

    /**
     * 특정 코드로 Permission을 생성합니다 (ORGANIZATION 범위).
     *
     * @param code Permission 코드
     * @return Permission
     */
    public static Permission createWithCode(String code) {
        return Permission.of(
            PermissionCode.of(code),
            "테스트 권한: " + code,
            Scope.ORGANIZATION
        );
    }

    /**
     * 특정 코드와 범위로 Permission을 생성합니다.
     *
     * @param code Permission 코드
     * @param scope Permission 범위
     * @return Permission
     */
    public static Permission createWithCodeAndScope(String code, Scope scope) {
        return Permission.of(
            PermissionCode.of(code),
            "테스트 권한: " + code,
            scope
        );
    }

    /**
     * 완전히 커스터마이징된 Permission을 생성합니다.
     *
     * @param code Permission 코드
     * @param description Permission 설명
     * @param scope Permission 범위
     * @return Permission
     */
    public static Permission create(String code, String description, Scope scope) {
        return Permission.of(
            PermissionCode.of(code),
            description,
            scope
        );
    }

    /**
     * 여러 개의 Permission을 생성합니다 (목록 테스트용).
     *
     * @param count 생성할 개수
     * @return Permission 배열
     */
    public static Permission[] createMultiple(int count) {
        Permission[] permissions = new Permission[count];
        for (int i = 0; i < count; i++) {
            permissions[i] = createWithCode("test.permission." + (i + 1));
        }
        return permissions;
    }

    // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
    private PermissionDomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
