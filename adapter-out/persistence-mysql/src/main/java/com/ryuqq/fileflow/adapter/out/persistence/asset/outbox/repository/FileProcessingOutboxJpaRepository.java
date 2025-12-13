package com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.repository;

import com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.entity.FileProcessingOutboxJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** FileProcessingOutbox JPA Repository. */
public interface FileProcessingOutboxJpaRepository
        extends JpaRepository<FileProcessingOutboxJpaEntity, UUID> {}
