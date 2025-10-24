package com.ryuqq.fileflow.domain.iam.permission;

import com.ryuqq.fileflow.fixtures.iam.permission.GrantFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Grant Unit Test
 *
 * <p>Grant Record의 동작을 검증하기 위한 단위 테스트입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@DisplayName("Grant 단위 테스트")
class GrantTest {

    @Test
    @DisplayName("조건 없는 Grant를 생성할 수 있어야 한다")
    void withoutCondition_shouldCreateGrant() {
        // Arrange
        String roleCode = "org.uploader";
        String permissionCode = "file.upload";
        Scope scope = Scope.ORGANIZATION;

        // Act
        Grant grant = Grant.withoutCondition(roleCode, permissionCode, scope);

        // Assert
        assertNotNull(grant);
        assertEquals(roleCode, grant.roleCode());
        assertEquals(permissionCode, grant.permissionCode());
        assertEquals(scope, grant.scope());
        assertNull(grant.conditionExpr());
        assertFalse(grant.hasCondition());
    }

    @Test
    @DisplayName("조건 있는 Grant를 생성할 수 있어야 한다")
    void withCondition_shouldCreateGrant() {
        // Arrange
        String roleCode = "org.admin";
        String permissionCode = "file.delete";
        Scope scope = Scope.ORGANIZATION;
        String condition = "departmentId == 'IT'";

        // Act
        Grant grant = Grant.withCondition(roleCode, permissionCode, scope, condition);

        // Assert
        assertNotNull(grant);
        assertEquals(roleCode, grant.roleCode());
        assertEquals(permissionCode, grant.permissionCode());
        assertEquals(scope, grant.scope());
        assertEquals(condition, grant.conditionExpr());
        assertTrue(grant.hasCondition());
    }

    @Test
    @DisplayName("GrantFixture의 fileUploadGrant를 사용하여 Grant를 생성할 수 있어야 한다")
    void fixtureFileUploadGrant_shouldCreateGrant() {
        // Act
        Grant grant = GrantFixture.fileUploadGrant();

        // Assert
        assertNotNull(grant);
        assertEquals("org.uploader", grant.roleCode());
        assertEquals("file.upload", grant.permissionCode());
        assertEquals(Scope.ORGANIZATION, grant.scope());
        assertFalse(grant.hasCondition());
    }

    @Test
    @DisplayName("GrantFixture Builder를 사용하여 커스텀 Grant를 생성할 수 있어야 한다")
    void fixtureBuilder_shouldCreateCustomGrant() {
        // Arrange & Act
        Grant grant = GrantFixture.builder()
            .roleCode("custom.role")
            .permissionCode("custom.permission")
            .scope(Scope.TENANT)
            .condition("status == 'active'")
            .build();

        // Assert
        assertNotNull(grant);
        assertEquals("custom.role", grant.roleCode());
        assertEquals("custom.permission", grant.permissionCode());
        assertEquals(Scope.TENANT, grant.scope());
        assertTrue(grant.hasCondition());
    }

