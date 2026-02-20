package com.ryuqq.fileflow.application.monitoring.assembler;

import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxQueueStatusResponse;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxStatusResponse;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import org.springframework.stereotype.Component;

@Component
public class OutboxStatusAssembler {

    private final TimeProvider timeProvider;

    public OutboxStatusAssembler(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public OutboxStatusResponse toResponse(
            OutboxStatusCount downloadCount, OutboxStatusCount transformCount) {
        return new OutboxStatusResponse(
                toQueueStatus(downloadCount), toQueueStatus(transformCount), timeProvider.now());
    }

    private OutboxQueueStatusResponse toQueueStatus(OutboxStatusCount count) {
        return new OutboxQueueStatusResponse(count.pending(), count.sent(), count.failed());
    }
}
