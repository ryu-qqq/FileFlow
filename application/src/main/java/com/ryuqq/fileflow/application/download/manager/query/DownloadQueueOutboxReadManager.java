package com.ryuqq.fileflow.application.download.manager.query;

import com.ryuqq.fileflow.application.download.port.out.query.DownloadQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DownloadQueueOutboxReadManager {

    private final DownloadQueueOutboxQueryPort downloadQueueOutboxQueryPort;

    public DownloadQueueOutboxReadManager(
            DownloadQueueOutboxQueryPort downloadQueueOutboxQueryPort) {
        this.downloadQueueOutboxQueryPort = downloadQueueOutboxQueryPort;
    }

    @Transactional(readOnly = true)
    public List<DownloadQueueOutbox> findPendingMessages(int limit) {
        return downloadQueueOutboxQueryPort.findPendingMessages(limit);
    }

    @Transactional(readOnly = true)
    public OutboxStatusCount countGroupByStatus(DateRange dateRange) {
        return downloadQueueOutboxQueryPort.countGroupByStatus(dateRange);
    }
}
