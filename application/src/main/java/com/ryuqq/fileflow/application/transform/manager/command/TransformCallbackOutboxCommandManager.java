package com.ryuqq.fileflow.application.transform.manager.command;

import com.ryuqq.fileflow.application.transform.port.out.command.TransformCallbackOutboxPersistencePort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import org.springframework.stereotype.Component;

@Component
public class TransformCallbackOutboxCommandManager {

    private final TransformCallbackOutboxPersistencePort transformCallbackOutboxPersistencePort;

    public TransformCallbackOutboxCommandManager(
            TransformCallbackOutboxPersistencePort transformCallbackOutboxPersistencePort) {
        this.transformCallbackOutboxPersistencePort = transformCallbackOutboxPersistencePort;
    }

    public void persist(TransformCallbackOutbox outbox) {
        transformCallbackOutboxPersistencePort.persist(outbox);
    }
}
