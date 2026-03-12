package com.ryuqq.fileflow.application.transform.port.out.command;

import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import java.time.Instant;
import java.util.List;

public interface TransformQueueOutboxPersistencePort {

    void persist(TransformQueueOutbox outbox);

    void bulkMarkSent(List<String> ids, Instant now);

    void bulkMarkFailed(List<String> ids, Instant now, String lastError);

    int recoverStuckProcessing(Instant cutoff);
}
