package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("UploadCategory 단위 테스트")
class UploadCategoryTest {

    @Nested
    @DisplayName("Enum 기본 테스트")
    class EnumBasicTest {

        @Test
        @DisplayName("모든 카테고리가 정의되어 있다")
        void values_ShouldContainAllCategories() {
            // when
            UploadCategory[] values = UploadCategory.values();

            // then
            assertThat(values).hasSize(6);
            assertThat(values)
                    .containsExactly(
                            UploadCategory.BANNER,
                            UploadCategory.EXCEL,
                            UploadCategory.SALES_MATERIAL,
                            UploadCategory.PRODUCT_IMAGE,
                            UploadCategory.DOCUMENT,
                            UploadCategory.HTML);
        }
    }

    @Nested
    @DisplayName("경로 반환 테스트")
    class PathTest {

        @ParameterizedTest
        @CsvSource({
            "BANNER, banner",
            "EXCEL, excel",
            "SALES_MATERIAL, sales",
            "PRODUCT_IMAGE, product",
            "DOCUMENT, document"
        })
        @DisplayName("카테고리별 경로를 반환한다")
        void getPath_ShouldReturnCorrectPath(UploadCategory category, String expectedPath) {
            // when
            String path = category.getPath();

            // then
            assertThat(path).isEqualTo(expectedPath);
        }
    }

    @Nested
    @DisplayName("설명 반환 테스트")
    class DescriptionTest {

        @ParameterizedTest
        @CsvSource({
            "BANNER, 배너 이미지",
            "EXCEL, 엑셀 데이터",
            "SALES_MATERIAL, 판매 자료",
            "PRODUCT_IMAGE, 상품 이미지",
            "DOCUMENT, 문서"
        })
        @DisplayName("카테고리별 설명을 반환한다")
        void getDescription_ShouldReturnCorrectDescription(
                UploadCategory category, String expectedDescription) {
            // when
            String description = category.getDescription();

            // then
            assertThat(description).isEqualTo(expectedDescription);
        }
    }

    @Nested
    @DisplayName("경로 문자열로 카테고리 찾기 테스트")
    class FromPathTest {

        @ParameterizedTest
        @CsvSource({
            "banner, BANNER",
            "excel, EXCEL",
            "sales, SALES_MATERIAL",
            "product, PRODUCT_IMAGE",
            "document, DOCUMENT"
        })
        @DisplayName("경로 문자열로 카테고리를 찾을 수 있다")
        void fromPath_WithValidPath_ShouldReturnCategory(
                String path, UploadCategory expectedCategory) {
            // when
            UploadCategory category = UploadCategory.fromPath(path);

            // then
            assertThat(category).isEqualTo(expectedCategory);
        }

        @ParameterizedTest
        @CsvSource({"BANNER, BANNER", "Banner, BANNER", "EXCEL, EXCEL", "Excel, EXCEL"})
        @DisplayName("대소문자 구분 없이 경로로 카테고리를 찾을 수 있다")
        void fromPath_WithCaseInsensitivePath_ShouldReturnCategory(
                String path, UploadCategory expectedCategory) {
            // when
            UploadCategory category = UploadCategory.fromPath(path);

            // then
            assertThat(category).isEqualTo(expectedCategory);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("null 또는 빈 문자열로 찾을 시 예외가 발생한다")
        void fromPath_WithNullOrEmpty_ShouldThrowException(String path) {
            // when & then
            assertThatThrownBy(() -> UploadCategory.fromPath(path))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null이거나 빈 문자열");
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid", "unknown", "image", "video"})
        @DisplayName("유효하지 않은 경로로 찾을 시 예외가 발생한다")
        void fromPath_WithInvalidPath_ShouldThrowException(String path) {
            // when & then
            assertThatThrownBy(() -> UploadCategory.fromPath(path))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 업로드 카테고리");
        }
    }

    @Nested
    @DisplayName("카테고리 유형 확인 테스트")
    class CategoryTypeTest {

        @Test
        @DisplayName("BANNER는 배너 카테고리이다")
        void isBanner_WithBanner_ShouldReturnTrue() {
            assertThat(UploadCategory.BANNER.isBanner()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"EXCEL", "SALES_MATERIAL", "PRODUCT_IMAGE", "DOCUMENT"})
        @DisplayName("BANNER 외의 카테고리는 배너가 아니다")
        void isBanner_WithOtherCategories_ShouldReturnFalse(String categoryName) {
            // given
            UploadCategory category = UploadCategory.valueOf(categoryName);

            // then
            assertThat(category.isBanner()).isFalse();
        }

        @Test
        @DisplayName("EXCEL은 엑셀 카테고리이다")
        void isExcel_WithExcel_ShouldReturnTrue() {
            assertThat(UploadCategory.EXCEL.isExcel()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"BANNER", "SALES_MATERIAL", "PRODUCT_IMAGE", "DOCUMENT"})
        @DisplayName("EXCEL 외의 카테고리는 엑셀이 아니다")
        void isExcel_WithOtherCategories_ShouldReturnFalse(String categoryName) {
            // given
            UploadCategory category = UploadCategory.valueOf(categoryName);

            // then
            assertThat(category.isExcel()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"BANNER", "PRODUCT_IMAGE"})
        @DisplayName("BANNER와 PRODUCT_IMAGE는 이미지 카테고리이다")
        void isImage_WithImageCategories_ShouldReturnTrue(String categoryName) {
            // given
            UploadCategory category = UploadCategory.valueOf(categoryName);

            // then
            assertThat(category.isImage()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"EXCEL", "SALES_MATERIAL", "DOCUMENT"})
        @DisplayName("이미지 카테고리가 아닌 경우 false를 반환한다")
        void isImage_WithNonImageCategories_ShouldReturnFalse(String categoryName) {
            // given
            UploadCategory category = UploadCategory.valueOf(categoryName);

            // then
            assertThat(category.isImage()).isFalse();
        }
    }

    @Nested
    @DisplayName("HTML 카테고리 테스트")
    class HtmlCategoryTest {

        @Test
        @DisplayName("HTML 카테고리가 존재한다")
        void shouldHaveHtmlCategory() {
            // when
            UploadCategory htmlCategory = UploadCategory.HTML;

            // then
            assertThat(htmlCategory).isNotNull();
            assertThat(htmlCategory.getPath()).isEqualTo("html");
            assertThat(htmlCategory.getDescription()).isEqualTo("HTML 문서");
        }

        @Test
        @DisplayName("HTML 카테고리는 isHtml()이 true를 반환한다")
        void shouldReturnTrueForHtmlCategory() {
            // given
            UploadCategory htmlCategory = UploadCategory.HTML;

            // when & then
            assertThat(htmlCategory.isHtml()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"BANNER", "EXCEL", "SALES_MATERIAL", "PRODUCT_IMAGE", "DOCUMENT"})
        @DisplayName("HTML이 아닌 카테고리는 isHtml()이 false를 반환한다")
        void shouldReturnFalseForNonHtmlCategory(String categoryName) {
            // given
            UploadCategory category = UploadCategory.valueOf(categoryName);

            // then
            assertThat(category.isHtml()).isFalse();
        }
    }
}
