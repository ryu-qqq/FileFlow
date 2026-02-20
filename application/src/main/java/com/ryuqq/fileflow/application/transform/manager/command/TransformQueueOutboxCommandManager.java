package com.ryuqq.fileflow.application.transform.manager.command;

import com.ryuqq.fileflow.application.transform.port.out.command.TransformQueueOutboxPersistencePort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformQueueOutboxCommandManager {

    private final TransformQueueOutboxPersistencePort transformQueueOutboxPersistencePort;

    public TransformQueueOutboxCommandManager(
            TransformQueueOutboxPersistencePort transformQueueOutboxPersistencePort) {
        this.transformQueueOutboxPersistencePort = transformQueueOutboxPersistencePort;
    }

    @Transactional
    public void persist(TransformQueueOutbox outbox) {
        transformQueueOutboxPersistencePort.persist(outbox);
    }
}
