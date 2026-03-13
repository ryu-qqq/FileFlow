package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformCallbackOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformCallbackOutboxQueryDslRepository;
import com.ryuqq.fileflow.application.transform.port.out.query.TransformCallbackOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformCallbackOutboxQueryAdapter implements TransformCallbackOutboxQueryPort {

    private final TransformCallbackOutboxQueryDslRepository queryDslRepository;
    private final TransformCallbackOutboxJpaMapper mapper;

    public TransformCallbackOutboxQueryAdapter(
            TransformCallbackOutboxQueryDslRepository queryDslRepository,
            TransformCallbackOutboxJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public List<TransformCallbackOutbox> findPendingMessages(int limit) {
        return queryDslRepository.findPendingOrderByCreatedAtAsc(limit).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public List<TransformCallbackOutbox> claimPendingMessages(int limit) {
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
