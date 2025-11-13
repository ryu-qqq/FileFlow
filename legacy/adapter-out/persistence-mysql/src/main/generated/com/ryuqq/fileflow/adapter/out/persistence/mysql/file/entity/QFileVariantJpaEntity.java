package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFileVariantJpaEntity is a Querydsl query type for FileVariantJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFileVariantJpaEntity extends EntityPathBase<FileVariantJpaEntity> {

    private static final long serialVersionUID = -914539521L;

    public static final QFileVariantJpaEntity fileVariantJpaEntity = new QFileVariantJpaEntity("fileVariantJpaEntity");

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mimeType = createString("mimeType");

    public final NumberPath<Long> parentFileAssetId = createNumber("parentFileAssetId", Long.class);

    public final StringPath storageKey = createString("storageKey");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final EnumPath<com.ryuqq.fileflow.domain.file.variant.VariantType> variantType = createEnum("variantType", com.ryuqq.fileflow.domain.file.variant.VariantType.class);

    public QFileVariantJpaEntity(String variable) {
        super(FileVariantJpaEntity.class, forVariable(variable));
    }

    public QFileVariantJpaEntity(Path<? extends FileVariantJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFileVariantJpaEntity(PathMetadata metadata) {
        super(FileVariantJpaEntity.class, metadata);
    }

}

