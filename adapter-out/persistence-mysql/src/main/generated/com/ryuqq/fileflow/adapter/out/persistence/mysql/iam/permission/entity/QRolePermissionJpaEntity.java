package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRolePermissionJpaEntity is a Querydsl query type for RolePermissionJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRolePermissionJpaEntity extends EntityPathBase<RolePermissionJpaEntity> {

    private static final long serialVersionUID = -1456169373L;

    public static final QRolePermissionJpaEntity rolePermissionJpaEntity = new QRolePermissionJpaEntity("rolePermissionJpaEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath permissionCode = createString("permissionCode");

    public final StringPath roleCode = createString("roleCode");

    public QRolePermissionJpaEntity(String variable) {
        super(RolePermissionJpaEntity.class, forVariable(variable));
    }

    public QRolePermissionJpaEntity(Path<? extends RolePermissionJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRolePermissionJpaEntity(PathMetadata metadata) {
        super(RolePermissionJpaEntity.class, metadata);
    }

}

