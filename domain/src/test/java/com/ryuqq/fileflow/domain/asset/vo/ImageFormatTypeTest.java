package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ImageFormatType 단위 테스트")
class ImageFormatTypeTest {

    @Nested
    @DisplayName("Enum 기본 테스트")
    class EnumBasicTest {

        @Test
        @DisplayName("모든 이미지 포맷 타입이 정의되어 있다")
        void values_ShouldContainAllTypes() {
            // when
            ImageFormatType[] values = ImageFormatType.values();

            // then
            assertThat(values).hasSize(3);
            assertThat(values)
                    .containsExactly(
                            ImageFormatType.WEBP, ImageFormatType.JPEG, ImageFormatType.PNG);
        }

        @Test
        @DisplayName("문자열로 타입을 찾을 수 있다")
        void valueOf_WithValidName_ShouldReturnType() {
            // when & then
            assertThat(ImageFormatType.valueOf("WEBP")).isEqualTo(ImageFormatType.WEBP);
            assertThat(ImageFormatType.valueOf("JPEG")).isEqualTo(ImageFormatType.JPEG);
            assertThat(ImageFormatType.valueOf("PNG")).isEqualTo(ImageFormatType.PNG);
        }
    }

    @Nested
    @DisplayName("타입별 테스트")
    class TypeTest {

        @Test
        @DisplayName("WEBP 타입이 존재한다")
        void shouldHaveWebpType() {
            // when
            ImageFormatType type = ImageFormatType.WEBP;

            // then
            assertThat(type).isNotNull();
            assertThat(type.name()).isEqualTo("WEBP");
        }

        @Test
        @DisplayName("JPEG 타입이 존재한다")
        void shouldHaveJpegType() {
            // when
            ImageFormatType type = ImageFormatType.JPEG;

            // then
            assertThat(type).isNotNull();
            assertThat(type.name()).isEqualTo("JPEG");
        }

        @Test
        @DisplayName("PNG 타입이 존재한다")
        void shouldHavePngType() {
            // when
            ImageFormatType type = ImageFormatType.PNG;

            // then
            assertThat(type).isNotNull();
            assertThat(type.name()).isEqualTo("PNG");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 타입은 동등하다")
        void equals_WithSameType_ShouldBeEqual() {
            // given
            ImageFormatType type1 = ImageFormatType.WEBP;
            ImageFormatType type2 = ImageFormatType.WEBP;

            // when & then
            assertThat(type1).isEqualTo(type2);
            assertThat(type1 == type2).isTrue();
        }

        @Test
        @DisplayName("다른 타입은 동등하지 않다")
        void equals_WithDifferentType_ShouldNotBeEqual() {
            // given
            ImageFormatType type1 = ImageFormatType.WEBP;
            ImageFormatType type2 = ImageFormatType.PNG;

            // when & then
            assertThat(type1).isNotEqualTo(type2);
        }
    }
}
