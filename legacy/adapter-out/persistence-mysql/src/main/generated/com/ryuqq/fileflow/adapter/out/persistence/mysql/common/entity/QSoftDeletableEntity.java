package com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSoftDeletableEntity is a Querydsl query type for SoftDeletableEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QSoftDeletableEntity extends EntityPathBase<SoftDeletableEntity> {

    private static final long serialVersionUID = -380485324L;

    public static final QSoftDeletableEntity softDeletableEntity = new QSoftDeletableEntity("softDeletableEntity");

    public final QBaseAuditEntity _super = new QBaseAuditEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSoftDeletableEntity(String variable) {
        super(SoftDeletableEntity.class, forVariable(variable));
    }

    public QSoftDeletableEntity(Path<? extends SoftDeletableEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSoftDeletableEntity(PathMetadata metadata) {
        super(SoftDeletableEntity.class, metadata);
    }

}

