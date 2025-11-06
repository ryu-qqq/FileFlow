package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QExternalDownloadOutboxJpaEntity is a Querydsl query type for ExternalDownloadOutboxJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExternalDownloadOutboxJpaEntity extends EntityPathBase<ExternalDownloadOutboxJpaEntity> {

    private static final long serialVersionUID = -218692008L;

    public static final QExternalDownloadOutboxJpaEntity externalDownloadOutboxJpaEntity = new QExternalDownloadOutboxJpaEntity("externalDownloadOutboxJpaEntity");

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> downloadId = createNumber("downloadId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath idempotencyKey = createString("idempotencyKey");

    public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

    public final EnumPath<com.ryuqq.fileflow.domain.common.OutboxStatus> status = createEnum("status", com.ryuqq.fileflow.domain.common.OutboxStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> uploadSessionId = createNumber("uploadSessionId", Long.class);

    public QExternalDownloadOutboxJpaEntity(String variable) {
        super(ExternalDownloadOutboxJpaEntity.class, forVariable(variable));
    }

    public QExternalDownloadOutboxJpaEntity(Path<? extends ExternalDownloadOutboxJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExternalDownloadOutboxJpaEntity(PathMetadata metadata) {
        super(ExternalDownloadOutboxJpaEntity.class, metadata);
    }

}

