package com.ryuqq.fileflow.domain.download.aggregate;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.common.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadRegisteredEvent;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadWebhookTriggeredEvent;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.RetryCount;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownload Aggregate 단위 테스트")
class ExternalDownloadTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-11-26T12:00:00Z"), ZoneId.of("UTC"));
    private static final S3Bucket DEFAULT_S3_BUCKET = S3Bucket.of("setof");
    private static final String DEFAULT_S3_PATH_PREFIX = "customer/";

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 ExternalDownload를 생성할 수 있다")
        void forNew_ShouldCreateNewExternalDownload() {
            // given
            IdempotencyKey idempotencyKey = IdempotencyKey.forNew();
            SourceUrl sourceUrl = SourceUrl.of("https://example.com/image.jpg");
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();

            // when
            ExternalDownload download =
                    ExternalDownload.forNew(
                            idempotencyKey,
                            sourceUrl,
                            tenantId,
                            organizationId,
                            DEFAULT_S3_BUCKET,
                            DEFAULT_S3_PATH_PREFIX,
                            null, // webhookUrl
                            FIXED_CLOCK);

            // then
            assertThat(download.getId().isNew()).isFalse(); // UUID는 항상 값이 있음
            assertThat(download.getIdempotencyKey()).isEqualTo(idempotencyKey);
            assertThat(download.getSourceUrl()).isEqualTo(sourceUrl);
            assertThat(download.getTenantId()).isEqualTo(tenantId);
            assertThat(download.getOrganizationId()).isEqualTo(organizationId);
            assertThat(download.getS3Bucket()).isEqualTo(DEFAULT_S3_BUCKET);
            assertThat(download.getS3PathPrefix()).isEqualTo(DEFAULT_S3_PATH_PREFIX);
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.PENDING);
            assertThat(download.getRetryCountValue()).isZero();
            assertThat(download.getFileAssetId()).isNull();
            assertThat(download.getErrorMessage()).isNull();
            assertThat(download.getWebhookUrl()).isNull();
            assertThat(download.getCreatedAt()).isEqualTo(Instant.now(FIXED_CLOCK));
            assertThat(download.getUpdatedAt()).isEqualTo(Instant.now(FIXED_CLOCK));
        }

        @Test
        @DisplayName("forNew()에 WebhookUrl을 전달하면 설정된다")
        void forNew_WithWebhookUrl_ShouldSetWebhookUrl() {
            // given
            IdempotencyKey idempotencyKey = IdempotencyKey.forNew();
            SourceUrl sourceUrl = SourceUrl.of("https://example.com/image.jpg");
            WebhookUrl webhookUrl = WebhookUrl.of("https://callback.example.com/webhook");
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();

            // when
            ExternalDownload download =
                    ExternalDownload.forNew(
                            idempotencyKey,
                            sourceUrl,
                            tenantId,
                            organizationId,
                            DEFAULT_S3_BUCKET,
                            DEFAULT_S3_PATH_PREFIX,
                            webhookUrl,
                            FIXED_CLOCK);

            // then
            assertThat(download.getWebhookUrl()).isEqualTo(webhookUrl);
            assertThat(download.hasWebhook()).isTrue();
        }

        @Test
        @DisplayName("of()로 기존 ExternalDownload를 재구성할 수 있다")
        void of_ShouldReconstituteExternalDownload() {
            // given
            ExternalDownloadId id = ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");
            IdempotencyKey idempotencyKey = IdempotencyKey.forNew();
            SourceUrl sourceUrl = SourceUrl.of("https://example.com/image.jpg");
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();
            ExternalDownloadStatus status = ExternalDownloadStatus.PROCESSING;
            RetryCount retryCount = RetryCount.of(1);
            FileAssetId fileAssetId = null;
            String errorMessage = null;
            WebhookUrl webhookUrl = WebhookUrl.of("https://callback.example.com/webhook");
            Instant createdAt = Instant.parse("2025-11-26T10:00:00Z");
            Instant updatedAt = Instant.parse("2025-11-26T11:00:00Z");

            // when
            ExternalDownload download =
                    ExternalDownload.of(
                            id,
                            idempotencyKey,
                            sourceUrl,
                            tenantId,
                            organizationId,
                            DEFAULT_S3_BUCKET,
                            DEFAULT_S3_PATH_PREFIX,
                            status,
                            retryCount,
                            fileAssetId,
                            errorMessage,
                            webhookUrl,
                            createdAt,
                            updatedAt,
                            0L);

            // then
            assertThat(download.getId()).isEqualTo(id);
            assertThat(download.getIdempotencyKey()).isEqualTo(idempotencyKey);
            assertThat(download.getSourceUrl()).isEqualTo(sourceUrl);
            assertThat(download.getTenantId()).isEqualTo(tenantId);
            assertThat(download.getOrganizationId()).isEqualTo(organizationId);
            assertThat(download.getS3Bucket()).isEqualTo(DEFAULT_S3_BUCKET);
            assertThat(download.getS3PathPrefix()).isEqualTo(DEFAULT_S3_PATH_PREFIX);
            assertThat(download.getStatus()).isEqualTo(status);
            assertThat(download.getRetryCount()).isEqualTo(retryCount);
            assertThat(download.getWebhookUrl()).isEqualTo(webhookUrl);
            assertThat(download.getCreatedAt()).isEqualTo(createdAt);
            assertThat(download.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("forNew()에 sourceUrl이 null이면 예외가 발생한다")
        void forNew_WithNullSourceUrl_ShouldThrowException() {
            // given
            IdempotencyKey idempotencyKey = IdempotencyKey.forNew();
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();

            // when & then
            assertThatThrownBy(
                            () ->
                                    ExternalDownload.forNew(
                                            idempotencyKey,
                                            null,
                                            tenantId,
                                            organizationId,
                                            DEFAULT_S3_BUCKET,
                                            DEFAULT_S3_PATH_PREFIX,
                                            null,
                                            FIXED_CLOCK))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("sourceUrl");
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트")
    class StateTransitionTest {

        @Test
        @DisplayName("startProcessing()을 호출하면 PENDING에서 PROCESSING으로 전환된다")
        void startProcessing_FromPending_ShouldTransitionToProcessing() {
            // given
            ExternalDownload download = createPendingDownload();

            // when
            download.startProcessing(FIXED_CLOCK);

            // then
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.PROCESSING);
            assertThat(download.getUpdatedAt()).isEqualTo(Instant.now(FIXED_CLOCK));
        }

        @Test
        @DisplayName("PENDING이 아닌 상태에서 startProcessing() 호출 시 예외가 발생한다")
        void startProcessing_FromNonPending_ShouldThrowException() {
            // given
            ExternalDownload download = createProcessingDownload();

            // when & then
            assertThatThrownBy(() -> download.startProcessing(FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("complete()를 호출하면 PROCESSING에서 COMPLETED로 전환된다")
        void complete_FromProcessing_ShouldTransitionToCompleted() {
            // given
            ExternalDownload download = createProcessingDownload();
            FileAssetId fileAssetId = FileAssetId.forNew();

            // when
            download.complete(fileAssetId, FIXED_CLOCK);

            // then
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.COMPLETED);
            assertThat(download.getFileAssetId()).isEqualTo(fileAssetId);
            assertThat(download.getUpdatedAt()).isEqualTo(Instant.now(FIXED_CLOCK));
        }

        @Test
        @DisplayName("PROCESSING이 아닌 상태에서 complete() 호출 시 예외가 발생한다")
        void complete_FromNonProcessing_ShouldThrowException() {
            // given
            ExternalDownload download = createPendingDownload();
            FileAssetId fileAssetId = FileAssetId.forNew();

            // when & then
            assertThatThrownBy(() -> download.complete(fileAssetId, FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PROCESSING");
        }

        @Test
        @DisplayName("retry()를 호출하면 PROCESSING에서 PENDING으로 전환되고 retryCount가 증가한다")
        void retry_FromProcessing_ShouldTransitionToPendingAndIncrementRetryCount() {
            // given
            ExternalDownload download = createProcessingDownload();
            int originalRetryCount = download.getRetryCountValue();

            // when
            download.retry(FIXED_CLOCK);

            // then
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.PENDING);
            assertThat(download.getRetryCountValue()).isEqualTo(originalRetryCount + 1);
            assertThat(download.getUpdatedAt()).isEqualTo(Instant.now(FIXED_CLOCK));
        }

        @Test
        @DisplayName("retryCount가 2 이상일 때 retry() 호출 시 예외가 발생한다")
        void retry_WhenRetryCountExceeded_ShouldThrowException() {
            // given
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();
            ExternalDownload download =
                    ExternalDownload.of(
                            ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"),
                            IdempotencyKey.forNew(),
                            SourceUrl.of("https://example.com/image.jpg"),
                            tenantId,
                            organizationId,
                            DEFAULT_S3_BUCKET,
                            DEFAULT_S3_PATH_PREFIX,
                            ExternalDownloadStatus.PROCESSING,
                            RetryCount.of(2), // retryCount already 2
                            null,
                            null,
                            null,
                            Instant.now(),
                            Instant.now(),
                            0L);

            // when & then
            assertThatThrownBy(() -> download.retry(FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("재시도");
        }

        @Test
        @DisplayName("fail()을 호출하면 PROCESSING에서 FAILED로 전환되고 에러 메시지와 디폴트 이미지가 설정된다")
        void fail_FromProcessing_ShouldTransitionToFailedWithErrorAndDefaultImage() {
            // given
            ExternalDownload download = createProcessingDownload();
            String errorMessage = "다운로드 실패: Connection timeout";
            FileAssetId defaultFileAssetId = FileAssetId.forNew();

            // when
            download.fail(errorMessage, defaultFileAssetId, FIXED_CLOCK);

            // then
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.FAILED);
            assertThat(download.getErrorMessage()).isEqualTo(errorMessage);
            assertThat(download.getFileAssetId()).isEqualTo(defaultFileAssetId);
            assertThat(download.getUpdatedAt()).isEqualTo(Instant.now(FIXED_CLOCK));
        }

        @Test
        @DisplayName("PROCESSING이 아닌 상태에서 fail() 호출 시 예외가 발생한다")
        void fail_FromNonProcessing_ShouldThrowException() {
            // given
            ExternalDownload download = createPendingDownload();

            // when & then
            assertThatThrownBy(() -> download.fail("error", FileAssetId.forNew(), FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PROCESSING");
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRuleTest {

        @Test
        @DisplayName("canRetry()는 retryCount가 2 미만이면 true를 반환한다")
        void canRetry_WhenRetryCountLessThan2_ShouldReturnTrue() {
            // given
            ExternalDownload download = createProcessingDownload();

            // when & then
            assertThat(download.canRetry()).isTrue();
        }

        @Test
        @DisplayName("canRetry()는 retryCount가 2 이상이면 false를 반환한다")
        void canRetry_WhenRetryCountGreaterOrEqual2_ShouldReturnFalse() {
            // given
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();
            ExternalDownload download =
                    ExternalDownload.of(
                            ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"),
                            IdempotencyKey.forNew(),
                            SourceUrl.of("https://example.com/image.jpg"),
                            tenantId,
                            organizationId,
                            DEFAULT_S3_BUCKET,
                            DEFAULT_S3_PATH_PREFIX,
                            ExternalDownloadStatus.PROCESSING,
                            RetryCount.of(2), // retryCount = 2
                            null,
                            null,
                            null,
                            Instant.now(),
                            Instant.now(),
                            0L);

            // when & then
            assertThat(download.canRetry()).isFalse();
        }

        @Test
        @DisplayName("hasWebhook()은 webhookUrl이 있으면 true를 반환한다")
        void hasWebhook_WithWebhookUrl_ShouldReturnTrue() {
            // given
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();
            ExternalDownload download =
                    ExternalDownload.forNew(
                            IdempotencyKey.forNew(),
                            SourceUrl.of("https://example.com/image.jpg"),
                            tenantId,
                            organizationId,
                            DEFAULT_S3_BUCKET,
                            DEFAULT_S3_PATH_PREFIX,
                            WebhookUrl.of("https://callback.example.com/webhook"),
                            FIXED_CLOCK);

            // when & then
            assertThat(download.hasWebhook()).isTrue();
        }

        @Test
        @DisplayName("hasWebhook()은 webhookUrl이 없으면 false를 반환한다")
        void hasWebhook_WithoutWebhookUrl_ShouldReturnFalse() {
            // given
            ExternalDownload download = createPendingDownload();

            // when & then
            assertThat(download.hasWebhook()).isFalse();
        }

        @Test
        @DisplayName("getIdValue()는 ID의 UUID 값을 반환한다")
        void getIdValue_ShouldReturnUuidValue() {
            // given
            String uuidString = "00000000-0000-0000-0000-000000000001";
            UUID expectedUuid = UUID.fromString(uuidString);
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();
            ExternalDownload download =
                    ExternalDownload.of(
                            ExternalDownloadId.of(uuidString),
                            IdempotencyKey.forNew(),
                            SourceUrl.of("https://example.com/image.jpg"),
                            tenantId,
                            organizationId,
                            DEFAULT_S3_BUCKET,
                            DEFAULT_S3_PATH_PREFIX,
                            ExternalDownloadStatus.PENDING,
                            RetryCount.initial(),
                            null,
                            null,
                            null,
                            Instant.now(),
                            Instant.now(),
                            0L);

            // when & then
            assertThat(download.getIdValue()).isEqualTo(expectedUuid);
        }

        @Test
        @DisplayName("createRegisteredEvent()는 등록 이벤트를 생성한다")
        void createRegisteredEvent_ShouldCreateEvent() {
            // given
            ExternalDownload download = createPendingDownload();

            // when
            ExternalDownloadRegisteredEvent event = download.createRegisteredEvent();

            // then
            assertThat(event.downloadId()).isEqualTo(download.getId());
            assertThat(event.sourceUrl()).isEqualTo(download.getSourceUrl());
            assertThat(event.tenantId()).isEqualTo(download.getTenantId());
            assertThat(event.organizationId()).isEqualTo(download.getOrganizationId());
            assertThat(event.webhookUrl()).isNull();
            assertThat(event.occurredAt()).isEqualTo(download.getCreatedAt());
        }

        @Test
        @DisplayName("createRegisteredEvent()는 webhookUrl이 있는 경우 이벤트에 포함한다")
        void createRegisteredEvent_WithWebhookUrl_ShouldIncludeInEvent() {
            // given
            WebhookUrl webhookUrl = WebhookUrl.of("https://callback.example.com/webhook");
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();
            ExternalDownload download =
                    ExternalDownload.forNew(
                            IdempotencyKey.forNew(),
                            SourceUrl.of("https://example.com/image.jpg"),
                            tenantId,
                            organizationId,
                            DEFAULT_S3_BUCKET,
                            DEFAULT_S3_PATH_PREFIX,
                            webhookUrl,
                            FIXED_CLOCK);

            // when
            ExternalDownloadRegisteredEvent event = download.createRegisteredEvent();

            // then
            assertThat(event.webhookUrl()).isEqualTo(webhookUrl);
        }
    }

    @Nested
    @DisplayName("도메인 이벤트 관리 테스트")
    class DomainEventManagementTest {

        @Test
        @DisplayName("registerEvent()로 도메인 이벤트를 등록할 수 있다")
        void registerEvent_ShouldAddEventToList() {
            // given
            ExternalDownload download = createPendingDownload();
            ExternalDownloadRegisteredEvent event = download.createRegisteredEvent();

            // when
            download.registerEvent(event);

            // then
            assertThat(download.getDomainEvents()).hasSize(1);
            assertThat(download.getDomainEvents().get(0)).isEqualTo(event);
        }

        @Test
        @DisplayName("registerEvent()에 null을 전달하면 예외가 발생한다")
        void registerEvent_WithNull_ShouldThrowException() {
            // given
            ExternalDownload download = createPendingDownload();

            // when & then
            assertThatThrownBy(() -> download.registerEvent(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("event");
        }

        @Test
        @DisplayName("getDomainEvents()는 불변 리스트를 반환한다")
        void getDomainEvents_ShouldReturnUnmodifiableList() {
            // given
            ExternalDownload download = createPendingDownload();
            download.registerEvent(download.createRegisteredEvent());

            // when & then
            assertThatThrownBy(() -> download.getDomainEvents().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("clearDomainEvents()는 모든 이벤트를 제거한다")
        void clearDomainEvents_ShouldRemoveAllEvents() {
            // given
            ExternalDownload download = createPendingDownload();
            download.registerEvent(download.createRegisteredEvent());
            assertThat(download.getDomainEvents()).hasSize(1);

            // when
            download.clearDomainEvents();

            // then
            assertThat(download.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("여러 이벤트를 순서대로 등록할 수 있다")
        void registerEvent_MultipleEvents_ShouldMaintainOrder() {
            // given
            ExternalDownload download = createPendingDownload();
            ExternalDownloadRegisteredEvent event1 = download.createRegisteredEvent();
            ExternalDownloadRegisteredEvent event2 = download.createRegisteredEvent();

            // when
            download.registerEvent(event1);
            download.registerEvent(event2);

            // then
            assertThat(download.getDomainEvents()).hasSize(2);
            assertThat(download.getDomainEvents().get(0)).isEqualTo(event1);
            assertThat(download.getDomainEvents().get(1)).isEqualTo(event2);
        }
    }

    @Nested
    @DisplayName("Webhook 이벤트 테스트")
    class WebhookEventTest {

        @Test
        @DisplayName("complete() 호출 시 webhookUrl이 있으면 WebhookTriggeredEvent가 등록된다")
        void complete_WithWebhook_ShouldRegisterWebhookTriggeredEvent() {
            // given
            ExternalDownload download = createProcessingDownloadWithWebhook();
            download.clearDomainEvents(); // 기존 이벤트 초기화

            // when
            download.complete(
                    "image/jpeg",
                    1024L,
                    com.ryuqq.fileflow.domain.session.vo.S3Key.of("test/image.jpg"),
                    com.ryuqq.fileflow.domain.session.vo.ETag.of("etag123"),
                    FIXED_CLOCK);

            // then
            assertThat(download.getDomainEvents())
                    .hasSize(2); // FileCreatedEvent + WebhookTriggeredEvent

            boolean hasWebhookEvent =
                    download.getDomainEvents().stream()
                            .anyMatch(e -> e instanceof ExternalDownloadWebhookTriggeredEvent);
            assertThat(hasWebhookEvent).isTrue();

            ExternalDownloadWebhookTriggeredEvent webhookEvent =
                    download.getDomainEvents().stream()
                            .filter(e -> e instanceof ExternalDownloadWebhookTriggeredEvent)
                            .map(e -> (ExternalDownloadWebhookTriggeredEvent) e)
                            .findFirst()
                            .orElseThrow();

            assertThat(webhookEvent.isCompleted()).isTrue();
            assertThat(webhookEvent.downloadId()).isEqualTo(download.getId());
            assertThat(webhookEvent.webhookUrl()).isEqualTo(download.getWebhookUrl());
        }

        @Test
        @DisplayName("complete() 호출 시 webhookUrl이 없으면 WebhookTriggeredEvent가 등록되지 않는다")
        void complete_WithoutWebhook_ShouldNotRegisterWebhookTriggeredEvent() {
            // given
            ExternalDownload download = createProcessingDownload();
            download.clearDomainEvents();

            // when
            download.complete(
                    "image/jpeg",
                    1024L,
                    com.ryuqq.fileflow.domain.session.vo.S3Key.of("test/image.jpg"),
                    com.ryuqq.fileflow.domain.session.vo.ETag.of("etag123"),
                    FIXED_CLOCK);

            // then
            assertThat(download.getDomainEvents()).hasSize(1); // FileCreatedEvent만

            boolean hasWebhookEvent =
                    download.getDomainEvents().stream()
                            .anyMatch(e -> e instanceof ExternalDownloadWebhookTriggeredEvent);
            assertThat(hasWebhookEvent).isFalse();
        }

        @Test
        @DisplayName("fail() 호출 시 webhookUrl이 있으면 WebhookTriggeredEvent가 등록된다")
        void fail_WithWebhook_ShouldRegisterWebhookTriggeredEvent() {
            // given
            ExternalDownload download = createProcessingDownloadWithWebhook();
            download.clearDomainEvents();
            String errorMessage = "다운로드 실패";
            FileAssetId defaultFileAssetId = FileAssetId.forNew();

            // when
            download.fail(errorMessage, defaultFileAssetId, FIXED_CLOCK);

            // then
            assertThat(download.getDomainEvents()).hasSize(1); // WebhookTriggeredEvent만

            ExternalDownloadWebhookTriggeredEvent webhookEvent =
                    (ExternalDownloadWebhookTriggeredEvent) download.getDomainEvents().get(0);

            assertThat(webhookEvent.isFailed()).isTrue();
            assertThat(webhookEvent.downloadId()).isEqualTo(download.getId());
            assertThat(webhookEvent.webhookUrl()).isEqualTo(download.getWebhookUrl());
            assertThat(webhookEvent.errorMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("fail() 호출 시 webhookUrl이 없으면 WebhookTriggeredEvent가 등록되지 않는다")
        void fail_WithoutWebhook_ShouldNotRegisterWebhookTriggeredEvent() {
            // given
            ExternalDownload download = createProcessingDownload();
            download.clearDomainEvents();
            String errorMessage = "다운로드 실패";
            FileAssetId defaultFileAssetId = FileAssetId.forNew();

            // when
            download.fail(errorMessage, defaultFileAssetId, FIXED_CLOCK);

            // then
            assertThat(download.getDomainEvents()).isEmpty();
        }
    }

    // Helper methods
    private ExternalDownload createPendingDownload() {
        return ExternalDownload.forNew(
                IdempotencyKey.forNew(),
                SourceUrl.of("https://example.com/image.jpg"),
                TenantId.generate(),
                OrganizationId.generate(),
                DEFAULT_S3_BUCKET,
                DEFAULT_S3_PATH_PREFIX,
                null,
                FIXED_CLOCK);
    }

    private ExternalDownload createProcessingDownload() {
        return ExternalDownload.of(
                ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"),
                IdempotencyKey.forNew(),
                SourceUrl.of("https://example.com/image.jpg"),
                TenantId.generate(),
                OrganizationId.generate(),
                DEFAULT_S3_BUCKET,
                DEFAULT_S3_PATH_PREFIX,
                ExternalDownloadStatus.PROCESSING,
                RetryCount.initial(),
                null,
                null,
                null,
                Instant.parse("2025-11-26T10:00:00Z"),
                Instant.parse("2025-11-26T11:00:00Z"),
                0L);
    }

    private ExternalDownload createProcessingDownloadWithWebhook() {
        return ExternalDownload.of(
                ExternalDownloadId.of("00000000-0000-0000-0000-000000000002"),
                IdempotencyKey.forNew(),
                SourceUrl.of("https://example.com/image.jpg"),
                TenantId.generate(),
                OrganizationId.generate(),
                DEFAULT_S3_BUCKET,
                DEFAULT_S3_PATH_PREFIX,
                ExternalDownloadStatus.PROCESSING,
                RetryCount.initial(),
                null,
                null,
                WebhookUrl.of("https://callback.example.com/webhook"),
                Instant.parse("2025-11-26T10:00:00Z"),
                Instant.parse("2025-11-26T11:00:00Z"),
                0L);
    }
}
