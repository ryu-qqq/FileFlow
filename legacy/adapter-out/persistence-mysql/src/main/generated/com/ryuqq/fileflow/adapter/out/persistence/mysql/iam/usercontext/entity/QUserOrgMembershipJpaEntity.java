package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserOrgMembershipJpaEntity is a Querydsl query type for UserOrgMembershipJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserOrgMembershipJpaEntity extends EntityPathBase<UserOrgMembershipJpaEntity> {

    private static final long serialVersionUID = 1485437958L;

    public static final QUserOrgMembershipJpaEntity userOrgMembershipJpaEntity = new QUserOrgMembershipJpaEntity("userOrgMembershipJpaEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath membershipType = createString("membershipType");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> tenantId = createNumber("tenantId", Long.class);

    public final NumberPath<Long> userContextId = createNumber("userContextId", Long.class);

    public QUserOrgMembershipJpaEntity(String variable) {
        super(UserOrgMembershipJpaEntity.class, forVariable(variable));
    }

    public QUserOrgMembershipJpaEntity(Path<? extends UserOrgMembershipJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserOrgMembershipJpaEntity(PathMetadata metadata) {
        super(UserOrgMembershipJpaEntity.class, metadata);
    }

}

