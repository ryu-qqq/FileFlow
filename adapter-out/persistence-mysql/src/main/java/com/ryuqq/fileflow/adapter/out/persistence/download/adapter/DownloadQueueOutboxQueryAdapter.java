package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.DownloadQueueOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadQueueOutboxQueryDslRepository;
import com.ryuqq.fileflow.application.download.port.out.query.DownloadQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DownloadQueueOutboxQueryAdapter implements DownloadQueueOutboxQueryPort {

    private final DownloadQueueOutboxQueryDslRepository queryDslRepository;
    private final DownloadQueueOutboxJpaMapper mapper;

    public DownloadQueueOutboxQueryAdapter(
            DownloadQueueOutboxQueryDslRepository queryDslRepository,
            DownloadQueueOutboxJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public List<DownloadQueueOutbox> findPendingMessages(int limit) {
        return queryDslRepository.findPendingOrderByCreatedAtAsc(limit).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public OutboxStatusCount countGroupByStatus(DateRange dateRange) {
        Instant startInstant =
                dateRange.startInstant() != null ? dateRange.startInstant() : Instant.EPOCH;
        Instant endInstant =
                dateRange.endInstant() != null ? dateRange.endInstant() : Instant.now();

        return queryDslRepository.countGroupByOutboxStatus(startInstant, endInstant);
    }

    @Override
    @Transactional
    public List<DownloadQueueOutbox> claimPendingMessages(int limit) {
        Instant now = Instant.now();
        int claimed = queryDslRepository.claimPending(limit, now);
        if (claimed == 0) {
            return List.of();
        }
        return queryDslRepository.findByStatusWithLock(OutboxStatus.PROCESSING).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
