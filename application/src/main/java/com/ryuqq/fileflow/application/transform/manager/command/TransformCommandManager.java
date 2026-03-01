package com.ryuqq.fileflow.application.transform.manager.command;

import com.ryuqq.fileflow.application.transform.port.out.command.TransformRequestPersistencePort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformCommandManager {

    private final TransformRequestPersistencePort persistencePort;

    public TransformCommandManager(TransformRequestPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Transactional
    public void persist(TransformRequest transformRequest) {
        persistencePort.persist(transformRequest);
    }
}
