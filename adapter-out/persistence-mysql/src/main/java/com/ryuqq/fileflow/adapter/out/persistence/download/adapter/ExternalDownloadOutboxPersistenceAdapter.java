package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.ExternalDownloadOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.ExternalDownloadOutboxJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.command.ExternalDownloadOutboxPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import org.springframework.stereotype.Repository;

/**
 * ExternalDownloadOutbox Persistence Adapter.
 *
 * <p>ExternalDownloadOutbox의 저장/수정을 담당합니다.
 */
@Repository
public class ExternalDownloadOutboxPersistenceAdapter
        implements ExternalDownloadOutboxPersistencePort {

    private final ExternalDownloadOutboxJpaRepository jpaRepository;
    private final ExternalDownloadOutboxJpaMapper mapper;

    public ExternalDownloadOutboxPersistenceAdapter(
            ExternalDownloadOutboxJpaRepository jpaRepository,
            ExternalDownloadOutboxJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ExternalDownloadOutboxId persist(ExternalDownloadOutbox outbox) {
        ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(outbox);
        ExternalDownloadOutboxJpaEntity savedEntity = jpaRepository.save(entity);
        return ExternalDownloadOutboxId.of(savedEntity.getId());
    }
}
