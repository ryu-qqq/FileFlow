package com.ryuqq.fileflow.adapter.in.rest.download.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * ExternalDownloadDetailApiResponse 단위 테스트.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ExternalDownloadDetailApiResponse 단위 테스트")
class ExternalDownloadDetailApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            String id = "download-123";
            String sourceUrl = "https://example.com/image.jpg";
            String status = "COMPLETED";
            String fileAssetId = "asset-456";
            String errorMessage = null;
            int retryCount = 0;
            String webhookUrl = "https://myservice.com/webhook";
            Instant createdAt = Instant.parse("2025-12-10T10:00:00Z");
            Instant updatedAt = Instant.parse("2025-12-10T10:05:00Z");

            // when
            ExternalDownloadDetailApiResponse response =
                    new ExternalDownloadDetailApiResponse(
                            id,
                            sourceUrl,
                            status,
                            fileAssetId,
                            errorMessage,
                            retryCount,
                            webhookUrl,
                            createdAt,
                            updatedAt);

            // then
            assertThat(response.id()).isEqualTo(id);
            assertThat(response.sourceUrl()).isEqualTo(sourceUrl);
            assertThat(response.status()).isEqualTo(status);
            assertThat(response.fileAssetId()).isEqualTo(fileAssetId);
            assertThat(response.errorMessage()).isNull();
            assertThat(response.retryCount()).isEqualTo(0);
            assertThat(response.webhookUrl()).isEqualTo(webhookUrl);
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("실패 상태 응답을 생성할 수 있다")
        void create_WithFailedStatus_ShouldSucceed() {
            // given
            Instant now = Instant.now();

            // when
            ExternalDownloadDetailApiResponse response =
                    new ExternalDownloadDetailApiResponse(
                            "download-123",
                            "https://example.com/image.jpg",
                            "FAILED",
                            null,
                            "Connection timeout",
                            3,
                            null,
                            now,
                            now);

            // then
            assertThat(response.status()).isEqualTo("FAILED");
            assertThat(response.fileAssetId()).isNull();
            assertThat(response.errorMessage()).isEqualTo("Connection timeout");
            assertThat(response.retryCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("상태별 테스트")
    class StatusTest {

        @ParameterizedTest
        @ValueSource(strings = {"PENDING", "PROCESSING", "COMPLETED", "FAILED"})
        @DisplayName("모든 상태 값으로 응답을 생성할 수 있다")
        void create_WithAllStatuses_ShouldSucceed(String status) {
            // when
            ExternalDownloadDetailApiResponse response = createResponseWithStatus(status);

            // then
            assertThat(response.status()).isEqualTo(status);
        }

        private ExternalDownloadDetailApiResponse createResponseWithStatus(String status) {
            Instant now = Instant.now();
            return new ExternalDownloadDetailApiResponse(
                    "download-id",
                    "https://example.com/image.jpg",
                    status,
                    "COMPLETED".equals(status) ? "asset-123" : null,
                    "FAILED".equals(status) ? "Error occurred" : null,
                    0,
                    null,
                    now,
                    now);
        }
    }

    @Nested
    @DisplayName("Nullable 필드 테스트")
    class NullableFieldsTest {

        @Test
        @DisplayName("fileAssetId가 null이어도 생성할 수 있다")
        void create_WithNullFileAssetId_ShouldSucceed() {
            // when
            ExternalDownloadDetailApiResponse response = createBasicResponse(null, null, null);

            // then
            assertThat(response.fileAssetId()).isNull();
        }

        @Test
        @DisplayName("errorMessage가 null이어도 생성할 수 있다")
        void create_WithNullErrorMessage_ShouldSucceed() {
            // when
            ExternalDownloadDetailApiResponse response =
                    createBasicResponse("asset-123", null, null);

            // then
            assertThat(response.errorMessage()).isNull();
        }

        @Test
        @DisplayName("webhookUrl이 null이어도 생성할 수 있다")
        void create_WithNullWebhookUrl_ShouldSucceed() {
            // when
            ExternalDownloadDetailApiResponse response =
                    createBasicResponse("asset-123", null, null);

            // then
            assertThat(response.webhookUrl()).isNull();
        }

        private ExternalDownloadDetailApiResponse createBasicResponse(
                String fileAssetId, String errorMessage, String webhookUrl) {
            Instant now = Instant.now();
            return new ExternalDownloadDetailApiResponse(
                    "download-123",
                    "https://example.com/image.jpg",
                    "COMPLETED",
                    fileAssetId,
                    errorMessage,
                    0,
                    webhookUrl,
                    now,
                    now);
        }
    }

    @Nested
    @DisplayName("재시도 횟수 테스트")
    class RetryCountTest {

        @Test
        @DisplayName("재시도 횟수가 0인 응답을 생성할 수 있다")
        void create_WithZeroRetryCount_ShouldSucceed() {
            // when
            ExternalDownloadDetailApiResponse response = createResponseWithRetryCount(0);

            // then
            assertThat(response.retryCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("재시도 횟수가 여러 번인 응답을 생성할 수 있다")
        void create_WithMultipleRetryCount_ShouldSucceed() {
            // when
            ExternalDownloadDetailApiResponse response = createResponseWithRetryCount(3);

            // then
            assertThat(response.retryCount()).isEqualTo(3);
        }

        private ExternalDownloadDetailApiResponse createResponseWithRetryCount(int retryCount) {
            Instant now = Instant.now();
            return new ExternalDownloadDetailApiResponse(
                    "download-123",
                    "https://example.com/image.jpg",
                    "PROCESSING",
                    null,
                    null,
                    retryCount,
                    null,
                    now,
                    now);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Instant createdAt = Instant.parse("2025-12-10T10:00:00Z");
            Instant updatedAt = Instant.parse("2025-12-10T10:05:00Z");

            ExternalDownloadDetailApiResponse response1 =
                    new ExternalDownloadDetailApiResponse(
                            "download-123",
                            "https://example.com/image.jpg",
                            "COMPLETED",
                            "asset-456",
                            null,
                            0,
                            "https://webhook.com",
                            createdAt,
                            updatedAt);
            ExternalDownloadDetailApiResponse response2 =
                    new ExternalDownloadDetailApiResponse(
                            "download-123",
                            "https://example.com/image.jpg",
                            "COMPLETED",
                            "asset-456",
                            null,
                            0,
                            "https://webhook.com",
                            createdAt,
                            updatedAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 id를 가진 응답은 동등하지 않다")
        void equals_WithDifferentId_ShouldNotBeEqual() {
            // given
            Instant now = Instant.now();
            ExternalDownloadDetailApiResponse response1 =
                    new ExternalDownloadDetailApiResponse(
                            "download-1",
                            "https://example.com/image.jpg",
                            "PENDING",
                            null,
                            null,
                            0,
                            null,
                            now,
                            now);
            ExternalDownloadDetailApiResponse response2 =
                    new ExternalDownloadDetailApiResponse(
                            "download-2",
                            "https://example.com/image.jpg",
                            "PENDING",
                            null,
                            null,
                            0,
                            null,
                            now,
                            now);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 status를 가진 응답은 동등하지 않다")
        void equals_WithDifferentStatus_ShouldNotBeEqual() {
            // given
            Instant now = Instant.now();
            ExternalDownloadDetailApiResponse response1 =
                    new ExternalDownloadDetailApiResponse(
                            "download-123",
                            "https://example.com/image.jpg",
                            "PENDING",
                            null,
                            null,
                            0,
                            null,
                            now,
                            now);
            ExternalDownloadDetailApiResponse response2 =
                    new ExternalDownloadDetailApiResponse(
                            "download-123",
                            "https://example.com/image.jpg",
                            "COMPLETED",
                            "asset-456",
                            null,
                            0,
                            null,
                            now,
                            now);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
