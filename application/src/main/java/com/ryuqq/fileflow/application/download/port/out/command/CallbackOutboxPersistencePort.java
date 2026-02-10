package com.ryuqq.fileflow.application.download.port.out.command;

import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;

public interface CallbackOutboxPersistencePort {

    void persist(CallbackOutbox callbackOutbox);
}
