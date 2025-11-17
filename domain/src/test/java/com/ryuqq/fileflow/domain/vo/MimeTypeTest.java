package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MimeType Value Object 테스트
 */
class MimeTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf",
            "text/plain",
            "text/html",
            "application/json",
            "application/xml",
            "video/mp4",
            "audio/mpeg"
    })
    @DisplayName("허용된 MIME 타입으로 MimeType을 생성해야 한다")
    void shouldCreateValidMimeType(String validMimeType) {
        // when
        MimeType mimeType = MimeType.of(validMimeType);

        // then
        assertThat(mimeType).isNotNull();
        assertThat(mimeType.getValue()).isEqualTo(validMimeType);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("null 또는 빈 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNullOrEmpty(String invalidMimeType) {
        // when & then
        assertThatThrownBy(() -> MimeType.of(invalidMimeType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MIME 타입은 null이거나 빈 값일 수 없습니다");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "application/x-sh",
            "application/x-executable",
            "application/x-msdownload",
            "text/x-script.python",
            "invalid/mime-type"
    })
    @DisplayName("허용되지 않은 MIME 타입은 예외가 발생해야 한다")
    void shouldThrowExceptionWhenMimeTypeIsNotAllowed(String notAllowedMimeType) {
        // when & then
        assertThatThrownBy(() -> MimeType.of(notAllowedMimeType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않은 MIME 타입입니다");
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환해야 한다")
    void shouldReturnSameValueFromGetValue() {
        // given
        String expectedValue = "image/jpeg";
        MimeType mimeType = MimeType.of(expectedValue);

        // when
        String actualValue = mimeType.getValue();

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("같은 값을 가진 MimeType은 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String value = "application/pdf";
        MimeType mimeType1 = MimeType.of(value);
        MimeType mimeType2 = MimeType.of(value);

        // when & then
        assertThat(mimeType1).isEqualTo(mimeType2);
    }

    @Test
    @DisplayName("같은 값을 가진 MimeType은 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenValueIsSame() {
        // given
        String value = "image/png";
        MimeType mimeType1 = MimeType.of(value);
        MimeType mimeType2 = MimeType.of(value);

        // when & then
        assertThat(mimeType1.hashCode()).isEqualTo(mimeType2.hashCode());
    }

    @Test
    @DisplayName("isImage()는 이미지 MIME 타입일 때 true를 반환해야 한다")
    void shouldReturnTrueForIsImageWhenMimeTypeIsImage() {
        // given
        MimeType jpegImage = MimeType.of("image/jpeg");
        MimeType pngImage = MimeType.of("image/png");

        // when & then
        assertThat(jpegImage.isImage()).isTrue();
        assertThat(pngImage.isImage()).isTrue();
    }

    @Test
    @DisplayName("isImage()는 이미지가 아닌 MIME 타입일 때 false를 반환해야 한다")
    void shouldReturnFalseForIsImageWhenMimeTypeIsNotImage() {
        // given
        MimeType pdf = MimeType.of("application/pdf");
        MimeType text = MimeType.of("text/plain");

        // when & then
        assertThat(pdf.isImage()).isFalse();
        assertThat(text.isImage()).isFalse();
    }

    @Test
    @DisplayName("isPdf()는 PDF MIME 타입일 때 true를 반환해야 한다")
    void shouldReturnTrueForIsPdfWhenMimeTypeIsPdf() {
        // given
        MimeType pdf = MimeType.of("application/pdf");

        // when & then
        assertThat(pdf.isPdf()).isTrue();
    }

    @Test
    @DisplayName("isPdf()는 PDF가 아닌 MIME 타입일 때 false를 반환해야 한다")
    void shouldReturnFalseForIsPdfWhenMimeTypeIsNotPdf() {
        // given
        MimeType image = MimeType.of("image/jpeg");

        // when & then
        assertThat(image.isPdf()).isFalse();
    }
}
