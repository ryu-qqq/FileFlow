package com.ryuqq.fileflow.application.transform.port.out.command;

import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;

public interface TransformCallbackOutboxPersistencePort {

    void persist(TransformCallbackOutbox outbox);
}
