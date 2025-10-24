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

    public final StringPath code = createString("code");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath defaultScope = createString("defaultScope");

    public final BooleanPath deleted = createBoolean("deleted");

    public final StringPath description = createString("description");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

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

