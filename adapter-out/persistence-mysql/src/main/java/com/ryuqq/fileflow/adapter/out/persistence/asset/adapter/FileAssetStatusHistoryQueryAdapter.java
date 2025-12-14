package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.FileAssetStatusHistoryJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.FileAssetStatusHistoryJpaRepository;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetStatusHistoryQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * FileAssetStatusHistory 조회 어댑터.
 *
 * <p>FileAssetStatusHistoryQueryPort 구현체입니다.
 */
@Component
public class FileAssetStatusHistoryQueryAdapter implements FileAssetStatusHistoryQueryPort {

    private final FileAssetStatusHistoryJpaRepository repository;
    private final FileAssetStatusHistoryJpaMapper mapper;

    public FileAssetStatusHistoryQueryAdapter(
            FileAssetStatusHistoryJpaRepository repository,
            FileAssetStatusHistoryJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<FileAssetStatusHistory> findByFileAssetId(String fileAssetId) {
        return repository.findByFileAssetIdOrderByChangedAtAsc(fileAssetId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<FileAssetStatusHistory> findLatestByFileAssetId(String fileAssetId) {
        return repository
                .findFirstByFileAssetIdOrderByChangedAtDesc(fileAssetId)
                .map(mapper::toDomain);
    }

    @Override
    public List<FileAssetStatusHistory> findExceedingSla(long slaMillis, int limit) {
        return repository.findExceedingSla(slaMillis, limit).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
