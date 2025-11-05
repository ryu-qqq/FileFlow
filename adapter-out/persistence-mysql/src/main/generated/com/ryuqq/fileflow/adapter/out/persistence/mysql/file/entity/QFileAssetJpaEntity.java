package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFileAssetJpaEntity is a Querydsl query type for FileAssetJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFileAssetJpaEntity extends EntityPathBase<FileAssetJpaEntity> {

    private static final long serialVersionUID = -494105740L;

    public static final QFileAssetJpaEntity fileAssetJpaEntity = new QFileAssetJpaEntity("fileAssetJpaEntity");

    public final com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity _super = new com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.QBaseAuditEntity(this);

    public final StringPath checksumSha256 = createString("checksumSha256");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final StringPath fileName = createString("fileName");

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mimeType = createString("mimeType");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> ownerUserId = createNumber("ownerUserId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> retentionDays = createNumber("retentionDays", Integer.class);

    public final EnumPath<com.ryuqq.fileflow.domain.file.asset.FileStatus> status = createEnum("status", com.ryuqq.fileflow.domain.file.asset.FileStatus.class);

    public final StringPath storageKey = createString("storageKey");

    public final NumberPath<Long> tenantId = createNumber("tenantId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final DateTimePath<java.time.LocalDateTime> uploadedAt = createDateTime("uploadedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> uploadSessionId = createNumber("uploadSessionId", Long.class);

    public final EnumPath<com.ryuqq.fileflow.domain.file.asset.Visibility> visibility = createEnum("visibility", com.ryuqq.fileflow.domain.file.asset.Visibility.class);

    public QFileAssetJpaEntity(String variable) {
        super(FileAssetJpaEntity.class, forVariable(variable));
    }

    public QFileAssetJpaEntity(Path<? extends FileAssetJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFileAssetJpaEntity(PathMetadata metadata) {
        super(FileAssetJpaEntity.class, metadata);
    }

}

