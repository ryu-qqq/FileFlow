package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.FileAssetJpaEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.FileAssetJpaRepository;
import com.ryuqq.fileflow.application.asset.port.out.command.PersistFileAssetPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import org.springframework.stereotype.Component;

/**
 * FileAsset Command Adapter.
 *
 * <p>PersistFileAssetPort 구현체로, FileAsset을 영속화합니다.
 */
@Component
public class FileAssetCommandAdapter implements PersistFileAssetPort {

    private final FileAssetJpaRepository fileAssetJpaRepository;
    private final FileAssetJpaEntityMapper fileAssetJpaEntityMapper;

    public FileAssetCommandAdapter(
            FileAssetJpaRepository fileAssetJpaRepository,
            FileAssetJpaEntityMapper fileAssetJpaEntityMapper) {
        this.fileAssetJpaRepository = fileAssetJpaRepository;
        this.fileAssetJpaEntityMapper = fileAssetJpaEntityMapper;
    }

    @Override
    public FileAssetId persist(FileAsset fileAsset) {
        FileAssetJpaEntity entity = fileAssetJpaEntityMapper.toEntity(fileAsset);
        FileAssetJpaEntity savedEntity = fileAssetJpaRepository.save(entity);
        return FileAssetId.of(savedEntity.getId());
    }
}
