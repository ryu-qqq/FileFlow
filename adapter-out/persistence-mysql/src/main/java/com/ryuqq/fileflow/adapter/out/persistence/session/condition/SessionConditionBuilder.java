package com.ryuqq.fileflow.adapter.out.persistence.session.condition;

import static com.ryuqq.fileflow.adapter.out.persistence.session.entity.QCompletedPartJpaEntity.completedPartJpaEntity;
import static com.ryuqq.fileflow.adapter.out.persistence.session.entity.QMultipartUploadSessionJpaEntity.multipartUploadSessionJpaEntity;
import static com.ryuqq.fileflow.adapter.out.persistence.session.entity.QSingleUploadSessionJpaEntity.singleUploadSessionJpaEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.stereotype.Component;

@Component
public class SessionConditionBuilder {

    public BooleanExpression singleSessionIdEq(String id) {
        if (id == null) return null;
        return singleUploadSessionJpaEntity.id.eq(id);
    }

    public BooleanExpression multipartSessionIdEq(String id) {
        if (id == null) return null;
        return multipartUploadSessionJpaEntity.id.eq(id);
    }

    public BooleanExpression completedPartSessionIdEq(String sessionId) {
        if (sessionId == null) return null;
        return completedPartJpaEntity.sessionId.eq(sessionId);
    }
}
