package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** ExternalDownloadOutbox JPA Repository. */
public interface ExternalDownloadOutboxJpaRepository
        extends JpaRepository<ExternalDownloadOutboxJpaEntity, Long> {}
