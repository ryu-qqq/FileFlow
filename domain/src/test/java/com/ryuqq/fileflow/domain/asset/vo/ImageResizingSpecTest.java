package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ImageResizingSpec 단위 테스트")
class ImageResizingSpecTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 ImageResizingSpec을 생성할 수 있다")
        void shouldCreateImageResizingSpecWithValidData() {
            // given
            ImageVariant variant = ImageVariant.LARGE;
            ImageFormat format = ImageFormat.WEBP;

            // when
            ImageResizingSpec spec = ImageResizingSpec.of(variant, format);

            // then
            assertThat(spec.variant()).isEqualTo(variant);
            assertThat(spec.format()).isEqualTo(format);
        }

        @Test
        @DisplayName("variant가 null이면 예외가 발생한다")
        void shouldThrowWhenVariantIsNull() {
            // given
            ImageVariant nullVariant = null;
            ImageFormat format = ImageFormat.WEBP;

            // when & then
            assertThatThrownBy(() -> ImageResizingSpec.of(nullVariant, format))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("format이 null이면 예외가 발생한다")
        void shouldThrowWhenFormatIsNull() {
            // given
            ImageVariant variant = ImageVariant.LARGE;
            ImageFormat nullFormat = null;

            // when & then
            assertThatThrownBy(() -> ImageResizingSpec.of(variant, nullFormat))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("specId 테스트")
    class SpecIdTest {

        @Test
        @DisplayName("specId는 'variant_format' 형식이다")
        void shouldReturnSpecIdInCorrectFormat() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);

            // when
            String specId = spec.specId();

            // then
            assertThat(specId).isEqualTo("LARGE_WEBP");
        }

        @Test
        @DisplayName("THUMBNAIL + JPEG 조합의 specId를 반환한다")
        void shouldReturnSpecIdForThumbnailJpeg() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.THUMBNAIL, ImageFormat.JPEG);

            // when
            String specId = spec.specId();

            // then
            assertThat(specId).isEqualTo("THUMBNAIL_JPEG");
        }
    }

    @Nested
    @DisplayName("requiresResize 테스트")
    class RequiresResizeTest {

        @Test
        @DisplayName("ORIGINAL이 아닌 variant는 리사이즈가 필요하다")
        void shouldRequireResizeWhenNotOriginal() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);

            // when & then
            assertThat(spec.requiresResize()).isTrue();
        }

        @Test
        @DisplayName("ORIGINAL variant는 리사이즈가 필요하지 않다")
        void shouldNotRequireResizeWhenOriginal() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.ORIGINAL, ImageFormat.WEBP);

            // when & then
            assertThat(spec.requiresResize()).isFalse();
        }
    }

    @Nested
    @DisplayName("위임 메서드 테스트")
    class DelegationTest {

        @Test
        @DisplayName("suffix()는 variant의 suffix를 반환한다")
        void shouldReturnVariantSuffix() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.THUMBNAIL, ImageFormat.WEBP);

            // when
            String suffix = spec.suffix();

            // then
            assertThat(suffix).isEqualTo("_thumb");
        }

        @Test
        @DisplayName("extension()은 format의 extension을 반환한다")
        void shouldReturnFormatExtension() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);

            // when
            String extension = spec.extension();

            // then
            assertThat(extension).isEqualTo("webp");
        }

        @Test
        @DisplayName("mimeType()은 format의 mimeType을 반환한다")
        void shouldReturnFormatMimeType() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.JPEG);

            // when
            String mimeType = spec.mimeType();

            // then
            assertThat(mimeType).isEqualTo("image/jpeg");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 variant와 format을 가진 spec은 동등하다")
        void shouldBeEqualWithSameVariantAndFormat() {
            // given
            ImageResizingSpec spec1 = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);
            ImageResizingSpec spec2 = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);

            // when & then
            assertThat(spec1).isEqualTo(spec2);
            assertThat(spec1.hashCode()).isEqualTo(spec2.hashCode());
        }

        @Test
        @DisplayName("다른 variant를 가진 spec은 동등하지 않다")
        void shouldNotBeEqualWithDifferentVariant() {
            // given
            ImageResizingSpec spec1 = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);
            ImageResizingSpec spec2 = ImageResizingSpec.of(ImageVariant.MEDIUM, ImageFormat.WEBP);

            // when & then
            assertThat(spec1).isNotEqualTo(spec2);
        }

        @Test
        @DisplayName("다른 format을 가진 spec은 동등하지 않다")
        void shouldNotBeEqualWithDifferentFormat() {
            // given
            ImageResizingSpec spec1 = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);
            ImageResizingSpec spec2 = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.JPEG);

            // when & then
            assertThat(spec1).isNotEqualTo(spec2);
        }
    }
}
