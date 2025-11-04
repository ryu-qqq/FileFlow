package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPermissionJpaEntity is a Querydsl query type for PermissionJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPermissionJpaEntity extends EntityPathBase<PermissionJpaEntity> {

    private static final long serialVersionUID = 1333013433L;

    public static final QPermissionJpaEntity permissionJpaEntity = new QPermissionJpaEntity("permissionJpaEntity");

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QSoftDeletableEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QSoftDeletableEntity(this);

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath defaultScope = createString("defaultScope");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath description = createString("description");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPermissionJpaEntity(String variable) {
        super(PermissionJpaEntity.class, forVariable(variable));
    }

    public QPermissionJpaEntity(Path<? extends PermissionJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPermissionJpaEntity(PathMetadata metadata) {
        super(PermissionJpaEntity.class, metadata);
    }

}

