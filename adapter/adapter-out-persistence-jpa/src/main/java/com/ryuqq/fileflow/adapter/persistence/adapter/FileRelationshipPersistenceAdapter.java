package com.ryuqq.fileflow.adapter.persistence.adapter;

import com.ryuqq.fileflow.adapter.persistence.entity.FileRelationshipEntity;
import com.ryuqq.fileflow.adapter.persistence.mapper.FileRelationshipMapper;
import com.ryuqq.fileflow.adapter.persistence.repository.FileRelationshipJpaRepository;
import com.ryuqq.fileflow.application.file.port.out.SaveFileRelationshipPort;
import com.ryuqq.fileflow.domain.file.FileRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * FileRelationship Persistence Adapter
 *
 * Hexagonal Architecture의 Outbound Adapter로서,
 * FileRelationship을 JPA를 통해 영구 저장소에 저장합니다.
 *
 * 역할:
 * - Domain FileRelationship → Entity 변환
 * - JPA Repository를 통한 저장
 * - Entity → Domain FileRelationship 변환
 *
 * 트랜잭션:
 * - 트랜잭션은 Application Layer의 UseCase에서 관리됩니다
 * - Adapter는 순수한 데이터 접근 계층으로 동작합니다
 * - saveAll은 배치 삽입으로 성능 최적화
 *
 * @author sangwon-ryu
 */
@Component
public class FileRelationshipPersistenceAdapter implements SaveFileRelationshipPort {

    private static final Logger logger = LoggerFactory.getLogger(FileRelationshipPersistenceAdapter.class);

    private final FileRelationshipJpaRepository fileRelationshipRepository;
    private final FileRelationshipMapper fileRelationshipMapper;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param fileRelationshipRepository JPA Repository
     * @param fileRelationshipMapper Entity-Domain 양방향 Mapper
     */
    public FileRelationshipPersistenceAdapter(
            FileRelationshipJpaRepository fileRelationshipRepository,
            FileRelationshipMapper fileRelationshipMapper
    ) {
        this.fileRelationshipRepository = Objects.requireNonNull(
                fileRelationshipRepository, "FileRelationshipJpaRepository must not be null"
        );
        this.fileRelationshipMapper = Objects.requireNonNull(
                fileRelationshipMapper, "FileRelationshipMapper must not be null"
        );
    }

    @Override
    public FileRelationship save(FileRelationship fileRelationship) {
        Objects.requireNonNull(fileRelationship, "FileRelationship must not be null");

        logger.debug("Saving file relationship: {} -> {} ({})",
                fileRelationship.getSourceFileId().value(),
                fileRelationship.getTargetFileId().value(),
                fileRelationship.getRelationshipType()
        );

        // Domain → Entity 변환
        FileRelationshipEntity entity = fileRelationshipMapper.toEntity(fileRelationship);

        // JPA 저장
        FileRelationshipEntity savedEntity = fileRelationshipRepository.save(entity);

        // Entity → Domain 변환
        FileRelationship savedRelationship = fileRelationshipMapper.toDomain(savedEntity);

        logger.info("Saved file relationship with ID: {}", savedEntity.getId());

        return savedRelationship;
    }

    @Override
    public List<FileRelationship> saveAll(List<FileRelationship> fileRelationships) {
        if (fileRelationships == null || fileRelationships.isEmpty()) {
            throw new IllegalArgumentException("FileRelationships list cannot be null or empty");
        }

        logger.debug("Batch saving {} file relationships", fileRelationships.size());

        // Domain → Entity 변환
        List<FileRelationshipEntity> entities = fileRelationships.stream()
                .map(fileRelationshipMapper::toEntity)
                .collect(Collectors.toList());

        // 배치 저장
        List<FileRelationshipEntity> savedEntities = fileRelationshipRepository.saveAll(entities);

        // Entity → Domain 변환
        List<FileRelationship> savedRelationships = savedEntities.stream()
                .map(fileRelationshipMapper::toDomain)
                .collect(Collectors.toList());

        logger.info("Batch saved {} file relationships", savedRelationships.size());

        return savedRelationships;
    }
}
