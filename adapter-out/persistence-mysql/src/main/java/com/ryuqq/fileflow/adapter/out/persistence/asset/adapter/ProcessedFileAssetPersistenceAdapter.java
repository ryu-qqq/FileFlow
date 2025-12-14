package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.ProcessedFileAssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.ProcessedFileAssetJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.ProcessedFileAssetJpaRepository;
import com.ryuqq.fileflow.application.asset.port.out.command.ProcessedFileAssetPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedFileAssetId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * ProcessedFileAsset 영속화 어댑터.
 *
 * <p>ProcessedFileAssetPersistencePort 구현체입니다.
 */
@Component
public class ProcessedFileAssetPersistenceAdapter implements ProcessedFileAssetPersistencePort {

    private final ProcessedFileAssetJpaRepository repository;
    private final ProcessedFileAssetJpaMapper mapper;

    public ProcessedFileAssetPersistenceAdapter(
            ProcessedFileAssetJpaRepository repository, ProcessedFileAssetJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ProcessedFileAssetId persist(ProcessedFileAsset processedFileAsset) {
        ProcessedFileAssetJpaEntity entity = mapper.toEntity(processedFileAsset);
        repository.save(entity);
        return processedFileAsset.getId();
    }

    @Override
    public List<ProcessedFileAssetId> persistAll(List<ProcessedFileAsset> processedFileAssets) {
        List<ProcessedFileAssetJpaEntity> entities =
                processedFileAssets.stream().map(mapper::toEntity).toList();
        repository.saveAll(entities);
        return processedFileAssets.stream().map(ProcessedFileAsset::getId).toList();
    }
}
