package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.DownloadQueueOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadQueueOutboxJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.query.DownloadQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class DownloadQueueOutboxQueryAdapter implements DownloadQueueOutboxQueryPort {

    private final DownloadQueueOutboxJpaRepository jpaRepository;
    private final DownloadQueueOutboxJpaMapper mapper;

    public DownloadQueueOutboxQueryAdapter(
            DownloadQueueOutboxJpaRepository jpaRepository, DownloadQueueOutboxJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<DownloadQueueOutbox> findPendingMessages(int limit) {
        return jpaRepository
                .findByOutboxStatusOrderByCreatedAtAsc(
                        OutboxStatus.PENDING, PageRequest.of(0, limit))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public OutboxStatusCount countGroupByStatus(DateRange dateRange) {
        Instant startInstant =
                dateRange.startInstant() != null ? dateRange.startInstant() : Instant.EPOCH;
        Instant endInstant =
                dateRange.endInstant() != null ? dateRange.endInstant() : Instant.now();

        List<Object[]> rows = jpaRepository.countGroupByOutboxStatus(startInstant, endInstant);
        return toOutboxStatusCount(rows);
    }

    private OutboxStatusCount toOutboxStatusCount(List<Object[]> rows) {
        long pending = 0;
        long sent = 0;
        long failed = 0;
        for (Object[] row : rows) {
            OutboxStatus status = (OutboxStatus) row[0];
            long count = (Long) row[1];
            switch (status) {
                case PENDING -> pending = count;
                case SENT -> sent = count;
                case FAILED -> failed = count;
            }
        }
        return new OutboxStatusCount(pending, sent, failed);
    }
}
