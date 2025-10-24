package com.ryuqq.fileflow.domain.iam.permission;

import com.ryuqq.fileflow.fixtures.iam.permission.RoleFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Role Unit Test
 *
 * <p>Role Aggregate Root의 동작을 검증하기 위한 단위 테스트입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@DisplayName("Role 단위 테스트")
class RoleTest {

    @Test
    @DisplayName("유효한 값으로 Role을 생성할 수 있어야 한다")
    void of_withValidValues_shouldCreateRole() {
        // Arrange
        RoleCode code = RoleCode.of("org.uploader");
        String description = "조직 내 업로더 역할";
        Set<PermissionCode> permissions = new HashSet<>();
        permissions.add(PermissionCode.of("file.upload"));
        permissions.add(PermissionCode.of("file.read"));

        // Act
        Role role = Role.of(code, description, permissions);

        // Assert
        assertNotNull(role);
        assertEquals(code, role.getCode());
        assertEquals(description, role.getDescription());
        assertEquals(2, role.getPermissionCount());
        assertNotNull(role.getCreatedAt());
        assertNotNull(role.getUpdatedAt());
        assertTrue(role.isActive());
    }

    @Test
    @DisplayName("RoleFixture의 orgUploader를 사용하여 Role을 생성할 수 있어야 한다")
    void fixtureOrgUploader_shouldCreateRole() {
        // Act
        Role role = RoleFixture.orgUploader();

        // Assert
        assertNotNull(role);
        assertEquals("org.uploader", role.getCodeValue());
        assertEquals("조직 내 업로더 역할", role.getDescription());
        assertEquals(2, role.getPermissionCount());
        assertTrue(role.hasPermission(PermissionCode.of("file.upload")));
        assertTrue(role.hasPermission(PermissionCode.of("file.read")));
    }

    @Test
    @DisplayName("RoleFixture Builder를 사용하여 커스텀 Role을 생성할 수 있어야 한다")
    void fixtureBuilder_shouldCreateCustomRole() {
        // Arrange & Act
        Role role = RoleFixture.builder()
            .code("custom.role")
            .description("커스텀 역할")
            .addPermission("custom.permission")
            .build();

        // Assert
        assertNotNull(role);
        assertEquals("custom.role", role.getCodeValue());
        assertEquals("커스텀 역할", role.getDescription());
        assertTrue(role.hasPermission(PermissionCode.of("custom.permission")));
    }

