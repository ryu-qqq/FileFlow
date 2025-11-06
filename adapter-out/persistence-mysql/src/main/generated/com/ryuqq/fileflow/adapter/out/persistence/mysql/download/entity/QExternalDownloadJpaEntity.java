package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QExternalDownloadJpaEntity is a Querydsl query type for ExternalDownloadJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExternalDownloadJpaEntity extends EntityPathBase<ExternalDownloadJpaEntity> {

    private static final long serialVersionUID = 1799252693L;

    public static final QExternalDownloadJpaEntity externalDownloadJpaEntity = new QExternalDownloadJpaEntity("externalDownloadJpaEntity");

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity(this);

    public final NumberPath<Long> bytesTransferred = createNumber("bytesTransferred", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath errorCode = createString("errorCode");

    public final StringPath errorMessage = createString("errorMessage");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastRetryAt = createDateTime("lastRetryAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

    public final StringPath sourceUrl = createString("sourceUrl");

    public final EnumPath<com.ryuqq.fileflow.domain.download.ExternalDownloadStatus> status = createEnum("status", com.ryuqq.fileflow.domain.download.ExternalDownloadStatus.class);

    public final NumberPath<Long> totalBytes = createNumber("totalBytes", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> uploadSessionId = createNumber("uploadSessionId", Long.class);

    public QExternalDownloadJpaEntity(String variable) {
        super(ExternalDownloadJpaEntity.class, forVariable(variable));
    }

    public QExternalDownloadJpaEntity(Path<? extends ExternalDownloadJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExternalDownloadJpaEntity(PathMetadata metadata) {
        super(ExternalDownloadJpaEntity.class, metadata);
    }

}

