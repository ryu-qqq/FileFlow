package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadQueueOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.DownloadQueueOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadQueueOutboxJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.command.DownloadQueueOutboxPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import org.springframework.stereotype.Component;

@Component
public class DownloadQueueOutboxCommandAdapter implements DownloadQueueOutboxPersistencePort {

    private final DownloadQueueOutboxJpaRepository jpaRepository;
    private final DownloadQueueOutboxJpaMapper mapper;

    public DownloadQueueOutboxCommandAdapter(
            DownloadQueueOutboxJpaRepository jpaRepository, DownloadQueueOutboxJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persist(DownloadQueueOutbox outbox) {
        DownloadQueueOutboxJpaEntity entity = mapper.toEntity(outbox);
        jpaRepository.save(entity);
    }
}
