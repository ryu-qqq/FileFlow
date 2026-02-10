package com.ryuqq.fileflow.adapter.out.persistence.transform.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.transform.entity.QTransformRequestJpaEntity.transformRequestJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.transform.condition.TransformConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformRequestJpaEntity;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TransformRequestQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final TransformConditionBuilder conditionBuilder;

    public TransformRequestQueryDslRepository(
            JPAQueryFactory queryFactory, TransformConditionBuilder conditionBuilder) {
        this.queryFactory = queryFactory;
        this.conditionBuilder = conditionBuilder;
    }

    public Optional<TransformRequestJpaEntity> findById(String id) {
        TransformRequestJpaEntity result =
                queryFactory
                        .selectFrom(transformRequestJpaEntity)
                        .where(conditionBuilder.idEq(id))
                        .fetchOne();
        return Optional.ofNullable(result);
    }

    public List<TransformRequestJpaEntity> findByStatusAndCreatedBefore(
            TransformStatus status, Instant createdBefore, int limit) {
        return queryFactory
                .selectFrom(transformRequestJpaEntity)
                .where(
                        conditionBuilder.statusEq(status),
                        conditionBuilder.createdBefore(createdBefore))
                .orderBy(transformRequestJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }
}
