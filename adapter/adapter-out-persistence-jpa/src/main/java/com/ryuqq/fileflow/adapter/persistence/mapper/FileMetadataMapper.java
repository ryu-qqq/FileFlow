package com.ryuqq.fileflow.adapter.persistence.mapper;

import com.ryuqq.fileflow.adapter.persistence.entity.FileMetadataEntity;
import com.ryuqq.fileflow.domain.file.FileMetadata;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FileMetadata Entity ↔ Domain 양방향 Mapper
 *
 * 변환 규칙:
 * - FileId: Domain VO ↔ Entity String (UUID)
 * - MetadataType: Enum은 동일하므로 그대로 사용
 * - Domain의 reconstitute() 메서드로 불변 객체 재구성
 *
 * @author sangwon-ryu
 */
@Component
public class FileMetadataMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain FileMetadata 도메인 객체
     * @return FileMetadataEntity
     */
    public FileMetadataEntity toEntity(FileMetadata domain) {
        if (domain == null) {
            return null;
        }

        return FileMetadataEntity.of(
                domain.getFileId().getValue(),
                domain.getMetadataKey(),
                domain.getMetadataValue(),
                domain.getValueType()
        );
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity FileMetadataEntity
     * @return FileMetadata 도메인 객체
     */
    public FileMetadata toDomain(FileMetadataEntity entity) {
        if (entity == null) {
            return null;
        }

        FileId fileId = FileId.reconstitute(entity.getFileId());

        return FileMetadata.reconstitute(
                fileId,
                entity.getMetadataKey(),
                entity.getMetadataValue(),
                entity.getValueType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Entity 리스트 → Domain 리스트 변환
     *
     * @param entities FileMetadataEntity 리스트
     * @return FileMetadata 도메인 객체 리스트
     */
    public List<FileMetadata> toDomainList(List<FileMetadataEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Domain 리스트 → Entity 리스트 변환
     *
     * @param domains FileMetadata 도메인 객체 리스트
     * @return FileMetadataEntity 리스트
     */
    public List<FileMetadataEntity> toEntityList(List<FileMetadata> domains) {
        if (domains == null || domains.isEmpty()) {
            return List.of();
        }

        return domains.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
