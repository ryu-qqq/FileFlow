package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrganizationJpaEntity is a Querydsl query type for OrganizationJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrganizationJpaEntity extends EntityPathBase<OrganizationJpaEntity> {

    private static final long serialVersionUID = -1455925263L;

    public static final QOrganizationJpaEntity organizationJpaEntity = new QOrganizationJpaEntity("organizationJpaEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath deleted = createBoolean("deleted");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath orgCode = createString("orgCode");

    public final EnumPath<com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus> status = createEnum("status", com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus.class);

    public final NumberPath<Long> tenantId = createNumber("tenantId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QOrganizationJpaEntity(String variable) {
        super(OrganizationJpaEntity.class, forVariable(variable));
    }

    public QOrganizationJpaEntity(Path<? extends OrganizationJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrganizationJpaEntity(PathMetadata metadata) {
        super(OrganizationJpaEntity.class, metadata);
    }

}

