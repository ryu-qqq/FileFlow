package com.ryuqq.fileflow.application.download.port.out.command;

import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;

public interface DownloadQueueOutboxPersistencePort {

    void persist(DownloadQueueOutbox outbox);
}
