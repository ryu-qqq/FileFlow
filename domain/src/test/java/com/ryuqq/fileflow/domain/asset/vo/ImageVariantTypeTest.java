package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ImageVariantType 단위 테스트")
class ImageVariantTypeTest {

    @Nested
    @DisplayName("Enum 기본 테스트")
    class EnumBasicTest {

        @Test
        @DisplayName("모든 이미지 변형 타입이 정의되어 있다")
        void values_ShouldContainAllTypes() {
            // when
            ImageVariantType[] values = ImageVariantType.values();

            // then
            assertThat(values).hasSize(4);
            assertThat(values)
                    .containsExactly(
                            ImageVariantType.ORIGINAL,
                            ImageVariantType.LARGE,
                            ImageVariantType.MEDIUM,
                            ImageVariantType.THUMBNAIL);
        }

        @Test
        @DisplayName("문자열로 타입을 찾을 수 있다")
        void valueOf_WithValidName_ShouldReturnType() {
            // when & then
            assertThat(ImageVariantType.valueOf("ORIGINAL")).isEqualTo(ImageVariantType.ORIGINAL);
            assertThat(ImageVariantType.valueOf("LARGE")).isEqualTo(ImageVariantType.LARGE);
            assertThat(ImageVariantType.valueOf("MEDIUM")).isEqualTo(ImageVariantType.MEDIUM);
            assertThat(ImageVariantType.valueOf("THUMBNAIL")).isEqualTo(ImageVariantType.THUMBNAIL);
        }
    }

    @Nested
    @DisplayName("타입별 테스트")
    class TypeTest {

        @Test
        @DisplayName("ORIGINAL 타입이 존재한다")
        void shouldHaveOriginalType() {
            // when
            ImageVariantType type = ImageVariantType.ORIGINAL;

            // then
            assertThat(type).isNotNull();
            assertThat(type.name()).isEqualTo("ORIGINAL");
        }

        @Test
        @DisplayName("LARGE 타입이 존재한다")
        void shouldHaveLargeType() {
            // when
            ImageVariantType type = ImageVariantType.LARGE;

            // then
            assertThat(type).isNotNull();
            assertThat(type.name()).isEqualTo("LARGE");
        }

        @Test
        @DisplayName("MEDIUM 타입이 존재한다")
        void shouldHaveMediumType() {
            // when
            ImageVariantType type = ImageVariantType.MEDIUM;

            // then
            assertThat(type).isNotNull();
            assertThat(type.name()).isEqualTo("MEDIUM");
        }

        @Test
        @DisplayName("THUMBNAIL 타입이 존재한다")
        void shouldHaveThumbnailType() {
            // when
            ImageVariantType type = ImageVariantType.THUMBNAIL;

            // then
            assertThat(type).isNotNull();
            assertThat(type.name()).isEqualTo("THUMBNAIL");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 타입은 동등하다")
        void equals_WithSameType_ShouldBeEqual() {
            // given
            ImageVariantType type1 = ImageVariantType.ORIGINAL;
            ImageVariantType type2 = ImageVariantType.ORIGINAL;

            // when & then
            assertThat(type1).isEqualTo(type2);
            assertThat(type1 == type2).isTrue();
        }

        @Test
        @DisplayName("다른 타입은 동등하지 않다")
        void equals_WithDifferentType_ShouldNotBeEqual() {
            // given
            ImageVariantType type1 = ImageVariantType.ORIGINAL;
            ImageVariantType type2 = ImageVariantType.THUMBNAIL;

            // when & then
            assertThat(type1).isNotEqualTo(type2);
        }
    }
}
