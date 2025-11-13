package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUploadPartJpaEntity is a Querydsl query type for UploadPartJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUploadPartJpaEntity extends EntityPathBase<UploadPartJpaEntity> {

    private static final long serialVersionUID = 1554732539L;

    public static final QUploadPartJpaEntity uploadPartJpaEntity = new QUploadPartJpaEntity("uploadPartJpaEntity");

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity(this);

    public final StringPath checksum = createString("checksum");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath etag = createString("etag");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> multipartUploadId = createNumber("multipartUploadId", Long.class);

    public final NumberPath<Integer> partNumber = createNumber("partNumber", Integer.class);

    public final NumberPath<Long> size = createNumber("size", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final DateTimePath<java.time.LocalDateTime> uploadedAt = createDateTime("uploadedAt", java.time.LocalDateTime.class);

    public QUploadPartJpaEntity(String variable) {
        super(UploadPartJpaEntity.class, forVariable(variable));
    }

    public QUploadPartJpaEntity(Path<? extends UploadPartJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUploadPartJpaEntity(PathMetadata metadata) {
        super(UploadPartJpaEntity.class, metadata);
    }

}

