package com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.entity.FileProcessingOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.mapper.FileProcessingOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.repository.FileProcessingOutboxJpaRepository;
import com.ryuqq.fileflow.application.asset.port.out.command.FileProcessingOutboxPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import org.springframework.stereotype.Component;

/**
 * FileProcessingOutbox Command Adapter.
 *
 * <p>FileProcessingOutbox의 저장/수정을 담당합니다.
 */
@Component
public class FileProcessingOutboxCommandAdapter implements FileProcessingOutboxPersistencePort {

    private final FileProcessingOutboxJpaRepository jpaRepository;
    private final FileProcessingOutboxJpaMapper mapper;

    public FileProcessingOutboxCommandAdapter(
            FileProcessingOutboxJpaRepository jpaRepository, FileProcessingOutboxJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public FileProcessingOutboxId persist(FileProcessingOutbox outbox) {
        FileProcessingOutboxJpaEntity entity = mapper.toEntity(outbox);
        FileProcessingOutboxJpaEntity savedEntity = jpaRepository.save(entity);
        return FileProcessingOutboxId.of(savedEntity.getId());
    }
}
