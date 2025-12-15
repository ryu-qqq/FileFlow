package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.ProcessedFileAssetJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** ProcessedFileAsset JPA Repository. */
public interface ProcessedFileAssetJpaRepository
        extends JpaRepository<ProcessedFileAssetJpaEntity, UUID> {

    /**
     * 원본 FileAsset ID로 ProcessedFileAsset 목록 조회.
     *
     * @param originalAssetId 원본 FileAsset ID
     * @return ProcessedFileAssetJpaEntity 목록
     */
    List<ProcessedFileAssetJpaEntity> findByOriginalAssetId(String originalAssetId);

    /**
     * 부모 ProcessedFileAsset ID로 하위 목록 조회.
     *
     * @param parentAssetId 부모 ProcessedFileAsset ID
     * @return ProcessedFileAssetJpaEntity 목록
     */
    List<ProcessedFileAssetJpaEntity> findByParentAssetId(String parentAssetId);
}
