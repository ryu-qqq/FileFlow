package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformQueueOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformQueueOutboxQueryDslRepository;
import com.ryuqq.fileflow.application.transform.port.out.query.TransformQueueOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformQueueOutboxQueryAdapter implements TransformQueueOutboxQueryPort {

    private final TransformQueueOutboxQueryDslRepository queryDslRepository;
    private final TransformQueueOutboxJpaMapper mapper;

    public TransformQueueOutboxQueryAdapter(
            TransformQueueOutboxQueryDslRepository queryDslRepository,
            TransformQueueOutboxJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public List<TransformQueueOutbox> findPendingMessages(int limit) {
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
    public List<TransformQueueOutbox> claimPendingMessages(int limit) {
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
