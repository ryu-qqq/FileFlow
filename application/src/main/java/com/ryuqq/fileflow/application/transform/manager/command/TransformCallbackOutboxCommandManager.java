package com.ryuqq.fileflow.application.transform.manager.command;

import com.ryuqq.fileflow.application.transform.port.out.command.TransformCallbackOutboxPersistencePort;
import com.ryuqq.fileflow.application.transform.port.out.query.TransformCallbackOutboxQueryPort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformCallbackOutboxCommandManager {

    private final TransformCallbackOutboxPersistencePort transformCallbackOutboxPersistencePort;
    private final TransformCallbackOutboxQueryPort transformCallbackOutboxQueryPort;

    public TransformCallbackOutboxCommandManager(
            TransformCallbackOutboxPersistencePort transformCallbackOutboxPersistencePort,
            TransformCallbackOutboxQueryPort transformCallbackOutboxQueryPort) {
        this.transformCallbackOutboxPersistencePort = transformCallbackOutboxPersistencePort;
        this.transformCallbackOutboxQueryPort = transformCallbackOutboxQueryPort;
    }

    public void persist(TransformCallbackOutbox outbox) {
        transformCallbackOutboxPersistencePort.persist(outbox);
    }

    @Transactional
    public List<TransformCallbackOutbox> claimPendingMessages(int limit) {
        return transformCallbackOutboxQueryPort.claimPendingMessages(limit);
    }

    @Transactional
    public void bulkMarkSent(List<String> ids, Instant now) {
        transformCallbackOutboxPersistencePort.bulkMarkSent(ids, now);
    }

    @Transactional
    public void bulkMarkFailed(List<String> ids, Instant now, String lastError) {
        transformCallbackOutboxPersistencePort.bulkMarkFailed(ids, now, lastError);
    }

    @Transactional
    public int recoverStuckProcessing(Instant cutoff) {
        return transformCallbackOutboxPersistencePort.recoverStuckProcessing(cutoff);
    }
}
