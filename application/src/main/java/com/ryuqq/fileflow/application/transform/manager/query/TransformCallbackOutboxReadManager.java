package com.ryuqq.fileflow.application.transform.manager.query;

import com.ryuqq.fileflow.application.transform.port.out.query.TransformCallbackOutboxQueryPort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformCallbackOutboxReadManager {

    private final TransformCallbackOutboxQueryPort transformCallbackOutboxQueryPort;

    public TransformCallbackOutboxReadManager(
            TransformCallbackOutboxQueryPort transformCallbackOutboxQueryPort) {
        this.transformCallbackOutboxQueryPort = transformCallbackOutboxQueryPort;
    }

    @Transactional(readOnly = true)
    public List<TransformCallbackOutbox> findPendingMessages(int limit) {
        return transformCallbackOutboxQueryPort.findPendingMessages(limit);
    }
}
