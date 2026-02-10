package com.ryuqq.fileflow.adapter.out.persistence.transform.condition;

import static com.ryuqq.fileflow.adapter.out.persistence.transform.entity.QTransformRequestJpaEntity.transformRequestJpaEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class TransformConditionBuilder {

    public BooleanExpression idEq(String id) {
        if (id == null) return null;
        return transformRequestJpaEntity.id.eq(id);
    }

    public BooleanExpression statusEq(TransformStatus status) {
        if (status == null) return null;
        return transformRequestJpaEntity.status.eq(status);
    }

    public BooleanExpression createdBefore(Instant createdBefore) {
        if (createdBefore == null) return null;
        return transformRequestJpaEntity.createdAt.before(createdBefore);
    }
}
