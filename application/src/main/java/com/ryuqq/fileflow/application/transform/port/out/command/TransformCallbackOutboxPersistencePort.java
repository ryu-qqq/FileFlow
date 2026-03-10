package com.ryuqq.fileflow.application.transform.port.out.command;

import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import java.time.Instant;
import java.util.List;

public interface TransformCallbackOutboxPersistencePort {

    void persist(TransformCallbackOutbox outbox);

    void bulkMarkSent(List<String> ids, Instant now);

    void bulkMarkFailed(List<String> ids, Instant now);
}
