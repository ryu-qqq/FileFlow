package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformQueueOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformQueueOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformQueueOutboxJpaRepository;
import com.ryuqq.fileflow.application.transform.port.out.command.TransformQueueOutboxPersistencePort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TransformQueueOutboxCommandAdapter implements TransformQueueOutboxPersistencePort {

    private final TransformQueueOutboxJpaRepository jpaRepository;
    private final TransformQueueOutboxJpaMapper mapper;

    public TransformQueueOutboxCommandAdapter(
            TransformQueueOutboxJpaRepository jpaRepository, TransformQueueOutboxJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persist(TransformQueueOutbox outbox) {
        TransformQueueOutboxJpaEntity entity = mapper.toEntity(outbox);
        jpaRepository.save(entity);
    }

    @Override
    public void bulkMarkSent(List<String> ids, Instant now) {
        if (ids.isEmpty()) return;
        jpaRepository.bulkMarkSent(ids, now);
    }

    @Override
    public void bulkMarkFailed(List<String> ids, Instant now, String lastError) {
        if (ids.isEmpty()) return;
        jpaRepository.bulkMarkFailed(ids, now, lastError);
    }

    @Override
    public int recoverStuckProcessing(Instant cutoff) {
        return jpaRepository.recoverStuckProcessing(cutoff);
    }
}
