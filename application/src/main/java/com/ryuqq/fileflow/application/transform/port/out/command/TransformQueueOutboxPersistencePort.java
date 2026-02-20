package com.ryuqq.fileflow.application.transform.port.out.command;

import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;

public interface TransformQueueOutboxPersistencePort {

    void persist(TransformQueueOutbox outbox);
}
