package com.ryuqq.fileflow.application.download.internal;

import com.ryuqq.fileflow.application.asset.manager.command.AssetCommandManager;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadCompletionBundle;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadFailureBundle;
import com.ryuqq.fileflow.application.download.manager.command.CallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadCommandManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DownloadCompletionFacade {

    private final DownloadCommandManager downloadCommandManager;
    private final AssetCommandManager assetCommandManager;
    private final CallbackOutboxCommandManager callbackOutboxCommandManager;

    public DownloadCompletionFacade(
            DownloadCommandManager downloadCommandManager,
            AssetCommandManager assetCommandManager,
            CallbackOutboxCommandManager callbackOutboxCommandManager) {
        this.downloadCommandManager = downloadCommandManager;
        this.assetCommandManager = assetCommandManager;
        this.callbackOutboxCommandManager = callbackOutboxCommandManager;
    }

    @Transactional
    public void completeDownload(DownloadCompletionBundle bundle) {
        downloadCommandManager.persist(bundle.downloadTask());
        assetCommandManager.persist(bundle.asset());

        if (bundle.hasCallbackOutbox()) {
            callbackOutboxCommandManager.persist(bundle.callbackOutbox());
        }
    }

    @Transactional
    public void failDownload(DownloadFailureBundle bundle) {
        downloadCommandManager.persist(bundle.downloadTask());

        if (bundle.hasCallbackOutbox()) {
            callbackOutboxCommandManager.persist(bundle.callbackOutbox());
        }
    }
}
