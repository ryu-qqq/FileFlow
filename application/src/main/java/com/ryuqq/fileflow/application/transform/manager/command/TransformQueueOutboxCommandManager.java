package com.ryuqq.fileflow.application.transform.manager.command;

import com.ryuqq.fileflow.application.transform.port.out.command.TransformQueueOutboxPersistencePort;
import com.ryuqq.fileflow.application.transform.port.out.query.TransformQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformQueueOutboxCommandManager {

    private final TransformQueueOutboxPersistencePort transformQueueOutboxPersistencePort;
    private final TransformQueueOutboxQueryPort transformQueueOutboxQueryPort;

    public TransformQueueOutboxCommandManager(
            TransformQueueOutboxPersistencePort transformQueueOutboxPersistencePort,
            TransformQueueOutboxQueryPort transformQueueOutboxQueryPort) {
        this.transformQueueOutboxPersistencePort = transformQueueOutboxPersistencePort;
        this.transformQueueOutboxQueryPort = transformQueueOutboxQueryPort;
    }

    @Transactional
    public void persist(TransformQueueOutbox outbox) {
        transformQueueOutboxPersistencePort.persist(outbox);
    }

    @Transactional
    public List<TransformQueueOutbox> claimPendingMessages(int limit) {
        return transformQueueOutboxQueryPort.claimPendingMessages(limit);
    }

    @Transactional
    public void bulkMarkSent(List<String> ids, Instant now) {
        transformQueueOutboxPersistencePort.bulkMarkSent(ids, now);
    }

    @Transactional
    public void bulkMarkFailed(List<String> ids, Instant now, String lastError) {
        transformQueueOutboxPersistencePort.bulkMarkFailed(ids, now, lastError);
    }

    @Transactional
    public int recoverStuckProcessing(Instant cutoff) {
        return transformQueueOutboxPersistencePort.recoverStuckProcessing(cutoff);
    }
}
