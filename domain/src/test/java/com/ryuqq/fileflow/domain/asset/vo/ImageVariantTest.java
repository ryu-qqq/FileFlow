package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ImageVariant 단위 테스트")
class ImageVariantTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 ImageVariant를 생성할 수 있다")
        void shouldCreateImageVariantWithValidData() {
            // given
            ImageVariantType type = ImageVariantType.LARGE;
            String suffix = "_large";

            // when
            ImageVariant variant = ImageVariant.of(type, suffix);

            // then
            assertThat(variant.type()).isEqualTo(type);
            assertThat(variant.suffix()).isEqualTo(suffix);
        }

        @Test
        @DisplayName("타입이 null이면 예외가 발생한다")
        void shouldThrowWhenTypeIsNull() {
            // given
            ImageVariantType nullType = null;
            String suffix = "_large";

            // when & then
            assertThatThrownBy(() -> ImageVariant.of(nullType, suffix))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("suffix가 null이면 예외가 발생한다")
        void shouldThrowWhenSuffixIsNull() {
            // given
            ImageVariantType type = ImageVariantType.LARGE;
            String nullSuffix = null;

            // when & then
            assertThatThrownBy(() -> ImageVariant.of(type, nullSuffix))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("requiresResize 테스트")
    class RequiresResizeTest {

        @Test
        @DisplayName("ORIGINAL이 아닌 타입은 리사이즈가 필요하다")
        void shouldReturnTrueForRequiresResizeWhenNotOriginal() {
            // given
            ImageVariant largeVariant = ImageVariant.of(ImageVariantType.LARGE, "_large");
            ImageVariant mediumVariant = ImageVariant.of(ImageVariantType.MEDIUM, "_medium");
            ImageVariant thumbnailVariant = ImageVariant.of(ImageVariantType.THUMBNAIL, "_thumb");

            // when & then
            assertThat(largeVariant.requiresResize()).isTrue();
            assertThat(mediumVariant.requiresResize()).isTrue();
            assertThat(thumbnailVariant.requiresResize()).isTrue();
        }

        @Test
        @DisplayName("ORIGINAL 타입은 리사이즈가 필요하지 않다")
        void shouldReturnFalseForRequiresResizeWhenOriginal() {
            // given
            ImageVariant originalVariant = ImageVariant.of(ImageVariantType.ORIGINAL, "");

            // when & then
            assertThat(originalVariant.requiresResize()).isFalse();
        }
    }

    @Nested
    @DisplayName("표준 상수 테스트")
    class StandardConstantsTest {

        @Test
        @DisplayName("ORIGINAL 상수가 정의되어 있다")
        void shouldHaveOriginalConstant() {
            // when
            ImageVariant original = ImageVariant.ORIGINAL;

            // then
            assertThat(original.type()).isEqualTo(ImageVariantType.ORIGINAL);
            assertThat(original.suffix()).isEmpty();
        }

        @Test
        @DisplayName("LARGE 상수가 정의되어 있다")
        void shouldHaveLargeConstant() {
            // when
            ImageVariant large = ImageVariant.LARGE;

            // then
            assertThat(large.type()).isEqualTo(ImageVariantType.LARGE);
            assertThat(large.suffix()).isEqualTo("_large");
        }

        @Test
        @DisplayName("MEDIUM 상수가 정의되어 있다")
        void shouldHaveMediumConstant() {
            // when
            ImageVariant medium = ImageVariant.MEDIUM;

            // then
            assertThat(medium.type()).isEqualTo(ImageVariantType.MEDIUM);
            assertThat(medium.suffix()).isEqualTo("_medium");
        }

        @Test
        @DisplayName("THUMBNAIL 상수가 정의되어 있다")
        void shouldHaveThumbnailConstant() {
            // when
            ImageVariant thumbnail = ImageVariant.THUMBNAIL;

            // then
            assertThat(thumbnail.type()).isEqualTo(ImageVariantType.THUMBNAIL);
            assertThat(thumbnail.suffix()).isEqualTo("_thumb");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 타입과 suffix를 가진 ImageVariant는 동등하다")
        void equals_WithSameTypeAndSuffix_ShouldBeEqual() {
            // given
            ImageVariant variant1 = ImageVariant.of(ImageVariantType.LARGE, "_large");
            ImageVariant variant2 = ImageVariant.of(ImageVariantType.LARGE, "_large");

            // when & then
            assertThat(variant1).isEqualTo(variant2);
            assertThat(variant1.hashCode()).isEqualTo(variant2.hashCode());
        }

        @Test
        @DisplayName("다른 타입을 가진 ImageVariant는 동등하지 않다")
        void equals_WithDifferentType_ShouldNotBeEqual() {
            // given
            ImageVariant variant1 = ImageVariant.of(ImageVariantType.LARGE, "_large");
            ImageVariant variant2 = ImageVariant.of(ImageVariantType.MEDIUM, "_medium");

            // when & then
            assertThat(variant1).isNotEqualTo(variant2);
        }
    }
}
