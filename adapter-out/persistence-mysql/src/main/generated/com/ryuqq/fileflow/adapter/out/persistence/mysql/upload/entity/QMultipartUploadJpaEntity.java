package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMultipartUploadJpaEntity is a Querydsl query type for MultipartUploadJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMultipartUploadJpaEntity extends EntityPathBase<MultipartUploadJpaEntity> {

    private static final long serialVersionUID = -955550368L;

    public static final QMultipartUploadJpaEntity multipartUploadJpaEntity = new QMultipartUploadJpaEntity("multipartUploadJpaEntity");

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity(this);

    public final DateTimePath<java.time.LocalDateTime> abortedAt = createDateTime("abortedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath providerUploadId = createString("providerUploadId");

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.ryuqq.fileflow.domain.upload.MultipartUpload.MultipartStatus> status = createEnum("status", com.ryuqq.fileflow.domain.upload.MultipartUpload.MultipartStatus.class);

    public final NumberPath<Integer> totalParts = createNumber("totalParts", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> uploadSessionId = createNumber("uploadSessionId", Long.class);

    public QMultipartUploadJpaEntity(String variable) {
        super(MultipartUploadJpaEntity.class, forVariable(variable));
    }

    public QMultipartUploadJpaEntity(Path<? extends MultipartUploadJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMultipartUploadJpaEntity(PathMetadata metadata) {
        super(MultipartUploadJpaEntity.class, metadata);
    }

}

