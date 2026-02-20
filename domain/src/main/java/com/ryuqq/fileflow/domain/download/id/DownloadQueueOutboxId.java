package com.ryuqq.fileflow.domain.download.id;

public record DownloadQueueOutboxId(String value) {

    public static DownloadQueueOutboxId of(String value) {
        return new DownloadQueueOutboxId(value);
    }
}
