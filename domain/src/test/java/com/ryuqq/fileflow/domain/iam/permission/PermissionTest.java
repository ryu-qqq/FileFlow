package com.ryuqq.fileflow.domain.iam.permission;

import com.ryuqq.fileflow.fixtures.iam.permission.PermissionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Permission Unit Test
 *
 * <p>Permission Aggregate Root의 동작을 검증하기 위한 단위 테스트입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@DisplayName("Permission 단위 테스트")
class PermissionTest {

    @Test
    @DisplayName("유효한 값으로 Permission을 생성할 수 있어야 한다")
    void of_withValidValues_shouldCreatePermission() {
        // Arrange
        PermissionCode code = PermissionCode.of("file.upload");
        String description = "파일 업로드 권한";
        Scope scope = Scope.ORGANIZATION;

        // Act
        Permission permission = Permission.of(code, description, scope);

        // Assert
        assertNotNull(permission);
        assertEquals(code, permission.getCode());
        assertEquals(description, permission.getDescription());
        assertEquals(scope, permission.getDefaultScope());
        assertNotNull(permission.getCreatedAt());
        assertNotNull(permission.getUpdatedAt());
        assertTrue(permission.isActive());
    }

    @Test
    @DisplayName("PermissionFixture의 fileUpload를 사용하여 Permission을 생성할 수 있어야 한다")
    void fixtureFileUpload_shouldCreatePermission() {
        // Act
        Permission permission = PermissionFixture.fileUpload();

        // Assert
        assertNotNull(permission);
        assertEquals("file.upload", permission.getCodeValue());
        assertEquals("파일 업로드 권한", permission.getDescription());
        assertEquals(Scope.ORGANIZATION, permission.getDefaultScope());
    }

    @Test
    @DisplayName("PermissionFixture Builder를 사용하여 커스텀 Permission을 생성할 수 있어야 한다")
    void fixtureBuilder_shouldCreateCustomPermission() {
        // Arrange & Act
        Permission permission = PermissionFixture.builder()
            .code("custom.permission")
            .description("커스텀 권한")
            .scope(Scope.TENANT)
            .build();

        // Assert
        assertNotNull(permission);
        assertEquals("custom.permission", permission.getCodeValue());
        assertEquals("커스텀 권한", permission.getDescription());
        assertEquals(Scope.TENANT, permission.getDefaultScope());
    }

