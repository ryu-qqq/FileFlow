package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetStatusHistoryJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * FileAssetStatusHistory JPA Repository.
 *
 * <p>Append-Only 테이블로 save만 사용합니다.
 */
public interface FileAssetStatusHistoryJpaRepository
        extends JpaRepository<FileAssetStatusHistoryJpaEntity, UUID> {}
