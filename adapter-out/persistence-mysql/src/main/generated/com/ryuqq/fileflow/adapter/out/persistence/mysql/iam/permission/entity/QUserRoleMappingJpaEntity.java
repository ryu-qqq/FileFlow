package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserRoleMappingJpaEntity is a Querydsl query type for UserRoleMappingJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserRoleMappingJpaEntity extends EntityPathBase<UserRoleMappingJpaEntity> {

    private static final long serialVersionUID = 1059789959L;

    public static final QUserRoleMappingJpaEntity userRoleMappingJpaEntity = new QUserRoleMappingJpaEntity("userRoleMappingJpaEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath roleCode = createString("roleCode");

    public final NumberPath<Long> tenantId = createNumber("tenantId", Long.class);

    public final NumberPath<Long> userContextId = createNumber("userContextId", Long.class);

    public QUserRoleMappingJpaEntity(String variable) {
        super(UserRoleMappingJpaEntity.class, forVariable(variable));
    }

    public QUserRoleMappingJpaEntity(Path<? extends UserRoleMappingJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserRoleMappingJpaEntity(PathMetadata metadata) {
        super(UserRoleMappingJpaEntity.class, metadata);
    }

}

