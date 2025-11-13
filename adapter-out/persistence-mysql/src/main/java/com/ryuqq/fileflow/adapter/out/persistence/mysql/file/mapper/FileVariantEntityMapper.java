package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileVariantJpaEntity;
import com.ryuqq.fileflow.domain.file.variant.FileVariant;
import com.ryuqq.fileflow.domain.file.variant.FileVariantId;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;

/**
 * FileVariant Entity Mapper
 *
 * <p><strong>역할</strong>: Domain {@code FileVariant} ↔ Persistence {@code FileVariantJpaEntity} 변환</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/mapper/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ 변환 로직만 담당 (비즈니스 로직 없음)</li>
 *   <li>✅ Domain ↔ Entity 양방향 변환</li>
 *   <li>✅ Value Object 변환 처리</li>
 *   <li>✅ Static Utility Class - 인스턴스 불필요</li>
 *   <li>❌ MapStruct 사용 안함 (Pure Java)</li>
 *   <li>❌ Lombok 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class FileVariantEntityMapper {

    /**
     * Private Constructor - Utility 클래스 인스턴스화 방지
     */
    private FileVariantEntityMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Domain → Entity 변환
     *
     * <p>신규 생성 시 사용 (ID 없음)</p>
     *
     * @param fileVariant Domain FileVariant
     * @return JPA Entity
     */
    public static FileVariantJpaEntity toEntity(FileVariant fileVariant) {
        if (fileVariant == null) {
            return null;
        }

        return FileVariantJpaEntity.create(
            fileVariant.getParentFileAssetIdValue(),
            fileVariant.getVariantType(),
            fileVariant.getStorageKey().value(),
            fileVariant.getFileSize().bytes(),
            fileVariant.getMimeType().value()
        );
    }

    /**
     * Entity → Domain 변환
     *
     * <p>DB 조회 시 사용 (ID 있음)</p>
     *
     * @param entity JPA Entity
     * @return Domain FileVariant
     */
    public static FileVariant toDomain(FileVariantJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return FileVariant.reconstitute(
            new FileVariantId(entity.getId()),
            entity.getParentFileAssetId(),
            entity.getVariantType(),
            StorageKey.of(entity.getStorageKey()),
            FileSize.of(entity.getFileSize()),
            new MimeType(entity.getMimeType()),
            entity.getCreatedAt()
        );
    }

    /**
     * Domain → Entity 변환 (기존 Entity 업데이트용)
     *
     * <p>FileVariant는 Immutable이므로 업데이트 불필요</p>
     * <p>대신 새로운 Variant를 생성</p>
     *
     * @param fileVariant Domain FileVariant
     * @param existingEntity 기존 Entity
     * @return 업데이트된 Entity
     */
    public static FileVariantJpaEntity updateEntity(
        FileVariant fileVariant,
        FileVariantJpaEntity existingEntity
    ) {
        // FileVariant는 Immutable
        // 업데이트 대신 새로운 Variant 생성 권장
        throw new UnsupportedOperationException(
            "FileVariant는 Immutable입니다. 업데이트 대신 새로운 Variant를 생성하세요."
        );
    }
}
