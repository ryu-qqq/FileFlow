package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.FileAssetJpaEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.FileAssetQueryRepository;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetCriteria;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * FileAsset Query Adapter.
 *
 * <p>FileAssetQueryPort 구현체로, Entity를 조회하고 Domain으로 변환합니다.
 */
@Component
public class FileAssetQueryAdapter implements FileAssetQueryPort {

    private final FileAssetQueryRepository fileAssetQueryRepository;
    private final FileAssetJpaEntityMapper fileAssetJpaEntityMapper;

    public FileAssetQueryAdapter(
            FileAssetQueryRepository fileAssetQueryRepository,
            FileAssetJpaEntityMapper fileAssetJpaEntityMapper) {
        this.fileAssetQueryRepository = fileAssetQueryRepository;
        this.fileAssetJpaEntityMapper = fileAssetJpaEntityMapper;
    }

    @Override
    public Optional<FileAsset> findById(FileAssetId id, Long organizationId, Long tenantId) {
        return fileAssetQueryRepository
                .findById(id.getValue(), organizationId, tenantId)
                .map(fileAssetJpaEntityMapper::toDomain);
    }

    @Override
    public List<FileAsset> findByCriteria(FileAssetCriteria criteria) {
        List<FileAssetJpaEntity> entities =
                fileAssetQueryRepository.findAll(
                        criteria.organizationId(),
                        criteria.tenantId(),
                        criteria.status(),
                        criteria.category(),
                        criteria.offset(),
                        criteria.limit());

        return entities.stream().map(fileAssetJpaEntityMapper::toDomain).toList();
    }

    @Override
    public long countByCriteria(FileAssetCriteria criteria) {
        return fileAssetQueryRepository.count(
                criteria.organizationId(),
                criteria.tenantId(),
                criteria.status(),
                criteria.category());
    }
}
