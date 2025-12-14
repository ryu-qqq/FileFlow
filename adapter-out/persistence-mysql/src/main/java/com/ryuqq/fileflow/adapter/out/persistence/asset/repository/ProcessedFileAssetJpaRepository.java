package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.ProcessedFileAssetJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ProcessedFileAsset JPA Repository.
 */
public interface ProcessedFileAssetJpaRepository
        extends JpaRepository<ProcessedFileAssetJpaEntity, UUID> {}