    @Test
    @DisplayName("null Role 코드로는 생성할 수 없어야 한다")
    void of_withNullCode_shouldThrowException() {
        // Arrange
        String description = "테스트 역할";
        Set<PermissionCode> permissions = new HashSet<>();
        permissions.add(PermissionCode.of("test.permission"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Role.of(null, description, permissions)
        );
        assertEquals("Role 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("null 설명으로는 생성할 수 없어야 한다")
    void of_withNullDescription_shouldThrowException() {
        // Arrange
        RoleCode code = RoleCode.of("org.uploader");
        Set<PermissionCode> permissions = new HashSet<>();
        permissions.add(PermissionCode.of("test.permission"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Role.of(code, null, permissions)
        );
        assertEquals("Role 설명은 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("빈 Permission Set으로는 생성할 수 없어야 한다")
    void of_withEmptyPermissions_shouldThrowException() {
        // Arrange
        RoleCode code = RoleCode.of("org.uploader");
        String description = "조직 내 업로더 역할";
        Set<PermissionCode> permissions = new HashSet<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Role.of(code, description, permissions)
        );
        assertEquals("Permission 코드는 최소 1개 이상 필요합니다", exception.getMessage());
    }

    @Test
    @DisplayName("Role 설명을 변경할 수 있어야 한다")
    void updateDescription_shouldChangeDescription() {
        // Arrange
        Role role = RoleFixture.orgUploader();
        String newDescription = "새로운 업로더 역할";

        // Act
        role.updateDescription(newDescription);

        // Assert
        assertEquals(newDescription, role.getDescription());
    }

    @Test
    @DisplayName("null 설명으로 변경하려고 하면 예외가 발생해야 한다")
    void updateDescription_withNull_shouldThrowException() {
        // Arrange
        Role role = RoleFixture.orgUploader();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> role.updateDescription(null)
        );
        assertEquals("새로운 Role 설명은 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("삭제된 Role의 설명을 변경하려고 하면 예외가 발생해야 한다")
    void updateDescription_onDeletedRole_shouldThrowException() {
        // Arrange
        Role role = RoleFixture.orgUploader();
        role.softDelete();

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> role.updateDescription("새로운 설명")
        );
        assertEquals("삭제된 Role의 설명은 변경할 수 없습니다", exception.getMessage());
    }

    @Test
    @DisplayName("Permission을 추가할 수 있어야 한다")
    void addPermission_shouldAddPermission() {
        // Arrange
        Role role = RoleFixture.orgUploader();
        PermissionCode newPermission = PermissionCode.of("file.delete");

        // Act
        role.addPermission(newPermission);

        // Assert
        assertTrue(role.hasPermission(newPermission));
        assertEquals(3, role.getPermissionCount());
    }

    @Test
    @DisplayName("이미 존재하는 Permission을 추가해도 중복되지 않아야 한다")
    void addPermission_alreadyExists_shouldNotDuplicate() {
        // Arrange
        Role role = RoleFixture.orgUploader();
        PermissionCode existingPermission = PermissionCode.of("file.upload");

        // Act
        role.addPermission(existingPermission);

        // Assert
        assertEquals(2, role.getPermissionCount());
    }

    @Test
    @DisplayName("null Permission을 추가하려고 하면 예외가 발생해야 한다")
    void addPermission_withNull_shouldThrowException() {
        // Arrange
        Role role = RoleFixture.orgUploader();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> role.addPermission(null)
        );
        assertEquals("추가할 Permission 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("삭제된 Role에 Permission을 추가하려고 하면 예외가 발생해야 한다")
    void addPermission_onDeletedRole_shouldThrowException() {
        // Arrange
        Role role = RoleFixture.orgUploader();
        role.softDelete();

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> role.addPermission(PermissionCode.of("file.delete"))
        );
        assertEquals("삭제된 Role에 Permission을 추가할 수 없습니다", exception.getMessage());
    }

    @Test
    @DisplayName("Permission을 제거할 수 있어야 한다")
    void removePermission_shouldRemovePermission() {
        // Arrange
        Role role = RoleFixture.orgUploader();
        PermissionCode toRemove = PermissionCode.of("file.read");

        // Act
        role.removePermission(toRemove);

        // Assert
        assertFalse(role.hasPermission(toRemove));
        assertEquals(1, role.getPermissionCount());
    }

    @Test
    @DisplayName("마지막 Permission을 제거하려고 하면 예외가 발생해야 한다")
    void removePermission_lastPermission_shouldThrowException() {
        // Arrange
        Set<PermissionCode> singlePermission = new HashSet<>();
        singlePermission.add(PermissionCode.of("only.permission"));

        Role role = RoleFixture.builder()
            .permissions(singlePermission)
            .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> role.removePermission(PermissionCode.of("only.permission"))
        );
        assertEquals("Role은 최소 1개 이상의 Permission을 포함해야 합니다", exception.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 Permission을 제거해도 예외가 발생하지 않아야 한다")
    void removePermission_nonExistent_shouldNotThrowException() {
        // Arrange
        Role role = RoleFixture.orgUploader();
        PermissionCode nonExistent = PermissionCode.of("non.existent");

        // Act & Assert
        assertDoesNotThrow(() -> role.removePermission(nonExistent));
        assertEquals(2, role.getPermissionCount());
    }

    @Test
    @DisplayName("Role을 소프트 삭제할 수 있어야 한다")
    void softDelete_shouldMarkAsDeleted() {
        // Arrange
        Role role = RoleFixture.orgUploader();
        assertTrue(role.isActive());

        // Act
        role.softDelete();

        // Assert
        assertFalse(role.isActive());
        assertTrue(role.isDeleted());
    }

    @Test
    @DisplayName("이미 삭제된 Role을 다시 삭제하려고 하면 예외가 발생해야 한다")
    void softDelete_alreadyDeleted_shouldThrowException() {
        // Arrange
        Role role = RoleFixture.orgUploader();
        role.softDelete();

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> role.softDelete()
        );
        assertEquals("이미 삭제된 Role입니다", exception.getMessage());
    }

    @Test
    @DisplayName("hasPermission 메서드는 Permission 포함 여부를 올바르게 판단해야 한다")
    void hasPermission_shouldCheckPermissionCorrectly() {
        // Arrange
        Role role = RoleFixture.orgUploader();

        // Act & Assert
        assertTrue(role.hasPermission(PermissionCode.of("file.upload")));
        assertTrue(role.hasPermission(PermissionCode.of("file.read")));
        assertFalse(role.hasPermission(PermissionCode.of("file.delete")));
    }

    @Test
    @DisplayName("getCodeValue 메서드는 Role 코드 문자열을 반환해야 한다")
    void getCodeValue_shouldReturnCodeString() {
        // Arrange
        Role role = RoleFixture.orgUploader();

        // Act
        String codeValue = role.getCodeValue();

        // Assert
        assertEquals("org.uploader", codeValue);
    }

    @Test
    @DisplayName("getPermissionCodes 메서드는 불변 Set을 반환해야 한다")
    void getPermissionCodes_shouldReturnUnmodifiableSet() {
        // Arrange
        Role role = RoleFixture.orgUploader();
        Set<PermissionCode> permissions = role.getPermissionCodes();

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            permissions.add(PermissionCode.of("new.permission"));
        });
    }

    @Test
    @DisplayName("reconstitute 메서드로 DB에서 조회한 데이터를 복원할 수 있어야 한다")
    void reconstitute_shouldRestoreRoleFromDatabase() {
        // Arrange
        RoleCode code = RoleCode.of("org.uploader");
        String description = "조직 내 업로더 역할";
        Set<PermissionCode> permissions = new HashSet<>();
        permissions.add(PermissionCode.of("file.upload"));
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.systemDefault());
        Role original = new Role(code, description, permissions, fixedClock);

        // Act
        Role reconstituted = Role.reconstitute(
            code,
            description,
            permissions,
            original.getCreatedAt(),
            original.getUpdatedAt(),
            false
        );

        // Assert
        assertEquals(original.getCodeValue(), reconstituted.getCodeValue());
        assertEquals(original.getDescription(), reconstituted.getDescription());
        assertEquals(original.getPermissionCount(), reconstituted.getPermissionCount());
        assertEquals(original.getCreatedAt(), reconstituted.getCreatedAt());
        assertEquals(original.getUpdatedAt(), reconstituted.getUpdatedAt());
        assertEquals(original.isDeleted(), reconstituted.isDeleted());
    }
}
