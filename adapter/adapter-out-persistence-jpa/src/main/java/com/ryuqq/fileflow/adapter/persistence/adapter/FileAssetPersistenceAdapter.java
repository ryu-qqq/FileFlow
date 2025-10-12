package com.ryuqq.fileflow.adapter.persistence.adapter;

import com.ryuqq.fileflow.adapter.persistence.entity.FileAssetEntity;
import com.ryuqq.fileflow.adapter.persistence.mapper.FileAssetMapper;
import com.ryuqq.fileflow.adapter.persistence.repository.FileAssetJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.SaveFileAssetPort;
import com.ryuqq.fileflow.domain.upload.vo.FileAsset;
import org.springframework.stereotype.Component;

/**
 * FileAsset Persistence Adapter
 *
 * Hexagonal Architecture의 Outbound Adapter로서,
 * SaveFileAssetPort 인터페이스를 구현하여 데이터베이스 영속성을 제공합니다.
 *
 * 구현 Port:
 * - SaveFileAssetPort: 파일 자산 저장
 *
 * 트랜잭션 관리:
 * - 트랜잭션은 Application Layer의 UseCase에서 관리됩니다
 * - Adapter는 순수한 데이터 접근 계층으로 동작합니다
 *
 * @author sangwon-ryu
 */
@Component
public class FileAssetPersistenceAdapter implements SaveFileAssetPort {

    private final FileAssetJpaRepository repository;
    private final FileAssetMapper mapper;

    public FileAssetPersistenceAdapter(
            FileAssetJpaRepository repository,
            FileAssetMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ========== SaveFileAssetPort Implementation ==========

    @Override
    public FileAsset save(FileAsset fileAsset) {
        if (fileAsset == null) {
            throw new IllegalArgumentException("FileAsset cannot be null");
        }

        FileAssetEntity entity = mapper.toEntity(fileAsset);
        FileAssetEntity savedEntity = repository.save(entity);

        return mapper.toDomain(savedEntity);
    }
}
