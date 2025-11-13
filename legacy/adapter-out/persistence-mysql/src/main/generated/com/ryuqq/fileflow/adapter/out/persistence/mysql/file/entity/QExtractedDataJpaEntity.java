package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QExtractedDataJpaEntity is a Querydsl query type for ExtractedDataJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExtractedDataJpaEntity extends EntityPathBase<ExtractedDataJpaEntity> {

    private static final long serialVersionUID = -473704098L;

    public static final QExtractedDataJpaEntity extractedDataJpaEntity = new QExtractedDataJpaEntity("extractedDataJpaEntity");

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity(this);

    public final NumberPath<Double> confidenceScore = createNumber("confidenceScore", Double.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> extractedAt = createDateTime("extractedAt", java.time.LocalDateTime.class);

    public final StringPath extractedUuid = createString("extractedUuid");

    public final EnumPath<com.ryuqq.fileflow.domain.file.extraction.ExtractionMethod> extractionMethod = createEnum("extractionMethod", com.ryuqq.fileflow.domain.file.extraction.ExtractionMethod.class);

    public final EnumPath<com.ryuqq.fileflow.domain.file.extraction.ExtractionType> extractionType = createEnum("extractionType", com.ryuqq.fileflow.domain.file.extraction.ExtractionType.class);

    public final NumberPath<Long> fileId = createNumber("fileId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath notes = createString("notes");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath previewData = createString("previewData");

    public final NumberPath<Double> qualityScore = createNumber("qualityScore", Double.class);

    public final StringPath structuredData = createString("structuredData");

    public final NumberPath<Long> tenantId = createNumber("tenantId", Long.class);

    public final StringPath textData = createString("textData");

    public final StringPath traceId = createString("traceId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final EnumPath<com.ryuqq.fileflow.domain.file.extraction.ValidationStatus> validationStatus = createEnum("validationStatus", com.ryuqq.fileflow.domain.file.extraction.ValidationStatus.class);

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QExtractedDataJpaEntity(String variable) {
        super(ExtractedDataJpaEntity.class, forVariable(variable));
    }

    public QExtractedDataJpaEntity(Path<? extends ExtractedDataJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExtractedDataJpaEntity(PathMetadata metadata) {
        super(ExtractedDataJpaEntity.class, metadata);
    }

}

