package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileCategory Value Object 테스트
 */
class FileCategoryTest {

    @ParameterizedTest
    @ValueSource(strings = {"상품", "전시영역", "외부몰연동", "문서"})
    @DisplayName("허용된 카테고리로 FileCategory를 생성해야 한다")
    void shouldCreateValidFileCategory(String validCategory) {
        // when
        FileCategory fileCategory = FileCategory.of(validCategory);

        // then
        assertThat(fileCategory).isNotNull();
        assertThat(fileCategory.getValue()).isEqualTo(validCategory);
    }

    @Test
    @DisplayName("null로 생성 시 기본값 '기타'를 반환해야 한다")
    void shouldReturnDefaultCategoryWhenNull() {
        // when
        FileCategory fileCategory = FileCategory.of(null);

        // then
        assertThat(fileCategory).isNotNull();
        assertThat(fileCategory.getValue()).isEqualTo("기타");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "잘못된카테고리", "unknown"})
    @DisplayName("허용되지 않은 카테고리는 예외가 발생해야 한다")
    void shouldThrowExceptionWhenCategoryIsNotAllowed(String invalidCategory) {
        // when & then
        assertThatThrownBy(() -> FileCategory.of(invalidCategory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않은 파일 카테고리입니다");
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환해야 한다")
    void shouldReturnSameValueFromGetValue() {
        // given
        String expectedValue = "상품";
        FileCategory fileCategory = FileCategory.of(expectedValue);

        // when
        String actualValue = fileCategory.getValue();

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("같은 값을 가진 FileCategory는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String value = "전시영역";
        FileCategory fileCategory1 = FileCategory.of(value);
        FileCategory fileCategory2 = FileCategory.of(value);

        // when & then
        assertThat(fileCategory1).isEqualTo(fileCategory2);
    }

    @Test
    @DisplayName("같은 값을 가진 FileCategory는 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenValueIsSame() {
        // given
        String value = "문서";
        FileCategory fileCategory1 = FileCategory.of(value);
        FileCategory fileCategory2 = FileCategory.of(value);

        // when & then
        assertThat(fileCategory1.hashCode()).isEqualTo(fileCategory2.hashCode());
    }

    @Test
    @DisplayName("ofDefault()는 기본값 '기타'를 반환해야 한다")
    void shouldReturnDefaultCategory() {
        // when
        FileCategory fileCategory = FileCategory.ofDefault();

        // then
        assertThat(fileCategory.getValue()).isEqualTo("기타");
    }
}
