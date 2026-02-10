package com.ryuqq.fileflow.application.download.manager.command;

import com.ryuqq.fileflow.application.download.port.out.command.CallbackOutboxPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CallbackOutboxCommandManager {

    private final CallbackOutboxPersistencePort callbackOutboxPersistencePort;

    public CallbackOutboxCommandManager(
            CallbackOutboxPersistencePort callbackOutboxPersistencePort) {
        this.callbackOutboxPersistencePort = callbackOutboxPersistencePort;
    }

    @Transactional
    public void persist(CallbackOutbox callbackOutbox) {
        callbackOutboxPersistencePort.persist(callbackOutbox);
    }
}
