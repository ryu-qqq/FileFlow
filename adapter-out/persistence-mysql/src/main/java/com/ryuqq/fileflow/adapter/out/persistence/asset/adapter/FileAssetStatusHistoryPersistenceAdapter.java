package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetStatusHistoryJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.FileAssetStatusHistoryJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.FileAssetStatusHistoryJpaRepository;
import com.ryuqq.fileflow.application.asset.port.out.command.FileAssetStatusHistoryPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatusHistoryId;
import org.springframework.stereotype.Component;

/**
 * FileAssetStatusHistory 영속화 어댑터.
 *
 * <p>FileAssetStatusHistoryPersistencePort 구현체입니다.
 */
@Component
public class FileAssetStatusHistoryPersistenceAdapter
        implements FileAssetStatusHistoryPersistencePort {

    private final FileAssetStatusHistoryJpaRepository repository;
    private final FileAssetStatusHistoryJpaMapper mapper;

    public FileAssetStatusHistoryPersistenceAdapter(
            FileAssetStatusHistoryJpaRepository repository,
            FileAssetStatusHistoryJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public FileAssetStatusHistoryId persist(FileAssetStatusHistory history) {
        FileAssetStatusHistoryJpaEntity entity = mapper.toEntity(history);
        repository.save(entity);
        return history.getId();
    }
}
