package com.ryuqq.fileflow.domain.download.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadWebhookTriggeredEvent 단위 테스트")
class ExternalDownloadWebhookTriggeredEventTest {

    private static final ExternalDownloadId DOWNLOAD_ID =
            ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");
    private static final WebhookUrl WEBHOOK_URL =
            WebhookUrl.of("https://callback.example.com/webhook");
    private static final FileAssetId FILE_ASSET_ID =
            FileAssetId.of("00000000-0000-0000-0000-000000000002");
    private static final String ERROR_MESSAGE = "Download failed";
    private static final Instant OCCURRED_AT = Instant.parse("2025-11-26T12:00:00Z");

    @Nested
    @DisplayName("완료 이벤트 생성 테스트")
    class ForCompletedTest {

        @Test
        @DisplayName("forCompleted()로 완료 이벤트를 생성할 수 있다")
        void forCompleted_ShouldCreateCompletedEvent() {
            // when
            ExternalDownloadWebhookTriggeredEvent event =
                    ExternalDownloadWebhookTriggeredEvent.forCompleted(
                            DOWNLOAD_ID, WEBHOOK_URL, FILE_ASSET_ID, OCCURRED_AT);

            // then
            assertThat(event.downloadId()).isEqualTo(DOWNLOAD_ID);
            assertThat(event.webhookUrl()).isEqualTo(WEBHOOK_URL);
            assertThat(event.status()).isEqualTo(ExternalDownloadStatus.COMPLETED);
            assertThat(event.fileAssetId()).isEqualTo(FILE_ASSET_ID);
            assertThat(event.errorMessage()).isNull();
            assertThat(event.occurredAt()).isEqualTo(OCCURRED_AT);
            assertThat(event.isCompleted()).isTrue();
            assertThat(event.isFailed()).isFalse();
        }

        @Test
        @DisplayName("forCompleted()에 downloadId가 null이면 예외가 발생한다")
        void forCompleted_WithNullDownloadId_ShouldThrowException() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    ExternalDownloadWebhookTriggeredEvent.forCompleted(
                                            null, WEBHOOK_URL, FILE_ASSET_ID, OCCURRED_AT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("downloadId");
        }

        @Test
        @DisplayName("forCompleted()에 webhookUrl이 null이면 예외가 발생한다")
        void forCompleted_WithNullWebhookUrl_ShouldThrowException() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    ExternalDownloadWebhookTriggeredEvent.forCompleted(
                                            DOWNLOAD_ID, null, FILE_ASSET_ID, OCCURRED_AT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("webhookUrl");
        }

        @Test
        @DisplayName("forCompleted()에 occurredAt이 null이면 예외가 발생한다")
        void forCompleted_WithNullOccurredAt_ShouldThrowException() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    ExternalDownloadWebhookTriggeredEvent.forCompleted(
                                            DOWNLOAD_ID, WEBHOOK_URL, FILE_ASSET_ID, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("occurredAt");
        }
    }

    @Nested
    @DisplayName("실패 이벤트 생성 테스트")
    class ForFailedTest {

        @Test
        @DisplayName("forFailed()로 실패 이벤트를 생성할 수 있다")
        void forFailed_ShouldCreateFailedEvent() {
            // when
            ExternalDownloadWebhookTriggeredEvent event =
                    ExternalDownloadWebhookTriggeredEvent.forFailed(
                            DOWNLOAD_ID, WEBHOOK_URL, ERROR_MESSAGE, OCCURRED_AT);

            // then
            assertThat(event.downloadId()).isEqualTo(DOWNLOAD_ID);
            assertThat(event.webhookUrl()).isEqualTo(WEBHOOK_URL);
            assertThat(event.status()).isEqualTo(ExternalDownloadStatus.FAILED);
            assertThat(event.fileAssetId()).isNull();
            assertThat(event.errorMessage()).isEqualTo(ERROR_MESSAGE);
            assertThat(event.occurredAt()).isEqualTo(OCCURRED_AT);
            assertThat(event.isCompleted()).isFalse();
            assertThat(event.isFailed()).isTrue();
        }

        @Test
        @DisplayName("forFailed()에 downloadId가 null이면 예외가 발생한다")
        void forFailed_WithNullDownloadId_ShouldThrowException() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    ExternalDownloadWebhookTriggeredEvent.forFailed(
                                            null, WEBHOOK_URL, ERROR_MESSAGE, OCCURRED_AT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("downloadId");
        }

        @Test
        @DisplayName("forFailed()에 webhookUrl이 null이면 예외가 발생한다")
        void forFailed_WithNullWebhookUrl_ShouldThrowException() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    ExternalDownloadWebhookTriggeredEvent.forFailed(
                                            DOWNLOAD_ID, null, ERROR_MESSAGE, OCCURRED_AT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("webhookUrl");
        }

        @Test
        @DisplayName("forFailed()에 occurredAt이 null이면 예외가 발생한다")
        void forFailed_WithNullOccurredAt_ShouldThrowException() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    ExternalDownloadWebhookTriggeredEvent.forFailed(
                                            DOWNLOAD_ID, WEBHOOK_URL, ERROR_MESSAGE, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("occurredAt");
        }
    }
}
