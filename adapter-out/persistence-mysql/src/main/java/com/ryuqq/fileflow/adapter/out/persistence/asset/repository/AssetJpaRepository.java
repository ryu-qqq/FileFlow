package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetJpaRepository extends JpaRepository<AssetJpaEntity, String> {}
