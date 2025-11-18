package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileCategory Value Object 테스트
 * <p>
 * UploaderType별 카테고리 검증 확인
 * </p>
 */
@DisplayName("FileCategory Value Object 테스트")
class FileCategoryTest {

    @ParameterizedTest
    @ValueSource(strings = {"banner", "event", "excel", "notice", "default"})
    @DisplayName("Admin 허용 카테고리로 FileCategory를 생성해야 한다")
    void shouldCreateValidAdminCategory(String validCategory) {
        // when
        FileCategory fileCategory = FileCategory.of(validCategory, UploaderType.ADMIN);

        // then
        assertThat(fileCategory).isNotNull();
        assertThat(fileCategory.getValue()).isEqualTo(validCategory.toLowerCase());
    }

    @ParameterizedTest
    @ValueSource(strings = {"product", "review", "promotion", "default"})
    @DisplayName("Seller 허용 카테고리로 FileCategory를 생성해야 한다")
    void shouldCreateValidSellerCategory(String validCategory) {
        // when
        FileCategory fileCategory = FileCategory.of(validCategory, UploaderType.SELLER);

        // then
        assertThat(fileCategory).isNotNull();
        assertThat(fileCategory.getValue()).isEqualTo(validCategory.toLowerCase());
    }

    @Test
    @DisplayName("Customer는 default 카테고리만 허용해야 한다")
    void shouldCreateDefaultCategoryForCustomer() {
        // when
        FileCategory fileCategory = FileCategory.of("default", UploaderType.CUSTOMER);

        // then
        assertThat(fileCategory).isNotNull();
        assertThat(fileCategory.getValue()).isEqualTo("default");
    }

    @Test
    @DisplayName("null로 생성 시 기본값 'default'를 반환해야 한다")
    void shouldReturnDefaultCategoryWhenNull() {
        // when
        FileCategory fileCategory = FileCategory.of(null, UploaderType.ADMIN);

        // then
        assertThat(fileCategory).isNotNull();
        assertThat(fileCategory.getValue()).isEqualTo("default");
    }

    @Test
    @DisplayName("빈 문자열로 생성 시 기본값 'default'를 반환해야 한다")
    void shouldReturnDefaultCategoryWhenBlank() {
        // when
        FileCategory fileCategory = FileCategory.of("   ", UploaderType.SELLER);

        // then
        assertThat(fileCategory).isNotNull();
        assertThat(fileCategory.getValue()).isEqualTo("default");
    }

    @Test
    @DisplayName("Admin에서 허용되지 않은 카테고리는 예외가 발생해야 한다")
    void shouldThrowExceptionWhenAdminCategoryIsNotAllowed() {
        // when & then
        assertThatThrownBy(() -> FileCategory.of("product", UploaderType.ADMIN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ADMIN에서 지원하지 않는 카테고리입니다");
    }

    @Test
    @DisplayName("Seller에서 허용되지 않은 카테고리는 예외가 발생해야 한다")
    void shouldThrowExceptionWhenSellerCategoryIsNotAllowed() {
        // when & then
        assertThatThrownBy(() -> FileCategory.of("banner", UploaderType.SELLER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SELLER에서 지원하지 않는 카테고리입니다");
    }

    @Test
    @DisplayName("Customer에서 default 외 카테고리는 예외가 발생해야 한다")
    void shouldThrowExceptionWhenCustomerCategoryIsNotDefault() {
        // when & then
        assertThatThrownBy(() -> FileCategory.of("product", UploaderType.CUSTOMER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CUSTOMER에서 지원하지 않는 카테고리입니다");
    }

    @Test
    @DisplayName("대소문자 구분 없이 카테고리를 생성해야 한다 (소문자 정규화)")
    void shouldNormalizeCategoryToLowerCase() {
        // when
        FileCategory fileCategory = FileCategory.of("BANNER", UploaderType.ADMIN);

        // then
        assertThat(fileCategory.getValue()).isEqualTo("banner");
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환해야 한다")
    void shouldReturnSameValueFromGetValue() {
        // given
        String expectedValue = "event";
        FileCategory fileCategory = FileCategory.of(expectedValue, UploaderType.ADMIN);

        // when
        String actualValue = fileCategory.getValue();

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("같은 값을 가진 FileCategory는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String value = "product";
        FileCategory fileCategory1 = FileCategory.of(value, UploaderType.SELLER);
        FileCategory fileCategory2 = FileCategory.of(value, UploaderType.SELLER);

        // when & then
        assertThat(fileCategory1).isEqualTo(fileCategory2);
    }

    @Test
    @DisplayName("같은 값을 가진 FileCategory는 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenValueIsSame() {
        // given
        String value = "review";
        FileCategory fileCategory1 = FileCategory.of(value, UploaderType.SELLER);
        FileCategory fileCategory2 = FileCategory.of(value, UploaderType.SELLER);

        // when & then
        assertThat(fileCategory1.hashCode()).isEqualTo(fileCategory2.hashCode());
    }

    @Test
    @DisplayName("defaultCategory()는 기본값 'default'를 반환해야 한다")
    void shouldReturnDefaultCategory() {
        // when
        FileCategory fileCategory = FileCategory.defaultCategory();

        // then
        assertThat(fileCategory.getValue()).isEqualTo("default");
    }
}
