package com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPipelineOutboxJpaEntity is a Querydsl query type for PipelineOutboxJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPipelineOutboxJpaEntity extends EntityPathBase<PipelineOutboxJpaEntity> {

    private static final long serialVersionUID = 884535791L;

    public static final QPipelineOutboxJpaEntity pipelineOutboxJpaEntity = new QPipelineOutboxJpaEntity("pipelineOutboxJpaEntity");

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> fileId = createNumber("fileId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath idempotencyKey = createString("idempotencyKey");

    public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

    public final EnumPath<com.ryuqq.fileflow.domain.download.OutboxStatus> status = createEnum("status", com.ryuqq.fileflow.domain.download.OutboxStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPipelineOutboxJpaEntity(String variable) {
        super(PipelineOutboxJpaEntity.class, forVariable(variable));
    }

    public QPipelineOutboxJpaEntity(Path<? extends PipelineOutboxJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPipelineOutboxJpaEntity(PathMetadata metadata) {
        super(PipelineOutboxJpaEntity.class, metadata);
    }

}

