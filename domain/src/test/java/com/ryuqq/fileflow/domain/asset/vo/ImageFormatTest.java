package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ImageFormat 단위 테스트")
class ImageFormatTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 ImageFormat을 생성할 수 있다")
        void shouldCreateImageFormatWithValidData() {
            // given
            ImageFormatType type = ImageFormatType.WEBP;
            String extension = "webp";
            String mimeType = "image/webp";

            // when
            ImageFormat format = ImageFormat.of(type, extension, mimeType);

            // then
            assertThat(format.type()).isEqualTo(type);
            assertThat(format.extension()).isEqualTo(extension);
            assertThat(format.mimeType()).isEqualTo(mimeType);
        }

        @Test
        @DisplayName("타입이 null이면 예외가 발생한다")
        void shouldThrowWhenTypeIsNull() {
            // given
            ImageFormatType nullType = null;
            String extension = "webp";
            String mimeType = "image/webp";

            // when & then
            assertThatThrownBy(() -> ImageFormat.of(nullType, extension, mimeType))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("확장자가 null이면 예외가 발생한다")
        void shouldThrowWhenExtensionIsNull() {
            // given
            ImageFormatType type = ImageFormatType.WEBP;
            String nullExtension = null;
            String mimeType = "image/webp";

            // when & then
            assertThatThrownBy(() -> ImageFormat.of(type, nullExtension, mimeType))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("확장자가 빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenExtensionIsBlank() {
            // given
            ImageFormatType type = ImageFormatType.WEBP;
            String blankExtension = "   ";
            String mimeType = "image/webp";

            // when & then
            assertThatThrownBy(() -> ImageFormat.of(type, blankExtension, mimeType))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("MIME 타입이 null이면 예외가 발생한다")
        void shouldThrowWhenMimeTypeIsNull() {
            // given
            ImageFormatType type = ImageFormatType.WEBP;
            String extension = "webp";
            String nullMimeType = null;

            // when & then
            assertThatThrownBy(() -> ImageFormat.of(type, extension, nullMimeType))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("MIME 타입이 빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenMimeTypeIsBlank() {
            // given
            ImageFormatType type = ImageFormatType.WEBP;
            String extension = "webp";
            String blankMimeType = "   ";

            // when & then
            assertThatThrownBy(() -> ImageFormat.of(type, extension, blankMimeType))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("fromOriginal 테스트")
    class FromOriginalTest {

        @Test
        @DisplayName("PNG 확장자면 PNG 포맷을 반환한다")
        void shouldReturnPngFromPngExtension() {
            // given
            String originalExtension = "png";

            // when
            ImageFormat format = ImageFormat.fromOriginal(originalExtension);

            // then
            assertThat(format.type()).isEqualTo(ImageFormatType.PNG);
            assertThat(format.extension()).isEqualTo("png");
            assertThat(format.mimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("JPG 확장자면 JPEG 포맷을 반환한다")
        void shouldReturnJpegFromJpgExtension() {
            // given
            String originalExtension = "jpg";

            // when
            ImageFormat format = ImageFormat.fromOriginal(originalExtension);

            // then
            assertThat(format.type()).isEqualTo(ImageFormatType.JPEG);
            assertThat(format.extension()).isEqualTo("jpg");
            assertThat(format.mimeType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("JPEG 확장자면 JPEG 포맷을 반환한다")
        void shouldReturnJpegFromJpegExtension() {
            // given
            String originalExtension = "jpeg";

            // when
            ImageFormat format = ImageFormat.fromOriginal(originalExtension);

            // then
            assertThat(format.type()).isEqualTo(ImageFormatType.JPEG);
            assertThat(format.extension()).isEqualTo("jpeg");
            assertThat(format.mimeType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("WEBP 확장자면 WEBP 포맷을 반환한다")
        void shouldReturnWebpFromWebpExtension() {
            // given
            String originalExtension = "webp";

            // when
            ImageFormat format = ImageFormat.fromOriginal(originalExtension);

            // then
            assertThat(format.type()).isEqualTo(ImageFormatType.WEBP);
            assertThat(format.extension()).isEqualTo("webp");
            assertThat(format.mimeType()).isEqualTo("image/webp");
        }

        @Test
        @DisplayName("GIF 확장자면 JPEG 포맷을 반환한다 (기본값)")
        void shouldReturnJpegFromGifExtension() {
            // given
            String originalExtension = "gif";

            // when
            ImageFormat format = ImageFormat.fromOriginal(originalExtension);

            // then
            assertThat(format.type()).isEqualTo(ImageFormatType.JPEG);
        }

        @Test
        @DisplayName("대소문자 구분 없이 확장자를 인식한다")
        void shouldRecognizeExtensionCaseInsensitively() {
            // given
            String upperCaseExtension = "PNG";

            // when
            ImageFormat format = ImageFormat.fromOriginal(upperCaseExtension);

            // then
            assertThat(format.type()).isEqualTo(ImageFormatType.PNG);
        }
    }

    @Nested
    @DisplayName("표준 상수 테스트")
    class StandardConstantsTest {

        @Test
        @DisplayName("WEBP 상수가 정의되어 있다")
        void shouldHaveWebpConstant() {
            // when
            ImageFormat webp = ImageFormat.WEBP;

            // then
            assertThat(webp.type()).isEqualTo(ImageFormatType.WEBP);
            assertThat(webp.extension()).isEqualTo("webp");
            assertThat(webp.mimeType()).isEqualTo("image/webp");
        }

        @Test
        @DisplayName("JPEG 상수가 정의되어 있다")
        void shouldHaveJpegConstant() {
            // when
            ImageFormat jpeg = ImageFormat.JPEG;

            // then
            assertThat(jpeg.type()).isEqualTo(ImageFormatType.JPEG);
            assertThat(jpeg.extension()).isEqualTo("jpg");
            assertThat(jpeg.mimeType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("PNG 상수가 정의되어 있다")
        void shouldHavePngConstant() {
            // when
            ImageFormat png = ImageFormat.PNG;

            // then
            assertThat(png.type()).isEqualTo(ImageFormatType.PNG);
            assertThat(png.extension()).isEqualTo("png");
            assertThat(png.mimeType()).isEqualTo("image/png");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 데이터를 가진 ImageFormat은 동등하다")
        void equals_WithSameData_ShouldBeEqual() {
            // given
            ImageFormat format1 = ImageFormat.of(ImageFormatType.WEBP, "webp", "image/webp");
            ImageFormat format2 = ImageFormat.of(ImageFormatType.WEBP, "webp", "image/webp");

            // when & then
            assertThat(format1).isEqualTo(format2);
            assertThat(format1.hashCode()).isEqualTo(format2.hashCode());
        }

        @Test
        @DisplayName("다른 타입을 가진 ImageFormat은 동등하지 않다")
        void equals_WithDifferentType_ShouldNotBeEqual() {
            // given
            ImageFormat format1 = ImageFormat.of(ImageFormatType.WEBP, "webp", "image/webp");
            ImageFormat format2 = ImageFormat.of(ImageFormatType.PNG, "png", "image/png");

            // when & then
            assertThat(format1).isNotEqualTo(format2);
        }
    }
}
