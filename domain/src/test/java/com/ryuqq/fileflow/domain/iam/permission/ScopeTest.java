package com.ryuqq.fileflow.domain.iam.permission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Scope Unit Test
 *
 * <p>Scope Enum의 계층적 권한 범위 검증을 위한 단위 테스트입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@DisplayName("Scope 단위 테스트")
class ScopeTest {

    @Test
    @DisplayName("Scope 생성 시 올바른 값이 설정되어야 한다")
    void createScope_shouldHaveCorrectValues() {
        // Arrange & Act & Assert
        assertEquals("SELF", Scope.SELF.getCode());
        assertEquals("자기 자신", Scope.SELF.getDescription());
        assertEquals(1, Scope.SELF.getLevel());

        assertEquals("ORGANIZATION", Scope.ORGANIZATION.getCode());
        assertEquals("조직", Scope.ORGANIZATION.getDescription());
        assertEquals(2, Scope.ORGANIZATION.getLevel());

        assertEquals("TENANT", Scope.TENANT.getCode());
        assertEquals("테넌트", Scope.TENANT.getDescription());
        assertEquals(3, Scope.TENANT.getLevel());

        assertEquals("GLOBAL", Scope.GLOBAL.getCode());
        assertEquals("전역", Scope.GLOBAL.getDescription());
        assertEquals(4, Scope.GLOBAL.getLevel());
    }

    @ParameterizedTest
    @CsvSource({
        "SELF, SELF, true",
        "ORGANIZATION, SELF, true",
        "ORGANIZATION, ORGANIZATION, true",
        "TENANT, SELF, true",
        "TENANT, ORGANIZATION, true",
        "TENANT, TENANT, true",
        "GLOBAL, SELF, true",
        "GLOBAL, ORGANIZATION, true",
        "GLOBAL, TENANT, true",
        "GLOBAL, GLOBAL, true",
        "SELF, ORGANIZATION, false",
        "SELF, TENANT, false",
        "SELF, GLOBAL, false",
        "ORGANIZATION, TENANT, false",
        "ORGANIZATION, GLOBAL, false",
        "TENANT, GLOBAL, false"
    })
    @DisplayName("includes() 메서드는 계층적 포함 관계를 올바르게 판단해야 한다")
    void includes_shouldCheckHierarchicalInclusion(String parentScope, String childScope, boolean expected) {
        // Arrange
        Scope parent = Scope.valueOf(parentScope);
        Scope child = Scope.valueOf(childScope);

        // Act
        boolean result = parent.includes(child);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("includes() 메서드에 null을 전달하면 예외가 발생해야 한다")
    void includes_withNull_shouldThrowException() {
        // Arrange
        Scope scope = Scope.ORGANIZATION;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> scope.includes(null)
        );
        assertEquals("비교 대상 Scope는 필수입니다", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "SELF, SELF",
        "ORGANIZATION, ORGANIZATION",
        "TENANT, TENANT",
        "GLOBAL, GLOBAL"
    })
    @DisplayName("fromCode() 메서드는 코드로 Scope를 찾아야 한다")
    void fromCode_shouldReturnCorrectScope(String code, String expectedScope) {
        // Act
        Scope result = Scope.fromCode(code);

        // Assert
        assertEquals(Scope.valueOf(expectedScope), result);
    }

    @Test
    @DisplayName("fromCode() 메서드에 존재하지 않는 코드를 전달하면 예외가 발생해야 한다")
    void fromCode_withInvalidCode_shouldThrowException() {
        // Arrange
        String invalidCode = "INVALID_SCOPE";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Scope.fromCode(invalidCode)
        );
        assertTrue(exception.getMessage().contains("알 수 없는 Scope 코드"));
    }

    @Test
    @DisplayName("fromCode() 메서드에 null을 전달하면 예외가 발생해야 한다")
    void fromCode_withNull_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Scope.fromCode(null)
        );
        assertEquals("Scope 코드는 필수입니다", exception.getMessage());
    }

    @Test
    @DisplayName("fromCode() 메서드에 빈 문자열을 전달하면 예외가 발생해야 한다")
    void fromCode_withEmptyString_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Scope.fromCode("")
        );
        assertEquals("Scope 코드는 필수입니다", exception.getMessage());
    }
}
