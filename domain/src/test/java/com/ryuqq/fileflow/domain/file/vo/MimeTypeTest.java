package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.fileflow.domain.file.fixture.MimeTypeFixture;
import com.ryuqq.fileflow.domain.file.exception.UnsupportedFileTypeException;

import static com.ryuqq.fileflow.domain.file.fixture.MimeTypeFixture.htmlMimeType;
import static com.ryuqq.fileflow.domain.file.fixture.MimeTypeFixture.imageMimeType;
import static com.ryuqq.fileflow.domain.file.fixture.MimeTypeFixture.textHtmlValue;
import static com.ryuqq.fileflow.domain.file.fixture.MimeTypeFixture.unsupportedMimeValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MimeType VO Tests")
class MimeTypeTest {

    @Test
    @DisplayName("허용된 MIME 타입을 생성할 수 있어야 한다")
    void shouldCreateAllowedMimeTypes() {
        // when
        MimeType image = imageMimeType();
        MimeType html = htmlMimeType();

        // then
        assertThat(image.value()).isEqualTo("image/jpeg");
        assertThat(html.value()).isEqualTo(textHtmlValue());
        assertThat(image.isImage()).isTrue();
        assertThat(html.isHtml()).isTrue();
    }

    @Test
    @DisplayName("허용되지 않은 타입이면 예외가 발생해야 한다")
    void shouldThrowExceptionForUnsupportedType() {
        // expect
        assertThatThrownBy(() -> MimeType.of(unsupportedMimeValue()))
            .isInstanceOf(UnsupportedFileTypeException.class);
    }

    @Test
    @DisplayName("확장자를 정확하게 추출해야 한다")
    void shouldExtractExtensionCorrectly() {
        // when
        MimeType image = imageMimeType();
        MimeType html = htmlMimeType();

        // then
        assertThat(image.extractExtension()).isEqualTo(".jpeg");
        assertThat(html.extractExtension()).isEqualTo(".html");
    }

    @Test
    @DisplayName("이미지 여부를 판단해야 한다")
    void shouldCheckIsImage() {
        // given
        MimeType image = imageMimeType();
        MimeType html = htmlMimeType();

        // expect
        assertThat(image.isImage()).isTrue();
        assertThat(html.isImage()).isFalse();
    }

    @Test
    @DisplayName("HTML 여부를 판단해야 한다")
    void shouldCheckIsHtml() {
        // given
        MimeType image = imageMimeType();
        MimeType html = htmlMimeType();

        // expect
        assertThat(html.isHtml()).isTrue();
        assertThat(image.isHtml()).isFalse();
    }
}

