package com.ryuqq.fileflow.application.download.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.download.dto.DownloadResult;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadBundle;
import com.ryuqq.fileflow.application.download.dto.command.RequestExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.S3UploadResponse;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadFixture;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadOutboxFixture;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.iam.fixture.UserContextFixture;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalDownloadCommandFactory 테스트")
class ExternalDownloadCommandFactoryTest {

    @Mock private ClockHolder clockHolder;
    @Mock private Supplier<UserContext> userContextSupplier;

    private ExternalDownloadCommandFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ExternalDownloadCommandFactory(clockHolder, userContextSupplier);
    }

    @Nested
    @DisplayName("getClock 메서드")
    class GetClockTest {

        @Test
        @DisplayName("ClockHolder에서 Clock을 반환한다")
        void shouldReturnClockFromClockHolder() {
            // given
            Clock expectedClock = ClockFixture.defaultClock();
            given(clockHolder.getClock()).willReturn(expectedClock);

            // when
            Clock result = factory.getClock();

            // then
            assertThat(result).isEqualTo(expectedClock);
        }
    }

    @Nested
    @DisplayName("createBundle 메서드")
    class CreateBundleTest {

        @Test
        @DisplayName("RequestExternalDownloadCommand를 ExternalDownloadBundle로 변환한다")
        void shouldCreateBundle() {
            // given
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();
            String idempotencyKey = UUID.randomUUID().toString();
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            idempotencyKey,
                            "https://example.com/image.jpg",
                            tenantId.value(),
                            organizationId.value(),
                            null);

            UserContext userContext =
                    UserContextFixture.customSellerUserContext(
                            organizationId, "Test Company", "seller@test.com");

            given(clockHolder.getClock()).willReturn(ClockFixture.defaultClock());
            given(userContextSupplier.get()).willReturn(userContext);

            // when
            ExternalDownloadBundle result = factory.createBundle(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.download()).isNotNull();
            assertThat(result.outbox()).isNotNull();

            ExternalDownload download = result.download();
            assertThat(download.getSourceUrl().value()).isEqualTo("https://example.com/image.jpg");
            assertThat(download.getTenantId()).isEqualTo(tenantId);
            assertThat(download.getOrganizationId()).isEqualTo(organizationId);
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.PENDING);
            assertThat(download.getIdempotencyKey().value().toString()).isEqualTo(idempotencyKey);
        }

        @Test
        @DisplayName("webhookUrl이 있는 경우 Bundle에 포함된다")
        void shouldCreateBundleWithWebhook() {
            // given
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();
            String idempotencyKey = UUID.randomUUID().toString();
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            idempotencyKey,
                            "https://example.com/image.jpg",
                            tenantId.value(),
                            organizationId.value(),
                            "https://callback.example.com/webhook");

            UserContext userContext =
                    UserContextFixture.customSellerUserContext(
                            organizationId, "Test Company", "seller@test.com");

            given(clockHolder.getClock()).willReturn(ClockFixture.defaultClock());
            given(userContextSupplier.get()).willReturn(userContext);

            // when
            ExternalDownloadBundle result = factory.createBundle(command);

            // then
            assertThat(result.download().hasWebhook()).isTrue();
            assertThat(result.download().getWebhookUrl().value())
                    .isEqualTo("https://callback.example.com/webhook");
        }

        @Test
        @DisplayName("생성된 Download에 등록 이벤트가 추가된다")
        void shouldAddRegisteredEventToDownload() {
            // given
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();
            String idempotencyKey = UUID.randomUUID().toString();
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            idempotencyKey,
                            "https://example.com/image.jpg",
                            tenantId.value(),
                            organizationId.value(),
                            null);

            UserContext userContext =
                    UserContextFixture.customSellerUserContext(
                            organizationId, "Test Company", "seller@test.com");

            given(clockHolder.getClock()).willReturn(ClockFixture.defaultClock());
            given(userContextSupplier.get()).willReturn(userContext);

            // when
            ExternalDownloadBundle result = factory.createBundle(command);

            // then
            assertThat(result.download().getDomainEvents()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("createS3UploadResponse 메서드")
    class CreateS3UploadResponseTest {

        @Test
        @DisplayName("Download와 DownloadResult로 S3UploadResponse를 생성한다")
        void shouldCreateS3UploadResponse() {
            // given
            ExternalDownload download = ExternalDownloadFixture.processingExternalDownload();
            byte[] content = "test image content".getBytes();
            DownloadResult downloadResult =
                    new DownloadResult(content, "image/jpeg", content.length);

            given(clockHolder.getClock()).willReturn(ClockFixture.defaultClock());

            // when
            S3UploadResponse result = factory.createS3UploadResponse(download, downloadResult);

            // then
            assertThat(result).isNotNull();
            assertThat(result.s3Key()).isNotNull();
            assertThat(result.fileName()).isNotNull();
            assertThat(result.contentType().type()).isEqualTo("image/jpeg");
            assertThat(result.content()).isEqualTo(content);
        }

        @Test
        @DisplayName("PNG 이미지에 대해 올바른 확장자를 적용한다")
        void shouldApplyCorrectExtensionForPng() {
            // given
            ExternalDownload download = ExternalDownloadFixture.processingExternalDownload();
            byte[] content = "test png content".getBytes();
            DownloadResult downloadResult =
                    new DownloadResult(content, "image/png", content.length);

            given(clockHolder.getClock()).willReturn(ClockFixture.defaultClock());

            // when
            S3UploadResponse result = factory.createS3UploadResponse(download, downloadResult);

            // then
            assertThat(result.s3Key().key()).contains("png");
        }
    }

    @Nested
    @DisplayName("startProcessing 메서드")
    class StartProcessingTest {

        @Test
        @DisplayName("ExternalDownload를 PROCESSING 상태로 변경한다")
        void shouldStartProcessing() {
            // given
            ExternalDownload download = ExternalDownloadFixture.pendingExternalDownload();
            given(clockHolder.getClock()).willReturn(ClockFixture.defaultClock());

            // when
            factory.startProcessing(download);

            // then
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.PROCESSING);
        }
    }

    @Nested
    @DisplayName("complete 메서드")
    class CompleteTest {

        @Test
        @DisplayName("ExternalDownload를 COMPLETED 상태로 변경한다")
        void shouldCompleteDownload() {
            // given
            ExternalDownload download = ExternalDownloadFixture.processingExternalDownload();
            ContentType contentType = ContentType.of("image/jpeg");
            long contentLength = 1024L;
            S3Key s3Key = S3Key.of("uploads/test/image.jpg");
            ETag etag = ETag.of("test-etag");

            given(clockHolder.getClock()).willReturn(ClockFixture.defaultClock());

            // when
            factory.complete(download, contentType, contentLength, s3Key, etag);

            // then
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.COMPLETED);
            assertThat(download.getDomainEvents()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("markAsFailed 메서드")
    class MarkAsFailedTest {

        @Test
        @DisplayName("ExternalDownload를 FAILED 상태로 변경한다")
        void shouldMarkAsFailed() {
            // given
            ExternalDownload download = ExternalDownloadFixture.processingExternalDownload();
            String errorMessage = "다운로드 실패";
            FileAssetId defaultFileAssetId = FileAssetId.forNew();

            given(clockHolder.getClock()).willReturn(ClockFixture.defaultClock());

            // when
            boolean result = factory.markAsFailed(download, errorMessage, defaultFileAssetId);

            // then
            assertThat(result).isTrue();
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.FAILED);
            assertThat(download.getErrorMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("이미 완료된 Download는 실패 처리하지 않는다")
        void shouldNotMarkAsFailedWhenAlreadyCompleted() {
            // given
            ExternalDownload download = ExternalDownloadFixture.completedExternalDownload();
            String errorMessage = "다운로드 실패";
            FileAssetId defaultFileAssetId = FileAssetId.forNew();

            given(clockHolder.getClock()).willReturn(ClockFixture.defaultClock());

            // when
            boolean result = factory.markAsFailed(download, errorMessage, defaultFileAssetId);

            // then
            assertThat(result).isFalse();
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("markAsPublished 메서드")
    class MarkAsPublishedTest {

        @Test
        @DisplayName("Outbox를 PUBLISHED 상태로 변경한다")
        void shouldMarkAsPublished() {
            // given
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();
            given(clockHolder.getClock()).willReturn(ClockFixture.defaultClock());

            // when
            factory.markAsPublished(outbox);

            // then
            assertThat(outbox.isPublished()).isTrue();
        }
    }
}
