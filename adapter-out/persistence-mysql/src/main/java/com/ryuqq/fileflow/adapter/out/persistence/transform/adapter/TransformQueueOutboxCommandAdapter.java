package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformQueueOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformQueueOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformQueueOutboxJpaRepository;
import com.ryuqq.fileflow.application.transform.port.out.command.TransformQueueOutboxPersistencePort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
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
}
