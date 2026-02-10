package com.ryuqq.fileflow.application.download.dto.bundle;

import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;

public record DownloadFailureBundle(DownloadTask downloadTask, CallbackOutbox callbackOutbox) {

    public boolean hasCallbackOutbox() {
        return callbackOutbox != null;
    }

    public boolean canRetry() {
        return downloadTask.canRetry();
    }
}
