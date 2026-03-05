package com.ryuqq.fileflow.application.download.manager.query;

import com.ryuqq.fileflow.application.download.port.out.query.CallbackOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CallbackOutboxReadManager {

    private final CallbackOutboxQueryPort callbackOutboxQueryPort;

    public CallbackOutboxReadManager(CallbackOutboxQueryPort callbackOutboxQueryPort) {
        this.callbackOutboxQueryPort = callbackOutboxQueryPort;
    }

    @Transactional(readOnly = true)
    public List<CallbackOutbox> findPendingMessages(int limit) {
        return callbackOutboxQueryPort.findPendingMessages(limit);
    }
}
