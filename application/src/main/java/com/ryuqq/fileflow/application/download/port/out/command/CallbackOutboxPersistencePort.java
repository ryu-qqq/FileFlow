package com.ryuqq.fileflow.application.download.port.out.command;

import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import java.time.Instant;
import java.util.List;

public interface CallbackOutboxPersistencePort {

    void persist(CallbackOutbox callbackOutbox);

    void bulkMarkSent(List<String> ids, Instant now);

    void bulkMarkFailed(List<String> ids, Instant now);

    int recoverStuckProcessing(Instant cutoff);
}
