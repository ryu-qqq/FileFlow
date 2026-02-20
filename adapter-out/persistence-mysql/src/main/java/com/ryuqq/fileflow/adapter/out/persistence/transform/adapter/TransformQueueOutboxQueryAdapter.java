package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformQueueOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformQueueOutboxJpaRepository;
import com.ryuqq.fileflow.application.transform.port.out.query.TransformQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class TransformQueueOutboxQueryAdapter implements TransformQueueOutboxQueryPort {

    private final TransformQueueOutboxJpaRepository jpaRepository;
    private final TransformQueueOutboxJpaMapper mapper;

    public TransformQueueOutboxQueryAdapter(
            TransformQueueOutboxJpaRepository jpaRepository, TransformQueueOutboxJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<TransformQueueOutbox> findPendingMessages(int limit) {
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
