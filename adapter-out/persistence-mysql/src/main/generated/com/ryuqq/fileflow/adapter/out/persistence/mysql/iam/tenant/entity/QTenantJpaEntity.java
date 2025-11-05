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

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final BooleanPath deleted = createBoolean("deleted");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final EnumPath<com.ryuqq.fileflow.domain.iam.tenant.TenantStatus> status = createEnum("status", com.ryuqq.fileflow.domain.iam.tenant.TenantStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

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

