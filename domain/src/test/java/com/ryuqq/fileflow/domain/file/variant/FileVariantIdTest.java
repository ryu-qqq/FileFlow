package com.ryuqq.fileflow.domain.file.variant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileVariantId 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>Compact Constructor 검증</li>
 *   <li>of() 정적 팩토리 메서드</li>
 *   <li>value() 접근자</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DisplayName("FileVariantId 단위 테스트")
class FileVariantIdTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("constructor_WithPositiveValue_ShouldCreateFileVariantId - 양수 값으로 생성")
        void constructor_WithPositiveValue_ShouldCreateFileVariantId() {
            // When
            FileVariantId id = new FileVariantId(1L);

            // Then
            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("constructor_WithNullValue_ShouldThrowException - null 값으로 예외 발생")
        void constructor_WithNullValue_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> new FileVariantId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileVariantId must be positive");
        }

        @Test
        @DisplayName("constructor_WithZeroValue_ShouldThrowException - 0 값으로 예외 발생")
        void constructor_WithZeroValue_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> new FileVariantId(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileVariantId must be positive");
        }

        @Test
        @DisplayName("constructor_WithNegativeValue_ShouldThrowException - 음수 값으로 예외 발생")
        void constructor_WithNegativeValue_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> new FileVariantId(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileVariantId must be positive");
        }
    }

    @Nested
    @DisplayName("of 메서드 테스트")
    class OfTests {

        @Test
        @DisplayName("of_WithPositiveValue_ShouldCreateFileVariantId - 양수 값으로 생성")
        void of_WithPositiveValue_ShouldCreateFileVariantId() {
            // When
            FileVariantId id = FileVariantId.of(1L);

            // Then
            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("of_WithNullValue_ShouldThrowException - null 값으로 예외 발생")
        void of_WithNullValue_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> FileVariantId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileVariantId must be positive");
        }

        @Test
        @DisplayName("of_WithLargeValue_ShouldCreateFileVariantId - 큰 값으로 생성")
        void of_WithLargeValue_ShouldCreateFileVariantId() {
            // When
            FileVariantId id = FileVariantId.of(Long.MAX_VALUE);

            // Then
            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(Long.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("value 메서드 테스트")
    class ValueTests {

        @Test
        @DisplayName("value_ShouldReturnLongValue - Long 값 반환")
        void value_ShouldReturnLongValue() {
            // Given
            FileVariantId id = FileVariantId.of(123L);

            // When
            Long value = id.value();

            // Then
            assertThat(value).isEqualTo(123L);
            assertThat(value).isInstanceOf(Long.class);
        }
    }
}


