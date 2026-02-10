package com.ryuqq.fileflow.application.download.dto.bundle;

import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;

public record DownloadCompletionBundle(
        DownloadTask downloadTask, Asset asset, CallbackOutbox callbackOutbox) {

    public boolean hasCallbackOutbox() {
        return callbackOutbox != null;
    }
}
