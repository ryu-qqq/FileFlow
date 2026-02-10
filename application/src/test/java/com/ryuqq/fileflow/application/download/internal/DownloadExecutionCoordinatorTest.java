package com.ryuqq.fileflow.application.download.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadCompletionBundle;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadFailureBundle;
import com.ryuqq.fileflow.application.download.dto.response.FileDownloadResult;
import com.ryuqq.fileflow.application.download.factory.command.DownloadCommandFactory;
import com.ryuqq.fileflow.application.download.manager.client.DownloadQueueManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadCommandManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadExecutionCoordinator 단위 테스트")
class DownloadExecutionCoordinatorTest {

    @InjectMocks private DownloadExecutionCoordinator sut;
    @Mock private DownloadCommandFactory downloadCommandFactory;
    @Mock private FileTransferFacade fileTransferFacade;
    @Mock private DownloadCommandManager downloadCommandManager;
    @Mock private DownloadCompletionFacade downloadCompletionFacade;
    @Mock private DownloadQueueManager downloadQueueManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("다운로드 성공 시 완료 파사드에 위임한다")
        void execute_DownloadSuccess_CompletesViaFacade() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();
            Instant startTime = Instant.parse("2026-01-01T00:00:10Z");
            StatusChangeContext<String> startContext =
                    new StatusChangeContext<>(downloadTask.idValue(), startTime);

            FileDownloadResult successResult =
                    FileDownloadResult.success("image.jpg", "image/jpeg", 1024L, "etag-123");

            Asset asset = AssetFixture.anAsset();
            DownloadCompletionBundle completionBundle =
                    new DownloadCompletionBundle(downloadTask, asset, null);

            given(downloadCommandFactory.createStartContext(downloadTask.idValue()))
                    .willReturn(startContext);
            given(fileTransferFacade.transfer(downloadTask)).willReturn(successResult);
            given(downloadCommandFactory.createCompletionBundle(downloadTask, successResult))
                    .willReturn(completionBundle);

            // when
            sut.execute(downloadTask);

            // then
            then(downloadCommandManager).should().persist(downloadTask);
            then(fileTransferFacade).should().transfer(downloadTask);
            then(downloadCompletionFacade).should().completeDownload(completionBundle);
            then(downloadQueueManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("다운로드 실패 시 재시도 가능하면 큐에 재등록한다")
        void execute_DownloadFailure_RetryableRequeues() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();
            Instant startTime = Instant.parse("2026-01-01T00:00:10Z");
            StatusChangeContext<String> startContext =
                    new StatusChangeContext<>(downloadTask.idValue(), startTime);

            FileDownloadResult failureResult = FileDownloadResult.failure("Connection timeout");

            DownloadTask retryableTask = DownloadTaskFixture.aQueuedTask();
            DownloadFailureBundle failureBundle = new DownloadFailureBundle(retryableTask, null);

            given(downloadCommandFactory.createStartContext(downloadTask.idValue()))
                    .willReturn(startContext);
            given(fileTransferFacade.transfer(downloadTask)).willReturn(failureResult);
            given(downloadCommandFactory.createFailureBundle(downloadTask, "Connection timeout"))
                    .willReturn(failureBundle);

            // when
            sut.execute(downloadTask);

            // then
            then(downloadCommandManager).should().persist(downloadTask);
            then(downloadCompletionFacade).should().failDownload(failureBundle);
            then(downloadQueueManager).should().enqueue(downloadTask.idValue());
        }

        @Test
        @DisplayName("다운로드 실패 후 재시도 불가능하면 큐에 재등록하지 않는다")
        void execute_DownloadFailure_NotRetryable_DoesNotRequeue() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();
            Instant startTime = Instant.parse("2026-01-01T00:00:10Z");
            StatusChangeContext<String> startContext =
                    new StatusChangeContext<>(downloadTask.idValue(), startTime);

            FileDownloadResult failureResult = FileDownloadResult.failure("Connection timeout");

            DownloadTask exhaustedTask = DownloadTaskFixture.aFailedTask();
            DownloadFailureBundle failureBundle = new DownloadFailureBundle(exhaustedTask, null);

            given(downloadCommandFactory.createStartContext(downloadTask.idValue()))
                    .willReturn(startContext);
            given(fileTransferFacade.transfer(downloadTask)).willReturn(failureResult);
            given(downloadCommandFactory.createFailureBundle(downloadTask, "Connection timeout"))
                    .willReturn(failureBundle);

            // when
            sut.execute(downloadTask);

            // then
            then(downloadCompletionFacade).should().failDownload(failureBundle);
            then(downloadQueueManager).should(never()).enqueue(downloadTask.idValue());
        }

        @Test
        @DisplayName("시작 상태 변경 후 영속화를 먼저 수행한다")
        void execute_PersistsAfterStarting() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();
            Instant startTime = Instant.parse("2026-01-01T00:00:10Z");
            StatusChangeContext<String> startContext =
                    new StatusChangeContext<>(downloadTask.idValue(), startTime);

            FileDownloadResult successResult =
                    FileDownloadResult.success("image.jpg", "image/jpeg", 1024L, "etag-123");

            DownloadCompletionBundle completionBundle =
                    new DownloadCompletionBundle(downloadTask, AssetFixture.anAsset(), null);

            given(downloadCommandFactory.createStartContext(downloadTask.idValue()))
                    .willReturn(startContext);
            given(fileTransferFacade.transfer(downloadTask)).willReturn(successResult);
            given(downloadCommandFactory.createCompletionBundle(downloadTask, successResult))
                    .willReturn(completionBundle);

            // when
            sut.execute(downloadTask);

            // then
            then(downloadCommandFactory).should().createStartContext(downloadTask.idValue());
            then(downloadCommandManager).should().persist(downloadTask);
        }
    }
}
