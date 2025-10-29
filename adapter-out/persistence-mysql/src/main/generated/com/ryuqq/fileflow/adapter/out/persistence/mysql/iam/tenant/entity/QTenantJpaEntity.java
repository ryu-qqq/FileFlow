package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTenantJpaEntity is a Querydsl query type for TenantJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTenantJpaEntity extends EntityPathBase<TenantJpaEntity> {

    private static final long serialVersionUID = 1803757059L;

    public static final QTenantJpaEntity tenantJpaEntity = new QTenantJpaEntity("tenantJpaEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath deleted = createBoolean("deleted");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final EnumPath<com.ryuqq.fileflow.domain.iam.tenant.TenantStatus> status = createEnum("status", com.ryuqq.fileflow.domain.iam.tenant.TenantStatus.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QTenantJpaEntity(String variable) {
        super(TenantJpaEntity.class, forVariable(variable));
    }

    public QTenantJpaEntity(Path<? extends TenantJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTenantJpaEntity(PathMetadata metadata) {
        super(TenantJpaEntity.class, metadata);
    }

}

