package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserContextJpaEntity is a Querydsl query type for UserContextJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserContextJpaEntity extends EntityPathBase<UserContextJpaEntity> {

    private static final long serialVersionUID = 2068680017L;

    public static final QUserContextJpaEntity userContextJpaEntity = new QUserContextJpaEntity("userContextJpaEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath deleted = createBoolean("deleted");

    public final StringPath email = createString("email");

    public final StringPath externalUserId = createString("externalUserId");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QUserContextJpaEntity(String variable) {
        super(UserContextJpaEntity.class, forVariable(variable));
    }

    public QUserContextJpaEntity(Path<? extends UserContextJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserContextJpaEntity(PathMetadata metadata) {
        super(UserContextJpaEntity.class, metadata);
    }

}

