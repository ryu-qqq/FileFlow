package com.ryuqq.fileflow.application.download.internal;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.asset.manager.command.AssetCommandManager;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadCompletionBundle;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadFailureBundle;
import com.ryuqq.fileflow.application.download.manager.command.CallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadCommandManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
import com.ryuqq.fileflow.domain.download.id.CallbackOutboxId;
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
@DisplayName("DownloadCompletionFacade 단위 테스트")
class DownloadCompletionFacadeTest {

    @InjectMocks private DownloadCompletionFacade sut;
    @Mock private DownloadCommandManager downloadCommandManager;
    @Mock private AssetCommandManager assetCommandManager;
    @Mock private CallbackOutboxCommandManager callbackOutboxCommandManager;

    @Nested
    @DisplayName("completeDownload 메서드")
    class CompleteDownloadTest {

        @Test
        @DisplayName("다운로드 태스크와 에셋을 영속화한다")
        void completeDownload_PersistsTaskAndAsset() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aCompletedTask();
            Asset asset = AssetFixture.anAsset();
            DownloadCompletionBundle bundle =
                    new DownloadCompletionBundle(downloadTask, asset, null);

            // when
            sut.completeDownload(bundle);

            // then
            then(downloadCommandManager).should().persist(downloadTask);
            then(assetCommandManager).should().persist(asset);
            then(callbackOutboxCommandManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("콜백 아웃박스가 있으면 함께 영속화한다")
        void completeDownload_WithCallbackOutbox_PersistsOutbox() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aCompletedTask();
            Asset asset = AssetFixture.anAsset();
            CallbackOutbox callbackOutbox =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-001"),
                            downloadTask.idValue(),
                            "https://callback.example.com/done",
                            "COMPLETED",
                            Instant.parse("2026-01-01T00:00:30Z"));
            DownloadCompletionBundle bundle =
                    new DownloadCompletionBundle(downloadTask, asset, callbackOutbox);

            // when
            sut.completeDownload(bundle);

            // then
            then(downloadCommandManager).should().persist(downloadTask);
            then(assetCommandManager).should().persist(asset);
            then(callbackOutboxCommandManager).should().persist(callbackOutbox);
        }
    }

    @Nested
    @DisplayName("failDownload 메서드")
    class FailDownloadTest {

        @Test
        @DisplayName("실패한 다운로드 태스크를 영속화한다")
        void failDownload_PersistsFailedTask() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aFailedTask();
            DownloadFailureBundle bundle = new DownloadFailureBundle(downloadTask, null);

            // when
            sut.failDownload(bundle);

            // then
            then(downloadCommandManager).should().persist(downloadTask);
            then(callbackOutboxCommandManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("콜백 아웃박스가 있으면 함께 영속화한다")
        void failDownload_WithCallbackOutbox_PersistsOutbox() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aFailedTask();
            CallbackOutbox callbackOutbox =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-001"),
                            downloadTask.idValue(),
                            "https://callback.example.com/done",
                            "FAILED",
                            Instant.parse("2026-01-01T00:01:10Z"));
            DownloadFailureBundle bundle = new DownloadFailureBundle(downloadTask, callbackOutbox);

            // when
            sut.failDownload(bundle);

            // then
            then(downloadCommandManager).should().persist(downloadTask);
            then(callbackOutboxCommandManager).should().persist(callbackOutbox);
        }
    }
}
