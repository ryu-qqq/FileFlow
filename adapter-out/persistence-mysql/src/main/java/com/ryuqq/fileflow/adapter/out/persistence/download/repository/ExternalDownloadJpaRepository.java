package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** ExternalDownload JPA Repository. */
public interface ExternalDownloadJpaRepository
        extends JpaRepository<ExternalDownloadJpaEntity, UUID> {}
