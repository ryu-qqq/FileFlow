package com.ryuqq.fileflow.domain.iam.permission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RoleCode Unit Test
 *
 * <p>RoleCode Value Object의 유효성 검증을 위한 단위 테스트입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@DisplayName("RoleCode 단위 테스트")
class RoleCodeTest {

    @Test
    @DisplayName("유효한 Role 코드로 RoleCode를 생성할 수 있어야 한다")
    void of_withValidCode_shouldCreateRoleCode() {
        // Arrange
        String validCode = "org.uploader";

        // Act
        RoleCode roleCode = RoleCode.of(validCode);

        // Assert
        assertNotNull(roleCode);
        assertEquals(validCode, roleCode.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "o.a", "org.uploader", "tenant.admin", "system_admin", "global-viewer"})
    @DisplayName("3자 이상 100자 이하의 유효한 문자만 포함하는 코드는 생성 가능해야 한다")
    void of_withValidFormats_shouldCreateRoleCode(String code) {
        // Act
        RoleCode roleCode = RoleCode.of(code);

        // Assert
        assertNotNull(roleCode);
        assertEquals(code, roleCode.getValue());
    }

    @Test
    @DisplayName("null 코드는 예외를 발생시켜야 한다")
    void of_withNull_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> RoleCode.of(null)
        );
        assertEquals("Role 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("빈 문자열 코드는 예외를 발생시켜야 한다")
    void of_withEmptyString_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> RoleCode.of("")
        );
        assertEquals("Role 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("공백만 있는 코드는 예외를 발생시켜야 한다")
    void of_withBlankString_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> RoleCode.of("   ")
        );
        assertEquals("Role 코드는 필수입니다", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "a"})
    @DisplayName("3자 미만의 코드는 예외를 발생시켜야 한다")
    void of_withTooShortCode_shouldThrowException(String code) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> RoleCode.of(code)
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
            () -> RoleCode.of(tooLongCode)
        );
        assertTrue(exception.getMessage().contains("초과할 수 없습니다"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"org uploader", "tenant@admin", "system#admin", "global$viewer"})
    @DisplayName("유효하지 않은 문자가 포함된 코드는 예외를 발생시켜야 한다")
    void of_withInvalidCharacters_shouldThrowException(String code) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> RoleCode.of(code)
        );
        assertTrue(exception.getMessage().contains("만 사용할 수 있습니다"));
    }

    @Test
    @DisplayName("같은 값의 RoleCode는 동등해야 한다")
    void equals_withSameValue_shouldBeEqual() {
        // Arrange
        RoleCode code1 = RoleCode.of("org.uploader");
        RoleCode code2 = RoleCode.of("org.uploader");

        // Act & Assert
        assertEquals(code1, code2);
        assertEquals(code1.hashCode(), code2.hashCode());
    }

    @Test
    @DisplayName("다른 값의 RoleCode는 동등하지 않아야 한다")
    void equals_withDifferentValue_shouldNotBeEqual() {
        // Arrange
        RoleCode code1 = RoleCode.of("org.uploader");
        RoleCode code2 = RoleCode.of("org.viewer");

        // Act & Assert
        assertNotEquals(code1, code2);
    }

    @Test
    @DisplayName("toString() 메서드는 코드 값을 반환해야 한다")
    void toString_shouldReturnCodeValue() {
        // Arrange
        String codeValue = "org.uploader";
        RoleCode roleCode = RoleCode.of(codeValue);

        // Act
        String result = roleCode.toString();

        // Assert
        assertTrue(result.contains(codeValue));
    }
}
