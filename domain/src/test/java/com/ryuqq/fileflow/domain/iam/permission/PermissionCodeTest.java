package com.ryuqq.fileflow.domain.iam.permission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PermissionCode Unit Test
 *
 * <p>PermissionCode Value Object의 유효성 검증을 위한 단위 테스트입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@DisplayName("PermissionCode 단위 테스트")
class PermissionCodeTest {

    @Test
    @DisplayName("유효한 Permission 코드로 PermissionCode를 생성할 수 있어야 한다")
    void of_withValidCode_shouldCreatePermissionCode() {
        // Arrange
        String validCode = "file.upload";

        // Act
        PermissionCode permissionCode = PermissionCode.of(validCode);

        // Assert
        assertNotNull(permissionCode);
        assertEquals(validCode, permissionCode.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "a.b", "file.upload", "user.read", "tenant_admin", "system-viewer"})
    @DisplayName("3자 이상 100자 이하의 유효한 문자만 포함하는 코드는 생성 가능해야 한다")
    void of_withValidFormats_shouldCreatePermissionCode(String code) {
        // Act
        PermissionCode permissionCode = PermissionCode.of(code);

        // Assert
        assertNotNull(permissionCode);
        assertEquals(code, permissionCode.getValue());
    }

    @Test
    @DisplayName("null 코드는 예외를 발생시켜야 한다")
    void of_withNull_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PermissionCode.of(null)
        );
        assertEquals("Permission 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("빈 문자열 코드는 예외를 발생시켜야 한다")
    void of_withEmptyString_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PermissionCode.of("")
        );
        assertEquals("Permission 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("공백만 있는 코드는 예외를 발생시켜야 한다")
    void of_withBlankString_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PermissionCode.of("   ")
        );
        assertEquals("Permission 코드는 필수입니다", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "a"})
    @DisplayName("3자 미만의 코드는 예외를 발생시켜야 한다")
    void of_withTooShortCode_shouldThrowException(String code) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PermissionCode.of(code)
        );
        assertTrue(exception.getMessage().contains("3자 이상"));
    }

    @Test
    @DisplayName("100자를 초과하는 코드는 예외를 발생시켜야 한다")
    void of_withTooLongCode_shouldThrowException() {
        // Arrange
        String tooLongCode = "a".repeat(101);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PermissionCode.of(tooLongCode)
        );
        assertTrue(exception.getMessage().contains("초과할 수 없습니다"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"file upload", "user@read", "tenant#admin", "system$viewer"})
    @DisplayName("유효하지 않은 문자가 포함된 코드는 예외를 발생시켜야 한다")
    void of_withInvalidCharacters_shouldThrowException(String code) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PermissionCode.of(code)
        );
        assertTrue(exception.getMessage().contains("만 사용할 수 있습니다"));
    }

    @Test
    @DisplayName("같은 값의 PermissionCode는 동등해야 한다")
    void equals_withSameValue_shouldBeEqual() {
        // Arrange
        PermissionCode code1 = PermissionCode.of("file.upload");
        PermissionCode code2 = PermissionCode.of("file.upload");

        // Act & Assert
        assertEquals(code1, code2);
        assertEquals(code1.hashCode(), code2.hashCode());
    }

    @Test
    @DisplayName("다른 값의 PermissionCode는 동등하지 않아야 한다")
    void equals_withDifferentValue_shouldNotBeEqual() {
        // Arrange
        PermissionCode code1 = PermissionCode.of("file.upload");
        PermissionCode code2 = PermissionCode.of("file.read");

        // Act & Assert
        assertNotEquals(code1, code2);
    }

    @Test
    @DisplayName("toString() 메서드는 코드 값을 반환해야 한다")
    void toString_shouldReturnCodeValue() {
        // Arrange
        String codeValue = "file.upload";
        PermissionCode permissionCode = PermissionCode.of(codeValue);

        // Act
        String result = permissionCode.toString();

        // Assert
        assertTrue(result.contains(codeValue));
    }
}
