package com.ryuqq.fileflow.application.download.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.asset.factory.command.AssetCommandFactory;
import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadCompletionBundle;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadFailureBundle;
import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.dto.response.FileDownloadResult;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadCommandFactory 단위 테스트")
class DownloadCommandFactoryTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock private IdGeneratorPort idGeneratorPort;
    @Mock private AssetCommandFactory assetCommandFactory;

    private DownloadCommandFactory sut;

    @BeforeEach
    void setUp() {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(NOW, ZoneOffset.UTC));
        sut = new DownloadCommandFactory(idGeneratorPort, timeProvider, assetCommandFactory);
    }

    @Nested
    @DisplayName("create 메서드")
    class CreateTest {

        @Test
        @DisplayName("커맨드로 QUEUED 상태의 DownloadTask를 생성한다")
        void create_ValidCommand_ReturnsQueuedDownloadTask() {
            // given
            given(idGeneratorPort.generate()).willReturn("download-001");

            CreateDownloadTaskCommand command =
                    new CreateDownloadTaskCommand(
                            "https://example.com/image.jpg",
                            "public/2026/02/download-001.jpg",
                            "test-bucket",
                            AccessType.PUBLIC,
                            "product-image",
                            "commerce-service",
                            "https://callback.example.com/done");

            // when
            DownloadTask result = sut.create(command);

            // then
            assertThat(result.idValue()).isEqualTo("download-001");
            assertThat(result.sourceUrlValue()).isEqualTo("https://example.com/image.jpg");
            assertThat(result.s3Key()).isEqualTo("public/2026/02/download-001.jpg");
            assertThat(result.bucket()).isEqualTo("test-bucket");
            assertThat(result.accessType()).isEqualTo(AccessType.PUBLIC);
            assertThat(result.purpose()).isEqualTo("product-image");
            assertThat(result.source()).isEqualTo("commerce-service");
            assertThat(result.status()).isEqualTo(DownloadTaskStatus.QUEUED);
            assertThat(result.callbackUrl()).isEqualTo("https://callback.example.com/done");
            assertThat(result.createdAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("createStartContext 메서드")
    class CreateStartContextTest {

        @Test
        @DisplayName("다운로드 태스크 ID와 현재 시간으로 StatusChangeContext를 생성한다")
        void createStartContext_ReturnsContextWithIdAndTime() {
            // given
            String downloadTaskId = "download-001";

            // when
            StatusChangeContext<String> result = sut.createStartContext(downloadTaskId);

            // then
            assertThat(result.id()).isEqualTo("download-001");
            assertThat(result.changedAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("createCompletionBundle 메서드")
    class CreateCompletionBundleTest {

        @Test
        @DisplayName("다운로드 완료 시 DownloadCompletionBundle을 생성한다")
        void createCompletionBundle_SuccessResult_ReturnsBundleWithAsset() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aDownloadingTask();
            FileDownloadResult result =
                    FileDownloadResult.success("image.jpg", "image/jpeg", 1024L, "etag-123");
            Asset mockAsset = AssetFixture.anAsset();

            given(assetCommandFactory.createAsset(any())).willReturn(mockAsset);

            // when
            DownloadCompletionBundle bundle = sut.createCompletionBundle(downloadTask, result);

            // then
            assertThat(bundle.downloadTask()).isEqualTo(downloadTask);
            assertThat(bundle.asset()).isEqualTo(mockAsset);
            assertThat(bundle.hasCallbackOutbox()).isTrue();
            assertThat(downloadTask.status()).isEqualTo(DownloadTaskStatus.COMPLETED);
        }

        @Test
        @DisplayName("콜백이 없는 태스크의 완료 번들에는 CallbackOutbox가 없다")
        void createCompletionBundle_NoCallback_ReturnsBundleWithoutOutbox() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aTaskWithoutCallback();
            downloadTask.start(NOW.plusSeconds(10));
            FileDownloadResult result =
                    FileDownloadResult.success("image.jpg", "image/jpeg", 1024L, "etag-123");
            Asset mockAsset = AssetFixture.anAsset();

            given(assetCommandFactory.createAsset(any())).willReturn(mockAsset);

            // when
            DownloadCompletionBundle bundle = sut.createCompletionBundle(downloadTask, result);

            // then
            assertThat(bundle.hasCallbackOutbox()).isFalse();
        }
    }

    @Nested
    @DisplayName("createFailureBundle 메서드")
    class CreateFailureBundleTest {

        @Test
        @DisplayName("재시도 가능한 실패 시 콜백 없이 FailureBundle을 생성한다")
        void createFailureBundle_RetryableFailure_ReturnsBundleWithoutCallback() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aDownloadingTask();
            String errorMessage = "Connection timeout";

            // when
            DownloadFailureBundle bundle = sut.createFailureBundle(downloadTask, errorMessage);

            // then
            assertThat(bundle.downloadTask()).isEqualTo(downloadTask);
            assertThat(bundle.canRetry()).isTrue();
            assertThat(bundle.hasCallbackOutbox()).isFalse();
        }

        @Test
        @DisplayName("최종 실패(재시도 소진) 시 콜백이 있으면 CallbackOutbox를 포함한다")
        void createFailureBundle_ExhaustedRetries_ReturnsBundleWithCallback() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aFailedTask();
            String errorMessage = "Final failure";

            given(idGeneratorPort.generate()).willReturn("outbox-001");

            // when
            DownloadFailureBundle bundle = sut.createFailureBundle(downloadTask, errorMessage);

            // then
            assertThat(bundle.downloadTask()).isEqualTo(downloadTask);
            assertThat(bundle.canRetry()).isFalse();
            assertThat(bundle.hasCallbackOutbox()).isTrue();
        }
    }

    @Nested
    @DisplayName("createCallbackOutbox 메서드")
    class CreateCallbackOutboxTest {

        @Test
        @DisplayName("콜백 아웃박스를 PENDING 상태로 생성한다")
        void createCallbackOutbox_ReturnsNewOutbox() {
            // given
            given(idGeneratorPort.generate()).willReturn("outbox-001");

            // when
            CallbackOutbox result =
                    sut.createCallbackOutbox(
                            "download-001", "https://callback.example.com/done", "COMPLETED");

            // then
            assertThat(result.idValue()).isEqualTo("outbox-001");
            assertThat(result.downloadTaskId()).isEqualTo("download-001");
            assertThat(result.callbackUrl()).isEqualTo("https://callback.example.com/done");
            assertThat(result.taskStatus()).isEqualTo("COMPLETED");
            assertThat(result.outboxStatus().name()).isEqualTo("PENDING");
            assertThat(result.createdAt()).isEqualTo(NOW);
        }
    }
}
