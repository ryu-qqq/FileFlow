package com.ryuqq.fileflow.application.download.manager.command;

import com.ryuqq.fileflow.application.download.port.out.command.DownloadQueueOutboxPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DownloadQueueOutboxCommandManager {

    private final DownloadQueueOutboxPersistencePort downloadQueueOutboxPersistencePort;

    public DownloadQueueOutboxCommandManager(
            DownloadQueueOutboxPersistencePort downloadQueueOutboxPersistencePort) {
        this.downloadQueueOutboxPersistencePort = downloadQueueOutboxPersistencePort;
    }

    @Transactional
    public void persist(DownloadQueueOutbox outbox) {
        downloadQueueOutboxPersistencePort.persist(outbox);
    }
}
