package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.ExtractedDataJpaEntity;
import com.ryuqq.fileflow.domain.file.extraction.ExtractedData;
import com.ryuqq.fileflow.domain.file.extraction.ExtractedDataId;

/**
 * ExtractedData Entity Mapper
 *
 * <p><strong>역할</strong>: Domain {@code ExtractedData} ↔ Persistence {@code ExtractedDataJpaEntity} 변환</p>
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
public final class ExtractedDataEntityMapper {

    /**
     * Private Constructor - Utility 클래스 인스턴스화 방지
     */
    private ExtractedDataEntityMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Domain → Entity 변환
     *
     * <p>신규 생성 시 사용 (ID 없음)</p>
     *
     * @param extractedData Domain ExtractedData
     * @return JPA Entity
     */
    public static ExtractedDataJpaEntity toEntity(ExtractedData extractedData) {
        if (extractedData == null) {
            return null;
        }

        return ExtractedDataJpaEntity.create(
            extractedData.getExtractedUuid(),
            extractedData.getFileId(),
            extractedData.getTenantId(),
            extractedData.getOrganizationId(),
            extractedData.getExtractionType(),
            extractedData.getExtractionMethod(),
            extractedData.getVersion(),
            extractedData.getTraceId(),
            extractedData.getTextData(),
            extractedData.getStructuredData(),
            extractedData.getPreviewData(),
            extractedData.getConfidenceScore(),
            extractedData.getQualityScore(),
            extractedData.getValidationStatus(),
            extractedData.getNotes(),
            extractedData.getExtractedAt()
        );
    }

    /**
     * Entity → Domain 변환
     *
     * <p>DB 조회 시 사용 (ID 있음)</p>
     *
     * @param entity JPA Entity
     * @return Domain ExtractedData
     */
    public static ExtractedData toDomain(ExtractedDataJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return ExtractedData.reconstitute(
            new ExtractedDataId(entity.getId()),
            entity.getExtractedUuid(),
            entity.getFileId(),
            entity.getTenantId(),
            entity.getOrganizationId(),
            entity.getExtractionType(),
            entity.getExtractionMethod(),
            entity.getVersion(),
            entity.getTraceId(),
            entity.getTextData(),
            entity.getStructuredData(),
            entity.getPreviewData(),
            entity.getConfidenceScore(),
            entity.getQualityScore(),
            entity.getValidationStatus(),
            entity.getNotes(),
            entity.getExtractedAt(),
            entity.getCreatedAt()
        );
    }
}
