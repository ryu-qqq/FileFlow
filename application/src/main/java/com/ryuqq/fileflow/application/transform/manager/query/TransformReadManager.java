package com.ryuqq.fileflow.application.transform.manager.query;

import com.ryuqq.fileflow.application.transform.port.out.query.TransformRequestQueryPort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.exception.TransformRequestNotFoundException;
import com.ryuqq.fileflow.domain.transform.id.TransformRequestId;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformReadManager {

    private final TransformRequestQueryPort queryPort;

    public TransformReadManager(TransformRequestQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Transactional(readOnly = true)
    public TransformRequest getTransformRequest(String transformRequestId) {
        return queryPort
                .findById(TransformRequestId.of(transformRequestId))
                .orElseThrow(() -> new TransformRequestNotFoundException(transformRequestId));
    }

    @Transactional(readOnly = true)
    public List<TransformRequest> getStaleQueuedRequests(Instant createdBefore, int limit) {
        return queryPort.findByStatusAndCreatedBefore(TransformStatus.QUEUED, createdBefore, limit);
    }
}
