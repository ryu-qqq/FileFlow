package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ExternalUrl Value Object 테스트
 */
class ExternalUrlTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com/image.jpg",
            "https://cdn.example.com/files/document.pdf",
            "https://storage.googleapis.com/bucket/file.png"
    })
    @DisplayName("유효한 HTTPS URL로 ExternalUrl을 생성해야 한다")
    void shouldCreateValidExternalUrl(String validUrl) {
        // when
        ExternalUrl externalUrl = ExternalUrl.of(validUrl);

        // then
        assertThat(externalUrl).isNotNull();
        assertThat(externalUrl.value()).isEqualTo(validUrl);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com/image.jpg",
            "ftp://example.com/file.zip",
            "file:///local/path.txt"
    })
    @DisplayName("HTTPS가 아닌 URL은 예외가 발생해야 한다")
    void shouldThrowExceptionWhenNotHTTPS(String invalidUrl) {
        // when & then
        assertThatThrownBy(() -> ExternalUrl.of(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("외부 URL은 HTTPS 프로토콜만 허용됩니다");
    }

    @Test
    @DisplayName("null 또는 빈 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNullOrEmpty() {
        // when & then
        assertThatThrownBy(() -> ExternalUrl.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("외부 URL은 null이거나 빈 값일 수 없습니다");

        assertThatThrownBy(() -> ExternalUrl.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("외부 URL은 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("잘못된 URL 형식은 예외가 발생해야 한다")
    void shouldThrowExceptionWhenInvalidUrlFormat() {
        // given
        String invalidUrl = "not-a-valid-url";

        // when & then
        assertThatThrownBy(() -> ExternalUrl.of(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 URL 형식입니다");
    }

    @Test
    @DisplayName("같은 값을 가진 ExternalUrl은 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String url = "https://example.com/file.jpg";
        ExternalUrl externalUrl1 = ExternalUrl.of(url);
        ExternalUrl externalUrl2 = ExternalUrl.of(url);

        // when & then
        assertThat(externalUrl1).isEqualTo(externalUrl2);
    }
}
