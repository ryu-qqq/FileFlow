package com.ryuqq.fileflow.adapter.in.rest.download.dto.command;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * RequestExternalDownloadApiRequest 단위 테스트.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("RequestExternalDownloadApiRequest 단위 테스트")
class RequestExternalDownloadApiRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("sourceUrl과 webhookUrl로 요청을 생성할 수 있다")
        void create_WithSourceUrlAndWebhookUrl_ShouldSucceed() {
            // given
            String sourceUrl = "https://example.com/image.jpg";
            String webhookUrl = "https://myservice.com/webhook";

            // when
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest(sourceUrl, webhookUrl);

            // then
            assertThat(request.sourceUrl()).isEqualTo(sourceUrl);
            assertThat(request.webhookUrl()).isEqualTo(webhookUrl);
        }

        @Test
        @DisplayName("webhookUrl 없이 요청을 생성할 수 있다")
        void create_WithoutWebhookUrl_ShouldSucceed() {
            // given
            String sourceUrl = "https://example.com/image.jpg";

            // when
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest(sourceUrl, null);

            // then
            assertThat(request.sourceUrl()).isEqualTo(sourceUrl);
            assertThat(request.webhookUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("유효성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 요청은 검증을 통과한다")
        void validate_ValidRequest_ShouldPass() {
            // given
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest(
                            "https://example.com/image.jpg", "https://webhook.com/notify");

            // when
            Set<ConstraintViolation<RequestExternalDownloadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("webhookUrl 없이도 유효한 요청이다")
        void validate_WithoutWebhookUrl_ShouldPass() {
            // given
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest("https://example.com/image.jpg", null);

            // when
            Set<ConstraintViolation<RequestExternalDownloadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("sourceUrl이 null이면 검증 실패한다")
        void validate_NullSourceUrl_ShouldFail() {
            // given
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest(null, null);

            // when
            Set<ConstraintViolation<RequestExternalDownloadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("필수");
        }

        @Test
        @DisplayName("sourceUrl이 빈 문자열이면 검증 실패한다")
        void validate_EmptySourceUrl_ShouldFail() {
            // given
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest("", null);

            // when
            Set<ConstraintViolation<RequestExternalDownloadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("sourceUrl이 공백만 있으면 검증 실패한다")
        void validate_BlankSourceUrl_ShouldFail() {
            // given
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest("   ", null);

            // when
            Set<ConstraintViolation<RequestExternalDownloadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("sourceUrl이 2048자 이하면 검증을 통과한다")
        void validate_SourceUrlAtMaxLength_ShouldPass() {
            // given
            String baseUrl = "https://example.com/";
            String longPath = "a".repeat(2048 - baseUrl.length());
            String sourceUrl = baseUrl + longPath;
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest(sourceUrl, null);

            // when
            Set<ConstraintViolation<RequestExternalDownloadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("sourceUrl이 2048자를 초과하면 검증 실패한다")
        void validate_SourceUrlExceedsMaxLength_ShouldFail() {
            // given
            String longUrl = "https://example.com/" + "a".repeat(2049);
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest(longUrl, null);

            // when
            Set<ConstraintViolation<RequestExternalDownloadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("2048자");
        }

        @Test
        @DisplayName("webhookUrl이 2048자 이하면 검증을 통과한다")
        void validate_WebhookUrlAtMaxLength_ShouldPass() {
            // given
            String baseUrl = "https://webhook.com/";
            String longPath = "b".repeat(2048 - baseUrl.length());
            String webhookUrl = baseUrl + longPath;
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest(
                            "https://example.com/image.jpg", webhookUrl);

            // when
            Set<ConstraintViolation<RequestExternalDownloadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("webhookUrl이 2048자를 초과하면 검증 실패한다")
        void validate_WebhookUrlExceedsMaxLength_ShouldFail() {
            // given
            String longWebhookUrl = "https://webhook.com/" + "b".repeat(2049);
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest(
                            "https://example.com/image.jpg", longWebhookUrl);

            // when
            Set<ConstraintViolation<RequestExternalDownloadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("2048자");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 요청은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            RequestExternalDownloadApiRequest request1 =
                    new RequestExternalDownloadApiRequest(
                            "https://example.com/image.jpg", "https://webhook.com");
            RequestExternalDownloadApiRequest request2 =
                    new RequestExternalDownloadApiRequest(
                            "https://example.com/image.jpg", "https://webhook.com");

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 sourceUrl을 가진 요청은 동등하지 않다")
        void equals_WithDifferentSourceUrl_ShouldNotBeEqual() {
            // given
            RequestExternalDownloadApiRequest request1 =
                    new RequestExternalDownloadApiRequest("https://example.com/image1.jpg", null);
            RequestExternalDownloadApiRequest request2 =
                    new RequestExternalDownloadApiRequest("https://example.com/image2.jpg", null);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("다른 webhookUrl을 가진 요청은 동등하지 않다")
        void equals_WithDifferentWebhookUrl_ShouldNotBeEqual() {
            // given
            RequestExternalDownloadApiRequest request1 =
                    new RequestExternalDownloadApiRequest(
                            "https://example.com/image.jpg", "https://webhook1.com");
            RequestExternalDownloadApiRequest request2 =
                    new RequestExternalDownloadApiRequest(
                            "https://example.com/image.jpg", "https://webhook2.com");

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("null webhookUrl 요청들은 동등하다")
        void equals_WithNullWebhookUrl_ShouldBeEqual() {
            // given
            RequestExternalDownloadApiRequest request1 =
                    new RequestExternalDownloadApiRequest("https://example.com/image.jpg", null);
            RequestExternalDownloadApiRequest request2 =
                    new RequestExternalDownloadApiRequest("https://example.com/image.jpg", null);

            // when & then
            assertThat(request1).isEqualTo(request2);
        }
    }
}
