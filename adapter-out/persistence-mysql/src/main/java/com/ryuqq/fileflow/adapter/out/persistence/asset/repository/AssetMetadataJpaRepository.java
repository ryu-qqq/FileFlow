package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetMetadataJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetMetadataJpaRepository extends JpaRepository<AssetMetadataJpaEntity, String> {}