    @Test
    @DisplayName("null Permission 코드로는 생성할 수 없어야 한다")
    void of_withNullCode_shouldThrowException() {
        // Arrange
        String description = "테스트 권한";
        Scope scope = Scope.ORGANIZATION;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Permission.of(null, description, scope)
        );
        assertEquals("Permission 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("null 설명으로는 생성할 수 없어야 한다")
    void of_withNullDescription_shouldThrowException() {
        // Arrange
        PermissionCode code = PermissionCode.of("file.upload");
        Scope scope = Scope.ORGANIZATION;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Permission.of(code, null, scope)
        );
        assertEquals("Permission 설명은 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("빈 문자열 설명으로는 생성할 수 없어야 한다")
    void of_withEmptyDescription_shouldThrowException() {
        // Arrange
        PermissionCode code = PermissionCode.of("file.upload");
        Scope scope = Scope.ORGANIZATION;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Permission.of(code, "", scope)
        );
        assertEquals("Permission 설명은 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("null Scope로는 생성할 수 없어야 한다")
    void of_withNullScope_shouldThrowException() {
        // Arrange
        PermissionCode code = PermissionCode.of("file.upload");
        String description = "파일 업로드 권한";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Permission.of(code, description, null)
        );
        assertEquals("기본 범위는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("Permission 설명을 변경할 수 있어야 한다")
    void updateDescription_shouldChangeDescription() {
        // Arrange
        Permission permission = PermissionFixture.fileUpload();
        String newDescription = "새로운 파일 업로드 권한";

        // Act
        permission.updateDescription(newDescription);

        // Assert
        assertEquals(newDescription, permission.getDescription());
    }

    @Test
    @DisplayName("null 설명으로 변경하려고 하면 예외가 발생해야 한다")
    void updateDescription_withNull_shouldThrowException() {
        // Arrange
        Permission permission = PermissionFixture.fileUpload();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> permission.updateDescription(null)
        );
        assertEquals("새로운 Permission 설명은 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("삭제된 Permission의 설명을 변경하려고 하면 예외가 발생해야 한다")
    void updateDescription_onDeletedPermission_shouldThrowException() {
        // Arrange
        Permission permission = PermissionFixture.fileUpload();
        permission.softDelete();

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> permission.updateDescription("새로운 설명")
        );
        assertEquals("삭제된 Permission의 설명은 변경할 수 없습니다", exception.getMessage());
    }

    @Test
    @DisplayName("Permission의 기본 Scope를 변경할 수 있어야 한다")
    void updateDefaultScope_shouldChangeScope() {
        // Arrange
        Permission permission = PermissionFixture.fileUpload();
        Scope newScope = Scope.TENANT;

        // Act
        permission.updateDefaultScope(newScope);

        // Assert
        assertEquals(newScope, permission.getDefaultScope());
    }

    @Test
    @DisplayName("null Scope로 변경하려고 하면 예외가 발생해야 한다")
    void updateDefaultScope_withNull_shouldThrowException() {
        // Arrange
        Permission permission = PermissionFixture.fileUpload();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> permission.updateDefaultScope(null)
        );
        assertEquals("새로운 기본 범위는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("Permission을 소프트 삭제할 수 있어야 한다")
    void softDelete_shouldMarkAsDeleted() {
        // Arrange
        Permission permission = PermissionFixture.fileUpload();
        assertTrue(permission.isActive());

        // Act
        permission.softDelete();

        // Assert
        assertFalse(permission.isActive());
        assertTrue(permission.isDeleted());
    }

    @Test
    @DisplayName("이미 삭제된 Permission을 다시 삭제하려고 하면 예외가 발생해야 한다")
    void softDelete_alreadyDeleted_shouldThrowException() {
        // Arrange
        Permission permission = PermissionFixture.fileUpload();
        permission.softDelete();

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> permission.softDelete()
        );
        assertEquals("이미 삭제된 Permission입니다", exception.getMessage());
    }

    @Test
    @DisplayName("isApplicableToScope 메서드는 Scope 포함 관계를 올바르게 판단해야 한다")
    void isApplicableToScope_shouldCheckScopeInclusion() {
        // Arrange
        Permission permission = PermissionFixture.builder()
            .scope(Scope.TENANT)
            .build();

        // Act & Assert
        assertTrue(permission.isApplicableToScope(Scope.SELF));
        assertTrue(permission.isApplicableToScope(Scope.ORGANIZATION));
        assertTrue(permission.isApplicableToScope(Scope.TENANT));
        assertFalse(permission.isApplicableToScope(Scope.GLOBAL));
    }

    @Test
    @DisplayName("getCodeValue 메서드는 Permission 코드 문자열을 반환해야 한다")
    void getCodeValue_shouldReturnCodeString() {
        // Arrange
        Permission permission = PermissionFixture.fileUpload();

        // Act
        String codeValue = permission.getCodeValue();

        // Assert
        assertEquals("file.upload", codeValue);
    }

    @Test
    @DisplayName("reconstitute 메서드로 DB에서 조회한 데이터를 복원할 수 있어야 한다")
    void reconstitute_shouldRestorePermissionFromDatabase() {
        // Arrange
        PermissionCode code = PermissionCode.of("file.upload");
        String description = "파일 업로드 권한";
        Scope scope = Scope.ORGANIZATION;
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.systemDefault());
        Permission original = new Permission(code, description, scope, fixedClock);

        // Act
        Permission reconstituted = Permission.reconstitute(
            code,
            description,
            scope,
            original.getCreatedAt(),
            original.getUpdatedAt(),
            false
        );

        // Assert
        assertEquals(original.getCodeValue(), reconstituted.getCodeValue());
        assertEquals(original.getDescription(), reconstituted.getDescription());
        assertEquals(original.getDefaultScope(), reconstituted.getDefaultScope());
        assertEquals(original.getCreatedAt(), reconstituted.getCreatedAt());
        assertEquals(original.getUpdatedAt(), reconstituted.getUpdatedAt());
        assertEquals(original.isDeleted(), reconstituted.isDeleted());
    }
}
