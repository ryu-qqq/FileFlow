package com.ryuqq.fileflow.application.monitoring.service.query;

import com.ryuqq.fileflow.application.download.manager.query.DownloadQueueOutboxReadManager;
import com.ryuqq.fileflow.application.monitoring.assembler.OutboxStatusAssembler;
import com.ryuqq.fileflow.application.monitoring.dto.query.OutboxStatusSearchParams;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxStatusResponse;
import com.ryuqq.fileflow.application.monitoring.port.in.query.GetOutboxStatusUseCase;
import com.ryuqq.fileflow.application.transform.manager.query.TransformQueueOutboxReadManager;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import org.springframework.stereotype.Service;

@Service
public class GetOutboxStatusService implements GetOutboxStatusUseCase {

    private final DownloadQueueOutboxReadManager downloadReadManager;
    private final TransformQueueOutboxReadManager transformReadManager;
    private final OutboxStatusAssembler outboxStatusAssembler;

    public GetOutboxStatusService(
            DownloadQueueOutboxReadManager downloadReadManager,
            TransformQueueOutboxReadManager transformReadManager,
            OutboxStatusAssembler outboxStatusAssembler) {
        this.downloadReadManager = downloadReadManager;
        this.transformReadManager = transformReadManager;
        this.outboxStatusAssembler = outboxStatusAssembler;
    }

    @Override
    public OutboxStatusResponse execute(OutboxStatusSearchParams params) {
        DateRange dateRange = params.dateRange();
        OutboxStatusCount downloadCount = downloadReadManager.countGroupByStatus(dateRange);
        OutboxStatusCount transformCount = transformReadManager.countGroupByStatus(dateRange);
        return outboxStatusAssembler.toResponse(downloadCount, transformCount);
    }
}
