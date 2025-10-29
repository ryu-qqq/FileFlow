package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRoleJpaEntity is a Querydsl query type for RoleJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoleJpaEntity extends EntityPathBase<RoleJpaEntity> {

    private static final long serialVersionUID = 342762610L;

    public static final QRoleJpaEntity roleJpaEntity = new QRoleJpaEntity("roleJpaEntity");

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.entity.QSoftDeletableEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.entity.QSoftDeletableEntity(this);

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath description = createString("description");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QRoleJpaEntity(String variable) {
        super(RoleJpaEntity.class, forVariable(variable));
    }

    public QRoleJpaEntity(Path<? extends RoleJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRoleJpaEntity(PathMetadata metadata) {
        super(RoleJpaEntity.class, metadata);
    }

}

