package com.ryuqq.fileflow.domain.iam.permission.fixture;

import com.ryuqq.fileflow.domain.iam.permission.Grant;
import com.ryuqq.fileflow.domain.iam.permission.Scope;

/**
 * GrantDomain 테스트 Fixture
 *
 * <p>테스트에서 Grant Domain 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 * <p>Object Mother 패턴을 사용하여 자주 사용되는 Grant를 미리 정의합니다.</p>
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
 * // 기본 fileUploadGrant
 * Grant grant = GrantDomainFixture.createFileUploadGrant();
 *
 * // 조건 없는 커스텀 Grant
 * Grant grant = GrantDomainFixture.createWithoutCondition("org.uploader", "file.upload");
 *
 * // 조건 있는 커스텀 Grant
 * Grant grant = GrantDomainFixture.createWithCondition("org.admin", "file.delete", "departmentId == 'IT'");
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-29
 * @see Grant
 */
public class GrantDomainFixture {

    // ==================== Object Mother 패턴 ====================

    /**
     * org.uploader 역할의 file.upload 권한 Grant (조건 없음)
     *
     * @return Grant
     */
    public static Grant createFileUploadGrant() {
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
    public static Grant createFileReadGrant() {
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
    public static Grant createFileDeleteGrant() {
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
    public static Grant createUserWriteGrantWithCondition() {
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
    public static Grant createTenantAdminGrant() {
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
    public static Grant createSystemAdminGrant() {
        return Grant.withoutCondition(
            "system.admin",
            "system.admin",
            Scope.GLOBAL
        );
    }

    // ==================== 커스터마이징 메서드 (조건 없음) ====================

    /**
     * 조건 없는 Grant를 생성합니다 (ORGANIZATION 범위).
     *
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     * @return Grant
     */
    public static Grant createWithoutCondition(String roleCode, String permissionCode) {
        return Grant.withoutCondition(
            roleCode,
            permissionCode,
            Scope.ORGANIZATION
        );
    }

    /**
     * 조건 없는 Grant를 생성합니다 (범위 지정).
     *
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     * @param scope Scope
     * @return Grant
     */
    public static Grant createWithoutCondition(String roleCode, String permissionCode, Scope scope) {
        return Grant.withoutCondition(
            roleCode,
            permissionCode,
            scope
        );
    }

    // ==================== 커스터마이징 메서드 (조건 있음) ====================

    /**
     * 조건 있는 Grant를 생성합니다 (ORGANIZATION 범위).
     *
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     * @param conditionExpr 조건 표현식
     * @return Grant
     */
    public static Grant createWithCondition(String roleCode, String permissionCode, String conditionExpr) {
        return Grant.withCondition(
            roleCode,
            permissionCode,
            Scope.ORGANIZATION,
            conditionExpr
        );
    }

    /**
     * 조건 있는 Grant를 생성합니다 (범위 지정).
     *
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     * @param scope Scope
     * @param conditionExpr 조건 표현식
     * @return Grant
     */
    public static Grant createWithCondition(String roleCode, String permissionCode, Scope scope, String conditionExpr) {
        return Grant.withCondition(
            roleCode,
            permissionCode,
            scope,
            conditionExpr
        );
    }

    /**
     * 여러 개의 Grant를 생성합니다 (목록 테스트용, 조건 없음).
     *
     * @param count 생성할 개수
     * @return Grant 배열
     */
    public static Grant[] createMultiple(int count) {
        Grant[] grants = new Grant[count];
        for (int i = 0; i < count; i++) {
            grants[i] = createWithoutCondition(
                "test.role." + (i + 1),
                "test.permission." + (i + 1)
            );
        }
        return grants;
    }

    // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
    private GrantDomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
