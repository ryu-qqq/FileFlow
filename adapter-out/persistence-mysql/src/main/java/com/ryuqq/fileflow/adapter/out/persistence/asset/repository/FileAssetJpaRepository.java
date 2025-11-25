package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * FileAsset JPA Repository.
 *
 * <p>FileAsset Entity의 기본 CRUD 연산을 제공합니다.
 */
public interface FileAssetJpaRepository extends JpaRepository<FileAssetJpaEntity, String> {}
