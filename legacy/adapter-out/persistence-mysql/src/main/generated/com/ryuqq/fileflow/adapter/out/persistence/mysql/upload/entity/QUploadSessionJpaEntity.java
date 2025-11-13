package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUploadSessionJpaEntity is a Querydsl query type for UploadSessionJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUploadSessionJpaEntity extends EntityPathBase<UploadSessionJpaEntity> {

    private static final long serialVersionUID = 1369451160L;

    public static final QUploadSessionJpaEntity uploadSessionJpaEntity = new QUploadSessionJpaEntity("uploadSessionJpaEntity");

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity(this);

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> failedAt = createDateTime("failedAt", java.time.LocalDateTime.class);

    public final StringPath failureReason = createString("failureReason");

    public final NumberPath<Long> fileId = createNumber("fileId", Long.class);

    public final StringPath fileName = createString("fileName");

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath sessionKey = createString("sessionKey");

    public final EnumPath<com.ryuqq.fileflow.domain.upload.SessionStatus> status = createEnum("status", com.ryuqq.fileflow.domain.upload.SessionStatus.class);

    public final StringPath storageKey = createString("storageKey");

    public final NumberPath<Long> tenantId = createNumber("tenantId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final EnumPath<com.ryuqq.fileflow.domain.upload.UploadType> uploadType = createEnum("uploadType", com.ryuqq.fileflow.domain.upload.UploadType.class);

    public QUploadSessionJpaEntity(String variable) {
        super(UploadSessionJpaEntity.class, forVariable(variable));
    }

    public QUploadSessionJpaEntity(Path<? extends UploadSessionJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUploadSessionJpaEntity(PathMetadata metadata) {
        super(UploadSessionJpaEntity.class, metadata);
    }

}

