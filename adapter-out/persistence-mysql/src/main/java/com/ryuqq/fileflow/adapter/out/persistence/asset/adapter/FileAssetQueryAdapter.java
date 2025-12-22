package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.FileAssetJpaEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.FileAssetQueryDslRepository;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetCriteria;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * FileAsset Query Adapter.
 *
 * <p>FileAssetQueryPort 구현체로, Entity를 조회하고 Domain으로 변환합니다.
 */
@Component
public class FileAssetQueryAdapter implements FileAssetQueryPort {

    private final FileAssetQueryDslRepository fileAssetQueryDslRepository;
    private final FileAssetJpaEntityMapper fileAssetJpaEntityMapper;

    public FileAssetQueryAdapter(
            FileAssetQueryDslRepository fileAssetQueryDslRepository,
            FileAssetJpaEntityMapper fileAssetJpaEntityMapper) {
        this.fileAssetQueryDslRepository = fileAssetQueryDslRepository;
        this.fileAssetJpaEntityMapper = fileAssetJpaEntityMapper;
    }

    @Override
    public Optional<FileAsset> findById(FileAssetId id, String organizationId, String tenantId) {
        return fileAssetQueryDslRepository
                .findById(id.getValue(), organizationId, tenantId)
                .map(fileAssetJpaEntityMapper::toDomain);
    }

    @Override
    public Optional<FileAsset> findById(FileAssetId id) {
        return fileAssetQueryDslRepository
                .findById(id.getValue())
                .map(fileAssetJpaEntityMapper::toDomain);
    }

    @Override
    public List<FileAsset> findByCriteria(FileAssetCriteria criteria) {
        List<FileAssetJpaEntity> entities =
                fileAssetQueryDslRepository.findByCriteria(
                        criteria.organizationId(),
                        criteria.tenantId(),
                        criteria.status(),
                        criteria.category(),
                        criteria.fileName(),
                        criteria.createdAtFrom(),
                        criteria.createdAtTo(),
                        criteria.sortBy(),
                        criteria.isAscending(),
                        criteria.offset(),
                        criteria.limit());

        return entities.stream().map(fileAssetJpaEntityMapper::toDomain).toList();
    }

    @Override
    public long countByCriteria(FileAssetCriteria criteria) {
        return fileAssetQueryDslRepository.countByCriteria(
                criteria.organizationId(),
                criteria.tenantId(),
                criteria.status(),
                criteria.category(),
                criteria.fileName(),
                criteria.createdAtFrom(),
                criteria.createdAtTo());
    }

    @Override
    public Map<String, Long> countByStatus(String organizationId, String tenantId) {
        return fileAssetQueryDslRepository.countByStatus(organizationId, tenantId);
    }

    @Override
    public Map<String, Long> countByCategory(String organizationId, String tenantId) {
        return fileAssetQueryDslRepository.countByCategory(organizationId, tenantId);
    }

    @Override
    public long countTotal(String organizationId, String tenantId) {
        return fileAssetQueryDslRepository.countTotal(organizationId, tenantId);
    }
}
