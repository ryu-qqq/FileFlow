package com.ryuqq.fileflow.application.download.manager.command;

import com.ryuqq.fileflow.application.download.port.out.command.DownloadQueueOutboxPersistencePort;
import com.ryuqq.fileflow.application.download.port.out.query.DownloadQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DownloadQueueOutboxCommandManager {

    private final DownloadQueueOutboxPersistencePort downloadQueueOutboxPersistencePort;
    private final DownloadQueueOutboxQueryPort downloadQueueOutboxQueryPort;

    public DownloadQueueOutboxCommandManager(
            DownloadQueueOutboxPersistencePort downloadQueueOutboxPersistencePort,
            DownloadQueueOutboxQueryPort downloadQueueOutboxQueryPort) {
        this.downloadQueueOutboxPersistencePort = downloadQueueOutboxPersistencePort;
        this.downloadQueueOutboxQueryPort = downloadQueueOutboxQueryPort;
    }

    @Transactional
    public void persist(DownloadQueueOutbox outbox) {
        downloadQueueOutboxPersistencePort.persist(outbox);
    }

    @Transactional
    public List<DownloadQueueOutbox> claimPendingMessages(int limit) {
        return downloadQueueOutboxQueryPort.claimPendingMessages(limit);
    }

    @Transactional
    public void bulkMarkSent(List<String> ids, Instant now) {
        downloadQueueOutboxPersistencePort.bulkMarkSent(ids, now);
    }

    @Transactional
    public void bulkMarkFailed(List<String> ids, Instant now) {
        downloadQueueOutboxPersistencePort.bulkMarkFailed(ids, now);
    }

    @Transactional
    public int recoverStuckProcessing(Instant cutoff) {
        return downloadQueueOutboxPersistencePort.recoverStuckProcessing(cutoff);
    }
}
