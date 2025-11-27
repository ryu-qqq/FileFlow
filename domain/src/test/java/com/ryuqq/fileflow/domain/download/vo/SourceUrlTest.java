package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("SourceUrl 단위 테스트")
class SourceUrlTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 HTTP URL로 생성할 수 있다")
        void of_WithValidHttpUrl_ShouldCreate() {
            // given
            String url = "http://example.com/image.jpg";

            // when
            SourceUrl sourceUrl = SourceUrl.of(url);

            // then
            assertThat(sourceUrl.value()).isEqualTo(url);
        }

        @Test
        @DisplayName("유효한 HTTPS URL로 생성할 수 있다")
        void of_WithValidHttpsUrl_ShouldCreate() {
            // given
            String url = "https://example.com/image.png";

            // when
            SourceUrl sourceUrl = SourceUrl.of(url);

            // then
            assertThat(sourceUrl.value()).isEqualTo(url);
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "https://example.com/path/to/image.jpg",
                    "https://cdn.example.com/images/photo.png",
                    "http://example.com/image.gif",
                    "https://example.com/image.webp",
                    "https://example.com/image.bmp",
                    "https://example.com/path/image",
                    "https://example.com/image?param=value"
                })
        @DisplayName("다양한 형태의 이미지 URL을 허용한다")
        void of_WithVariousImageUrls_ShouldCreate(String url) {
            // when
            SourceUrl sourceUrl = SourceUrl.of(url);

            // then
            assertThat(sourceUrl.value()).isEqualTo(url);
        }

        @Test
        @DisplayName("null 값으로 생성 시 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> SourceUrl.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("SourceUrl");
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 예외가 발생한다")
        void of_WithBlank_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> SourceUrl.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비어");

            assertThatThrownBy(() -> SourceUrl.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비어");
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "ftp://example.com/image.jpg",
                    "file:///local/image.jpg",
                    "invalid-url",
                    "example.com/image.jpg",
                    "//example.com/image.jpg"
                })
        @DisplayName("HTTP/HTTPS가 아닌 URL로 생성 시 예외가 발생한다")
        void of_WithInvalidProtocol_ShouldThrowException(String url) {
            // given & when & then
            assertThatThrownBy(() -> SourceUrl.of(url))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("http://")
                    .hasMessageContaining("https://");
        }
    }

    @Nested
    @DisplayName("extractFileName 테스트")
    class ExtractFileNameTest {

        @Test
        @DisplayName("쿼리 파라미터가 없는 URL에서 파일명을 추출한다")
        void extractFileName_WithoutQueryParams_ShouldExtractFileName() {
            // given
            SourceUrl sourceUrl = SourceUrl.of("https://example.com/path/to/image.jpg");

            // when
            var fileName = sourceUrl.extractFileName("jpg");

            // then
            assertThat(fileName.name()).isEqualTo("image.jpg");
        }

        @Test
        @DisplayName("쿼리 파라미터가 있는 URL에서 파일명을 추출한다")
        void extractFileName_WithQueryParams_ShouldExtractFileName() {
            // given
            SourceUrl sourceUrl =
                    SourceUrl.of("https://example.com/path/image.png?token=abc&size=large");

            // when
            var fileName = sourceUrl.extractFileName("png");

            // then
            assertThat(fileName.name()).isEqualTo("image.png");
        }

        @Test
        @DisplayName("파일명이 없는 URL에서 기본값을 반환한다")
        void extractFileName_WithNoFileName_ShouldReturnDefault() {
            // given
            SourceUrl sourceUrl = SourceUrl.of("https://example.com/path/");

            // when
            var fileName = sourceUrl.extractFileName("jpg");

            // then
            assertThat(fileName.name()).isEqualTo("external-download.jpg");
        }

        @Test
        @DisplayName("루트 경로 URL에서 기본값을 반환한다")
        void extractFileName_WithRootPath_ShouldReturnDefault() {
            // given
            SourceUrl sourceUrl = SourceUrl.of("https://example.com/");

            // when
            var fileName = sourceUrl.extractFileName("png");

            // then
            assertThat(fileName.name()).isEqualTo("external-download.png");
        }

        @Test
        @DisplayName("복잡한 경로의 URL에서 파일명을 추출한다")
        void extractFileName_WithComplexPath_ShouldExtractFileName() {
            // given
            SourceUrl sourceUrl =
                    SourceUrl.of("https://cdn.example.com/a/b/c/d/e/f/final-image.webp");

            // when
            var fileName = sourceUrl.extractFileName("webp");

            // then
            assertThat(fileName.name()).isEqualTo("final-image.webp");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 URL을 가진 SourceUrl은 동등하다")
        void equals_WithSameUrl_ShouldBeEqual() {
            // given
            String url = "https://example.com/image.jpg";
            SourceUrl url1 = SourceUrl.of(url);
            SourceUrl url2 = SourceUrl.of(url);

            // when & then
            assertThat(url1).isEqualTo(url2);
            assertThat(url1.hashCode()).isEqualTo(url2.hashCode());
        }

        @Test
        @DisplayName("다른 URL을 가진 SourceUrl은 동등하지 않다")
        void equals_WithDifferentUrl_ShouldNotBeEqual() {
            // given
            SourceUrl url1 = SourceUrl.of("https://example.com/image1.jpg");
            SourceUrl url2 = SourceUrl.of("https://example.com/image2.jpg");

            // when & then
            assertThat(url1).isNotEqualTo(url2);
        }
    }
}