    @Test
    @DisplayName("null Role 코드로는 생성할 수 없어야 한다")
    void constructor_withNullRoleCode_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Grant(null, "file.upload", Scope.ORGANIZATION, null)
        );
        assertEquals("Role 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("빈 Role 코드로는 생성할 수 없어야 한다")
    void constructor_withEmptyRoleCode_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Grant("", "file.upload", Scope.ORGANIZATION, null)
        );
        assertEquals("Role 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("null Permission 코드로는 생성할 수 없어야 한다")
    void constructor_withNullPermissionCode_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Grant("org.uploader", null, Scope.ORGANIZATION, null)
        );
        assertEquals("Permission 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("null Scope로는 생성할 수 없어야 한다")
    void constructor_withNullScope_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Grant("org.uploader", "file.upload", null, null)
        );
        assertEquals("권한 적용 범위는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("withCondition 메서드에 null 조건을 전달하면 예외가 발생해야 한다")
    void withCondition_withNullCondition_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Grant.withCondition("org.admin", "file.delete", Scope.ORGANIZATION, null)
        );
        assertEquals("조건 표현식은 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("withCondition 메서드에 빈 조건을 전달하면 예외가 발생해야 한다")
    void withCondition_withEmptyCondition_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Grant.withCondition("org.admin", "file.delete", Scope.ORGANIZATION, "")
        );
        assertEquals("조건 표현식은 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("공백이 포함된 값들을 trim해야 한다")
    void constructor_shouldTrimWhitespace() {
        // Arrange & Act
        Grant grant = new Grant("  org.uploader  ", "  file.upload  ", Scope.ORGANIZATION, "  condition  ");

        // Assert
        assertEquals("org.uploader", grant.roleCode());
        assertEquals("file.upload", grant.permissionCode());
        assertEquals("condition", grant.conditionExpr());
    }

    @Test
    @DisplayName("빈 문자열 조건은 null로 정규화되어야 한다")
    void constructor_shouldNormalizeEmptyConditionToNull() {
        // Arrange & Act
        Grant grant = new Grant("org.uploader", "file.upload", Scope.ORGANIZATION, "   ");

        // Assert
        assertNull(grant.conditionExpr());
        assertFalse(grant.hasCondition());
    }

    @Test
    @DisplayName("isApplicableToScope 메서드는 Scope 포함 관계를 올바르게 판단해야 한다")
    void isApplicableToScope_shouldCheckScopeInclusion() {
        // Arrange
        Grant grant = GrantFixture.builder()
            .scope(Scope.TENANT)
            .build();

        // Act & Assert
        assertTrue(grant.isApplicableToScope(Scope.SELF));
        assertTrue(grant.isApplicableToScope(Scope.ORGANIZATION));
        assertTrue(grant.isApplicableToScope(Scope.TENANT));
        assertFalse(grant.isApplicableToScope(Scope.GLOBAL));
    }

    @Test
    @DisplayName("isApplicableToScope 메서드에 null을 전달하면 예외가 발생해야 한다")
    void isApplicableToScope_withNull_shouldThrowException() {
        // Arrange
        Grant grant = GrantFixture.fileUploadGrant();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> grant.isApplicableToScope(null)
        );
        assertEquals("확인할 Scope는 필수입니다", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "file.upload, file.upload, true",
        "file.upload, FILE.UPLOAD, true",
        "file.upload, file.read, false"
    })
    @DisplayName("isForPermission 메서드는 Permission 코드 일치 여부를 판단해야 한다")
    void isForPermission_shouldCheckPermissionMatch(String grantPermission, String targetPermission, boolean expected) {
        // Arrange
        Grant grant = GrantFixture.builder()
            .permissionCode(grantPermission)
            .build();

        // Act
        boolean result = grant.isForPermission(targetPermission);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("isForPermission 메서드에 null을 전달하면 예외가 발생해야 한다")
    void isForPermission_withNull_shouldThrowException() {
        // Arrange
        Grant grant = GrantFixture.fileUploadGrant();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> grant.isForPermission(null)
        );
        assertEquals("확인할 Permission 코드는 필수입니다", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "org.uploader, org.uploader, true",
        "org.uploader, ORG.UPLOADER, true",
        "org.uploader, org.admin, false"
    })
    @DisplayName("isFromRole 메서드는 Role 코드 일치 여부를 판단해야 한다")
    void isFromRole_shouldCheckRoleMatch(String grantRole, String targetRole, boolean expected) {
        // Arrange
        Grant grant = GrantFixture.builder()
            .roleCode(grantRole)
            .build();

        // Act
        boolean result = grant.isFromRole(targetRole);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("isFromRole 메서드에 null을 전달하면 예외가 발생해야 한다")
    void isFromRole_withNull_shouldThrowException() {
        // Arrange
        Grant grant = GrantFixture.fileUploadGrant();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> grant.isFromRole(null)
        );
        assertEquals("확인할 Role 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("toString 메서드는 조건 없는 Grant의 정보를 출력해야 한다")
    void toString_withoutCondition_shouldFormatCorrectly() {
        // Arrange
        Grant grant = GrantFixture.fileUploadGrant();

        // Act
        String result = grant.toString();

        // Assert
        assertTrue(result.contains("org.uploader"));
        assertTrue(result.contains("file.upload"));
        assertTrue(result.contains("ORGANIZATION"));
        assertTrue(result.contains("unconditional"));
    }

    @Test
    @DisplayName("toString 메서드는 조건 있는 Grant의 정보를 출력해야 한다")
    void toString_withCondition_shouldFormatCorrectly() {
        // Arrange
        Grant grant = GrantFixture.userWriteGrantWithCondition();

        // Act
        String result = grant.toString();

        // Assert
        assertTrue(result.contains("org.admin"));
        assertTrue(result.contains("user.write"));
        assertTrue(result.contains("ORGANIZATION"));
        assertTrue(result.contains("departmentId == 'HR'"));
    }

    @Test
    @DisplayName("같은 값의 Grant는 동등해야 한다")
    void equals_withSameValues_shouldBeEqual() {
        // Arrange
        Grant grant1 = new Grant("org.uploader", "file.upload", Scope.ORGANIZATION, null);
        Grant grant2 = new Grant("org.uploader", "file.upload", Scope.ORGANIZATION, null);

        // Act & Assert
        assertEquals(grant1, grant2);
        assertEquals(grant1.hashCode(), grant2.hashCode());
    }

    @Test
    @DisplayName("다른 값의 Grant는 동등하지 않아야 한다")
    void equals_withDifferentValues_shouldNotBeEqual() {
        // Arrange
        Grant grant1 = new Grant("org.uploader", "file.upload", Scope.ORGANIZATION, null);
        Grant grant2 = new Grant("org.uploader", "file.read", Scope.ORGANIZATION, null);

        // Act & Assert
        assertNotEquals(grant1, grant2);
    }
}
