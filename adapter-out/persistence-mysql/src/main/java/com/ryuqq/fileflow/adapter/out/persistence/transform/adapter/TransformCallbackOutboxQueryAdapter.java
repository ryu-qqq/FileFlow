package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformCallbackOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformCallbackOutboxJpaRepository;
import com.ryuqq.fileflow.application.transform.port.out.query.TransformCallbackOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformCallbackOutboxQueryAdapter implements TransformCallbackOutboxQueryPort {

    private final TransformCallbackOutboxJpaRepository transformCallbackOutboxJpaRepository;
    private final TransformCallbackOutboxJpaMapper transformCallbackOutboxJpaMapper;

    public TransformCallbackOutboxQueryAdapter(
            TransformCallbackOutboxJpaRepository transformCallbackOutboxJpaRepository,
            TransformCallbackOutboxJpaMapper transformCallbackOutboxJpaMapper) {
        this.transformCallbackOutboxJpaRepository = transformCallbackOutboxJpaRepository;
        this.transformCallbackOutboxJpaMapper = transformCallbackOutboxJpaMapper;
    }

    @Override
    public List<TransformCallbackOutbox> findPendingMessages(int limit) {
        return transformCallbackOutboxJpaRepository
                .findByOutboxStatusOrderByCreatedAtAsc(
                        OutboxStatus.PENDING, PageRequest.of(0, limit))
                .stream()
                .map(transformCallbackOutboxJpaMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public List<TransformCallbackOutbox> claimPendingMessages(int limit) {
        Instant now = Instant.now();
        int claimed = transformCallbackOutboxJpaRepository.claimPending(limit, now);
        if (claimed == 0) {
            return List.of();
        }
        return transformCallbackOutboxJpaRepository.findByStatus(OutboxStatus.PROCESSING).stream()
                .map(transformCallbackOutboxJpaMapper::toDomain)
                .toList();
    }
}
