package com.ryuqq.fileflow.application.transform.manager.query;

import com.ryuqq.fileflow.application.transform.port.out.query.TransformQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformQueueOutboxReadManager {

    private final TransformQueueOutboxQueryPort transformQueueOutboxQueryPort;

    public TransformQueueOutboxReadManager(
            TransformQueueOutboxQueryPort transformQueueOutboxQueryPort) {
        this.transformQueueOutboxQueryPort = transformQueueOutboxQueryPort;
    }

    @Transactional(readOnly = true)
    public List<TransformQueueOutbox> findPendingMessages(int limit) {
        return transformQueueOutboxQueryPort.findPendingMessages(limit);
    }

    @Transactional(readOnly = true)
    public OutboxStatusCount countGroupByStatus(DateRange dateRange) {
        return transformQueueOutboxQueryPort.countGroupByStatus(dateRange);
    }
}
