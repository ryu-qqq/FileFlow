package com.ryuqq.fileflow.application.download.manager.command;

import com.ryuqq.fileflow.application.download.port.out.command.CallbackOutboxPersistencePort;
import com.ryuqq.fileflow.application.download.port.out.query.CallbackOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CallbackOutboxCommandManager {

    private final CallbackOutboxPersistencePort callbackOutboxPersistencePort;
    private final CallbackOutboxQueryPort callbackOutboxQueryPort;

    public CallbackOutboxCommandManager(
            CallbackOutboxPersistencePort callbackOutboxPersistencePort,
            CallbackOutboxQueryPort callbackOutboxQueryPort) {
        this.callbackOutboxPersistencePort = callbackOutboxPersistencePort;
        this.callbackOutboxQueryPort = callbackOutboxQueryPort;
    }

    @Transactional
    public void persist(CallbackOutbox callbackOutbox) {
        callbackOutboxPersistencePort.persist(callbackOutbox);
    }

    @Transactional
    public List<CallbackOutbox> claimPendingMessages(int limit) {
        return callbackOutboxQueryPort.claimPendingMessages(limit);
    }

    @Transactional
    public void bulkMarkSent(List<String> ids, Instant now) {
        callbackOutboxPersistencePort.bulkMarkSent(ids, now);
    }

    @Transactional
    public void bulkMarkFailed(List<String> ids, Instant now, String lastError) {
        callbackOutboxPersistencePort.bulkMarkFailed(ids, now, lastError);
    }

    @Transactional
    public int recoverStuckProcessing(Instant cutoff) {
        return callbackOutboxPersistencePort.recoverStuckProcessing(cutoff);
    }
}
