package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("WebhookUrl 단위 테스트")
class WebhookUrlTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 HTTP URL로 생성할 수 있다")
        void of_WithValidHttpUrl_ShouldCreate() {
            // given
            String url = "http://example.com/webhook";

            // when
            WebhookUrl webhookUrl = WebhookUrl.of(url);

            // then
            assertThat(webhookUrl.value()).isEqualTo(url);
        }

        @Test
        @DisplayName("유효한 HTTPS URL로 생성할 수 있다")
        void of_WithValidHttpsUrl_ShouldCreate() {
            // given
            String url = "https://example.com/callback";

            // when
            WebhookUrl webhookUrl = WebhookUrl.of(url);

            // then
            assertThat(webhookUrl.value()).isEqualTo(url);
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "https://example.com/api/webhook",
                    "https://api.example.com/callbacks/download",
                    "http://internal-service.local/notify",
                    "https://example.com/webhook?token=abc123"
                })
        @DisplayName("다양한 형태의 Webhook URL을 허용한다")
        void of_WithVariousWebhookUrls_ShouldCreate(String url) {
            // when
            WebhookUrl webhookUrl = WebhookUrl.of(url);

            // then
            assertThat(webhookUrl.value()).isEqualTo(url);
        }

        @Test
        @DisplayName("null 값으로 생성 시 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> WebhookUrl.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("WebhookUrl");
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 예외가 발생한다")
        void of_WithBlank_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> WebhookUrl.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비어");

            assertThatThrownBy(() -> WebhookUrl.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비어");
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "ftp://example.com/webhook",
                    "file:///local/webhook",
                    "invalid-url",
                    "example.com/webhook"
                })
        @DisplayName("HTTP/HTTPS가 아닌 URL로 생성 시 예외가 발생한다")
        void of_WithInvalidProtocol_ShouldThrowException(String url) {
            // given & when & then
            assertThatThrownBy(() -> WebhookUrl.of(url))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("http://")
                    .hasMessageContaining("https://");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 URL을 가진 WebhookUrl은 동등하다")
        void equals_WithSameUrl_ShouldBeEqual() {
            // given
            String url = "https://example.com/webhook";
            WebhookUrl url1 = WebhookUrl.of(url);
            WebhookUrl url2 = WebhookUrl.of(url);

            // when & then
            assertThat(url1).isEqualTo(url2);
            assertThat(url1.hashCode()).isEqualTo(url2.hashCode());
        }

        @Test
        @DisplayName("다른 URL을 가진 WebhookUrl은 동등하지 않다")
        void equals_WithDifferentUrl_ShouldNotBeEqual() {
            // given
            WebhookUrl url1 = WebhookUrl.of("https://example.com/webhook1");
            WebhookUrl url2 = WebhookUrl.of("https://example.com/webhook2");

            // when & then
            assertThat(url1).isNotEqualTo(url2);
        }
    }
}
