package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSettingJpaEntity is a Querydsl query type for SettingJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettingJpaEntity extends EntityPathBase<SettingJpaEntity> {

    private static final long serialVersionUID = -983980257L;

    public static final QSettingJpaEntity settingJpaEntity = new QSettingJpaEntity("settingJpaEntity");

    public final NumberPath<Long> contextId = createNumber("contextId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isSecret = createBoolean("isSecret");

    public final EnumPath<com.ryuqq.fileflow.domain.settings.SettingLevel> level = createEnum("level", com.ryuqq.fileflow.domain.settings.SettingLevel.class);

    public final StringPath settingKey = createString("settingKey");

    public final EnumPath<com.ryuqq.fileflow.domain.settings.SettingType> settingType = createEnum("settingType", com.ryuqq.fileflow.domain.settings.SettingType.class);

    public final StringPath settingValue = createString("settingValue");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QSettingJpaEntity(String variable) {
        super(SettingJpaEntity.class, forVariable(variable));
    }

    public QSettingJpaEntity(Path<? extends SettingJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSettingJpaEntity(PathMetadata metadata) {
        super(SettingJpaEntity.class, metadata);
    }

}

