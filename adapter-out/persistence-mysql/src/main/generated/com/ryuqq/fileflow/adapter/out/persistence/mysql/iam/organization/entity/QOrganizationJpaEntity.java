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

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.entity.QBaseAuditEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.entity.QBaseAuditEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final BooleanPath deleted = createBoolean("deleted");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath orgCode = createString("orgCode");

    public final EnumPath<com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus> status = createEnum("status", com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus.class);

    public final NumberPath<Long> tenantId = createNumber("tenantId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

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

