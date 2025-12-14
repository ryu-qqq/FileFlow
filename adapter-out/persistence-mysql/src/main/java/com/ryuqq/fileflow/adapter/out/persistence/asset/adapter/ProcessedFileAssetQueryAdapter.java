package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.ProcessedFileAssetJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.ProcessedFileAssetQueryDslRepository;
import com.ryuqq.fileflow.application.asset.port.out.query.ProcessedFileAssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * ProcessedFileAsset 조회 어댑터.
 *
 * <p>ProcessedFileAssetQueryPort 구현체입니다.
 */
@Component
public class ProcessedFileAssetQueryAdapter implements ProcessedFileAssetQueryPort {

    private final ProcessedFileAssetQueryDslRepository queryDslRepository;
    private final ProcessedFileAssetJpaMapper mapper;

    public ProcessedFileAssetQueryAdapter(
            ProcessedFileAssetQueryDslRepository queryDslRepository,
            ProcessedFileAssetJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ProcessedFileAsset> findByOriginalAssetId(String originalAssetId) {
        return queryDslRepository.findByOriginalAssetId(originalAssetId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ProcessedFileAsset> findByParentAssetId(String parentAssetId) {
        return queryDslRepository.findByParentAssetId(parentAssetId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
