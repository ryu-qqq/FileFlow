package com.ryuqq.fileflow.domain.image.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ImageFormat 테스트")
class ImageFormatTest {

    @Test
    @DisplayName("MIME 타입으로 ImageFormat을 찾을 수 있다")
    void fromMimeType_Success() {
        // given & when
        ImageFormat jpeg = ImageFormat.fromMimeType("image/jpeg");
        ImageFormat png = ImageFormat.fromMimeType("image/png");
        ImageFormat gif = ImageFormat.fromMimeType("image/gif");
        ImageFormat webp = ImageFormat.fromMimeType("image/webp");

        // then
        assertThat(jpeg).isEqualTo(ImageFormat.JPEG);
        assertThat(png).isEqualTo(ImageFormat.PNG);
        assertThat(gif).isEqualTo(ImageFormat.GIF);
        assertThat(webp).isEqualTo(ImageFormat.WEBP);
    }

    @Test
    @DisplayName("대소문자를 구분하지 않고 MIME 타입으로 찾을 수 있다")
    void fromMimeType_CaseInsensitive() {
        // given & when
        ImageFormat jpeg = ImageFormat.fromMimeType("IMAGE/JPEG");
        ImageFormat png = ImageFormat.fromMimeType("Image/Png");

        // then
        assertThat(jpeg).isEqualTo(ImageFormat.JPEG);
        assertThat(png).isEqualTo(ImageFormat.PNG);
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/bmp", "application/pdf"})
    @DisplayName("지원하지 않는 MIME 타입은 예외가 발생한다")
    void fromMimeType_UnsupportedFormat_ThrowsException(String mimeType) {
        // when & then
        assertThatThrownBy(() -> ImageFormat.fromMimeType(mimeType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported image format");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    @DisplayName("빈 MIME 타입은 예외가 발생한다")
    void fromMimeType_EmptyMimeType_ThrowsException(String mimeType) {
        // when & then
        assertThatThrownBy(() -> ImageFormat.fromMimeType(mimeType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MimeType cannot be null");
    }

    @Test
    @DisplayName("null MIME 타입은 예외가 발생한다")
    void fromMimeType_Null_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> ImageFormat.fromMimeType(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MimeType cannot be null");
    }

    @Test
    @DisplayName("파일 확장자로 ImageFormat을 찾을 수 있다")
    void fromExtension_Success() {
        // given & when
        ImageFormat jpeg = ImageFormat.fromExtension("jpg");
        ImageFormat png = ImageFormat.fromExtension("png");
        ImageFormat gif = ImageFormat.fromExtension("gif");
        ImageFormat webp = ImageFormat.fromExtension("webp");

        // then
        assertThat(jpeg).isEqualTo(ImageFormat.JPEG);
        assertThat(png).isEqualTo(ImageFormat.PNG);
        assertThat(gif).isEqualTo(ImageFormat.GIF);
        assertThat(webp).isEqualTo(ImageFormat.WEBP);
    }

    @Test
    @DisplayName("앞에 점이 있는 확장자도 처리할 수 있다")
    void fromExtension_WithDot() {
        // given & when
        ImageFormat jpeg = ImageFormat.fromExtension(".jpg");
        ImageFormat png = ImageFormat.fromExtension(".png");

        // then
        assertThat(jpeg).isEqualTo(ImageFormat.JPEG);
        assertThat(png).isEqualTo(ImageFormat.PNG);
    }

    @Test
    @DisplayName("WebP로 변환 가능한지 확인할 수 있다")
    void isConvertibleToWebP() {
        // when & then
        assertThat(ImageFormat.JPEG.isConvertibleToWebP()).isTrue();
        assertThat(ImageFormat.PNG.isConvertibleToWebP()).isTrue();
        assertThat(ImageFormat.GIF.isConvertibleToWebP()).isFalse();
        assertThat(ImageFormat.WEBP.isConvertibleToWebP()).isFalse();
    }

    @Test
    @DisplayName("WebP 포맷인지 확인할 수 있다")
    void isWebP() {
        // when & then
        assertThat(ImageFormat.JPEG.isWebP()).isFalse();
        assertThat(ImageFormat.PNG.isWebP()).isFalse();
        assertThat(ImageFormat.GIF.isWebP()).isFalse();
        assertThat(ImageFormat.WEBP.isWebP()).isTrue();
    }

    @Test
    @DisplayName("압축 가능한 포맷인지 확인할 수 있다")
    void isCompressible() {
        // when & then
        assertThat(ImageFormat.JPEG.isCompressible()).isTrue();
        assertThat(ImageFormat.PNG.isCompressible()).isTrue();
        assertThat(ImageFormat.GIF.isCompressible()).isFalse();
        assertThat(ImageFormat.WEBP.isCompressible()).isTrue();
    }

    @Test
    @DisplayName("투명도를 지원하는지 확인할 수 있다")
    void supportsTransparency() {
        // when & then
        assertThat(ImageFormat.JPEG.supportsTransparency()).isFalse();
        assertThat(ImageFormat.PNG.supportsTransparency()).isTrue();
        assertThat(ImageFormat.GIF.supportsTransparency()).isTrue();
        assertThat(ImageFormat.WEBP.supportsTransparency()).isTrue();
    }

    @Test
    @DisplayName("MIME 타입과 확장자를 반환할 수 있다")
    void getMimeTypeAndExtension() {
        // given
        ImageFormat jpeg = ImageFormat.JPEG;

        // when & then
        assertThat(jpeg.getMimeType()).isEqualTo("image/jpeg");
        assertThat(jpeg.getExtension()).isEqualTo("jpg");
    }
}
