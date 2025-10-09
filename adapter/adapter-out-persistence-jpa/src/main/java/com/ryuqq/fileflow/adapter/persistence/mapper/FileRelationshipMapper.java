package com.ryuqq.fileflow.adapter.persistence.mapper;

import com.ryuqq.fileflow.adapter.persistence.entity.FileRelationshipEntity;
import com.ryuqq.fileflow.domain.file.FileRelationship;
import com.ryuqq.fileflow.domain.file.FileRelationshipType;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.springframework.stereotype.Component;

/**
 * FileRelationship Entity ↔ Domain 양방향 Mapper
 *
 * 변환 규칙:
 * - FileId: Domain VO ↔ Entity String (UUID)
 * - FileRelationshipType: Domain Enum ↔ Entity Enum (동일한 이름 매핑)
 * - relationshipMetadata: Map<String, Object> 직접 전달 (JSON 처리는 JPA가 자동 처리)
 * - Domain의 reconstitute() 메서드로 불변 객체 재구성
 *
 * @author sangwon-ryu
 */
@Component
public class FileRelationshipMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain FileRelationship 도메인 객체
     * @return FileRelationshipEntity
     */
    public FileRelationshipEntity toEntity(FileRelationship domain) {
        if (domain == null) {
            return null;
        }

        return FileRelationshipEntity.of(
                domain.getSourceFileId().value(),
                domain.getTargetFileId().value(),
                toEntityType(domain.getRelationshipType()),
                domain.getRelationshipMetadata()
        );
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity FileRelationshipEntity
     * @return FileRelationship 도메인 객체
     */
    public FileRelationship toDomain(FileRelationshipEntity entity) {
        if (entity == null) {
            return null;
        }

        FileId sourceFileId = FileId.of(entity.getSourceFileId());
        FileId targetFileId = FileId.of(entity.getTargetFileId());
        FileRelationshipType relationshipType = toDomainType(entity.getRelationshipType());

        return FileRelationship.reconstitute(
                entity.getId(),
                sourceFileId,
                targetFileId,
                relationshipType,
                entity.getRelationshipMetadata(),
                entity.getCreatedAt()
        );
    }

    /**
     * Domain FileRelationshipType → Entity FileRelationshipTypeEntity 변환
     *
     * @param domainType Domain enum
     * @return Entity enum
     * @throws IllegalArgumentException if domainType is null
     */
    private FileRelationshipEntity.FileRelationshipTypeEntity toEntityType(FileRelationshipType domainType) {
        if (domainType == null) {
            throw new IllegalArgumentException("Domain relationship type cannot be null");
        }

        return switch (domainType) {
            case THUMBNAIL -> FileRelationshipEntity.FileRelationshipTypeEntity.THUMBNAIL;
            case OPTIMIZED -> FileRelationshipEntity.FileRelationshipTypeEntity.OPTIMIZED;
            case CONVERTED -> FileRelationshipEntity.FileRelationshipTypeEntity.CONVERTED;
            case DERIVATIVE -> FileRelationshipEntity.FileRelationshipTypeEntity.DERIVATIVE;
            case VERSION -> FileRelationshipEntity.FileRelationshipTypeEntity.VERSION;
        };
    }

    /**
     * Entity FileRelationshipTypeEntity → Domain FileRelationshipType 변환
     *
     * @param entityType Entity enum
     * @return Domain enum
     * @throws IllegalArgumentException if entityType is null
     */
    private FileRelationshipType toDomainType(FileRelationshipEntity.FileRelationshipTypeEntity entityType) {
        if (entityType == null) {
            throw new IllegalArgumentException("Entity relationship type cannot be null");
        }

        return switch (entityType) {
            case THUMBNAIL -> FileRelationshipType.THUMBNAIL;
            case OPTIMIZED -> FileRelationshipType.OPTIMIZED;
            case CONVERTED -> FileRelationshipType.CONVERTED;
            case DERIVATIVE -> FileRelationshipType.DERIVATIVE;
            case VERSION -> FileRelationshipType.VERSION;
        };
    }
}
