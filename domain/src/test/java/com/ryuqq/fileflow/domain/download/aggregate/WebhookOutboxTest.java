package com.ryuqq.fileflow.domain.download.aggregate;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxStatus;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("WebhookOutbox 단위 테스트")
class WebhookOutboxTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-11-26T12:00:00Z"), ZoneId.of("UTC"));

    private static final ExternalDownloadId DOWNLOAD_ID =
            ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");
    private static final WebhookUrl WEBHOOK_URL =
            WebhookUrl.of("https://callback.example.com/webhook");
    private static final FileAssetId FILE_ASSET_ID =
            FileAssetId.of("00000000-0000-0000-0000-000000000002");
    private static final String ERROR_MESSAGE = "Download failed due to network error";

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 COMPLETED 상태의 WebhookOutbox를 생성할 수 있다")
        void forNew_WithCompleted_ShouldCreatePendingOutbox() {
            // given & when
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);

            // then
            assertThat(outbox.getId()).isNotNull();
            assertThat(outbox.getExternalDownloadId()).isEqualTo(DOWNLOAD_ID);
            assertThat(outbox.getWebhookUrl()).isEqualTo(WEBHOOK_URL);
            assertThat(outbox.getStatus()).isEqualTo(WebhookOutboxStatus.PENDING);
            assertThat(outbox.getDownloadStatus()).isEqualTo(ExternalDownloadStatus.COMPLETED);
            assertThat(outbox.getFileAssetId()).isEqualTo(FILE_ASSET_ID);
            assertThat(outbox.getErrorMessage()).isNull();
            assertThat(outbox.getRetryCount()).isZero();
            assertThat(outbox.getLastErrorMessage()).isNull();
            assertThat(outbox.getSentAt()).isNull();
            assertThat(outbox.getCreatedAt()).isNotNull();
            assertThat(outbox.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("forNew()로 FAILED 상태의 WebhookOutbox를 생성할 수 있다")
        void forNew_WithFailed_ShouldCreatePendingOutbox() {
            // given & when
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.FAILED,
                            null,
                            ERROR_MESSAGE,
                            FIXED_CLOCK);

            // then
            assertThat(outbox.getStatus()).isEqualTo(WebhookOutboxStatus.PENDING);
            assertThat(outbox.getDownloadStatus()).isEqualTo(ExternalDownloadStatus.FAILED);
            assertThat(outbox.getFileAssetId()).isNull();
            assertThat(outbox.getErrorMessage()).isEqualTo(ERROR_MESSAGE);
        }

        @Test
        @DisplayName("externalDownloadId가 null이면 예외가 발생한다")
        void forNew_WithNullExternalDownloadId_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    WebhookOutbox.forNew(
                                            null,
                                            WEBHOOK_URL,
                                            ExternalDownloadStatus.COMPLETED,
                                            FILE_ASSET_ID,
                                            null,
                                            FIXED_CLOCK))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("externalDownloadId");
        }

        @Test
        @DisplayName("webhookUrl이 null이면 예외가 발생한다")
        void forNew_WithNullWebhookUrl_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    WebhookOutbox.forNew(
                                            DOWNLOAD_ID,
                                            null,
                                            ExternalDownloadStatus.COMPLETED,
                                            FILE_ASSET_ID,
                                            null,
                                            FIXED_CLOCK))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("webhookUrl");
        }
    }

    @Nested
    @DisplayName("발송 성공 테스트")
    class MarkAsSentTest {

        @Test
        @DisplayName("PENDING 상태에서 markAsSent()를 호출하면 SENT 상태로 변경된다")
        void markAsSent_FromPending_ShouldChangToSent() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);

            // when
            outbox.markAsSent(FIXED_CLOCK);

            // then
            assertThat(outbox.getStatus()).isEqualTo(WebhookOutboxStatus.SENT);
            assertThat(outbox.getSentAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 SENT 상태에서 markAsSent()를 호출하면 예외가 발생한다")
        void markAsSent_FromSent_ShouldThrowException() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);
            outbox.markAsSent(FIXED_CLOCK);

            // when & then
            assertThatThrownBy(() -> outbox.markAsSent(FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("종료 상태");
        }

        @Test
        @DisplayName("FAILED 상태에서 markAsSent()를 호출하면 예외가 발생한다")
        void markAsSent_FromFailed_ShouldThrowException() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);
            outbox.markAsFailed("Final error", FIXED_CLOCK);

            // when & then
            assertThatThrownBy(() -> outbox.markAsSent(FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("종료 상태");
        }
    }

    @Nested
    @DisplayName("재시도 테스트")
    class IncrementRetryTest {

        @Test
        @DisplayName("PENDING 상태에서 incrementRetry()를 호출하면 retryCount가 증가한다")
        void incrementRetry_FromPending_ShouldIncreaseRetryCount() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);
            String errorMessage = "Connection timeout";

            // when
            outbox.incrementRetry(errorMessage, FIXED_CLOCK);

            // then
            assertThat(outbox.getRetryCount()).isEqualTo(1);
            assertThat(outbox.getLastErrorMessage()).isEqualTo(errorMessage);
            assertThat(outbox.getStatus()).isEqualTo(WebhookOutboxStatus.PENDING);
        }

        @Test
        @DisplayName("최대 재시도 횟수(2)까지 incrementRetry()를 호출할 수 있다")
        void incrementRetry_UpToMaxRetry_ShouldSucceed() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);

            // when
            outbox.incrementRetry("Error 1", FIXED_CLOCK);
            outbox.incrementRetry("Error 2", FIXED_CLOCK);

            // then
            assertThat(outbox.getRetryCount()).isEqualTo(2);
            assertThat(outbox.getLastErrorMessage()).isEqualTo("Error 2");
            assertThat(outbox.hasReachedMaxRetry()).isTrue();
        }

        @Test
        @DisplayName("최대 재시도 횟수 초과 시 incrementRetry()를 호출하면 예외가 발생한다")
        void incrementRetry_ExceedMaxRetry_ShouldThrowException() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);
            outbox.incrementRetry("Error 1", FIXED_CLOCK);
            outbox.incrementRetry("Error 2", FIXED_CLOCK);

            // when & then
            assertThatThrownBy(() -> outbox.incrementRetry("Error 3", FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("최대 재시도");
        }

        @Test
        @DisplayName("SENT 상태에서 incrementRetry()를 호출하면 예외가 발생한다")
        void incrementRetry_FromSent_ShouldThrowException() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);
            outbox.markAsSent(FIXED_CLOCK);

            // when & then
            assertThatThrownBy(() -> outbox.incrementRetry("Error", FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("종료 상태");
        }
    }

    @Nested
    @DisplayName("최종 실패 테스트")
    class MarkAsFailedTest {

        @Test
        @DisplayName("PENDING 상태에서 markAsFailed()를 호출하면 FAILED 상태로 변경된다")
        void markAsFailed_FromPending_ShouldChangeToFailed() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);
            String errorMessage = "Max retry exceeded";

            // when
            outbox.markAsFailed(errorMessage, FIXED_CLOCK);

            // then
            assertThat(outbox.getStatus()).isEqualTo(WebhookOutboxStatus.FAILED);
            assertThat(outbox.getLastErrorMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("이미 FAILED 상태에서 markAsFailed()를 호출하면 예외가 발생한다")
        void markAsFailed_FromFailed_ShouldThrowException() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);
            outbox.markAsFailed("First failure", FIXED_CLOCK);

            // when & then
            assertThatThrownBy(() -> outbox.markAsFailed("Second failure", FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("종료 상태");
        }
    }

    @Nested
    @DisplayName("상태 확인 테스트")
    class StatusCheckTest {

        @Test
        @DisplayName("초기 상태에서 canRetry()는 true를 반환한다")
        void canRetry_InitialState_ShouldReturnTrue() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);

            // when & then
            assertThat(outbox.canRetry()).isTrue();
        }

        @Test
        @DisplayName("최대 재시도 후 canRetry()는 false를 반환한다")
        void canRetry_AfterMaxRetry_ShouldReturnFalse() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);
            outbox.incrementRetry("Error 1", FIXED_CLOCK);
            outbox.incrementRetry("Error 2", FIXED_CLOCK);

            // when & then
            assertThat(outbox.canRetry()).isFalse();
            assertThat(outbox.hasReachedMaxRetry()).isTrue();
        }

        @Test
        @DisplayName("SENT 상태에서 canRetry()는 false를 반환한다")
        void canRetry_WhenSent_ShouldReturnFalse() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);
            outbox.markAsSent(FIXED_CLOCK);

            // when & then
            assertThat(outbox.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("getIdValue()는 UUID 값을 반환한다")
        void getIdValue_ShouldReturnUuid() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);

            // when & then
            assertThat(outbox.getIdValue()).isNotNull();
        }

        @Test
        @DisplayName("getExternalDownloadIdValue()는 UUID 값을 반환한다")
        void getExternalDownloadIdValue_ShouldReturnUuid() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);

            // when & then
            assertThat(outbox.getExternalDownloadIdValue()).isEqualTo(DOWNLOAD_ID.value());
        }

        @Test
        @DisplayName("getWebhookUrlValue()는 URL 문자열을 반환한다")
        void getWebhookUrlValue_ShouldReturnUrlString() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);

            // when & then
            assertThat(outbox.getWebhookUrlValue())
                    .isEqualTo("https://callback.example.com/webhook");
        }

        @Test
        @DisplayName("getFileAssetIdValue()는 FileAssetId가 있으면 UUID 값을 반환한다")
        void getFileAssetIdValue_WhenPresent_ShouldReturnUuid() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.COMPLETED,
                            FILE_ASSET_ID,
                            null,
                            FIXED_CLOCK);

            // when & then
            assertThat(outbox.getFileAssetIdValue()).isEqualTo(FILE_ASSET_ID.value());
        }

        @Test
        @DisplayName("getFileAssetIdValue()는 FileAssetId가 없으면 null을 반환한다")
        void getFileAssetIdValue_WhenAbsent_ShouldReturnNull() {
            // given
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            DOWNLOAD_ID,
                            WEBHOOK_URL,
                            ExternalDownloadStatus.FAILED,
                            null,
                            ERROR_MESSAGE,
                            FIXED_CLOCK);

            // when & then
            assertThat(outbox.getFileAssetIdValue()).isNull();
        }
    }
}
