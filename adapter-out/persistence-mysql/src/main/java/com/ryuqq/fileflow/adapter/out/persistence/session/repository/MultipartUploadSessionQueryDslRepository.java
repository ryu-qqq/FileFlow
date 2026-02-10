package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.session.entity.QCompletedPartJpaEntity.completedPartJpaEntity;
import static com.ryuqq.fileflow.adapter.out.persistence.session.entity.QMultipartUploadSessionJpaEntity.multipartUploadSessionJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.session.condition.SessionConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MultipartUploadSessionQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final SessionConditionBuilder conditionBuilder;

    public MultipartUploadSessionQueryDslRepository(
            JPAQueryFactory queryFactory, SessionConditionBuilder conditionBuilder) {
        this.queryFactory = queryFactory;
        this.conditionBuilder = conditionBuilder;
    }

    public Optional<MultipartUploadSessionJpaEntity> findById(String id) {
        MultipartUploadSessionJpaEntity entity =
                queryFactory
                        .selectFrom(multipartUploadSessionJpaEntity)
                        .where(conditionBuilder.multipartSessionIdEq(id))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    public List<CompletedPartJpaEntity> findCompletedPartsBySessionId(String sessionId) {
        return queryFactory
                .selectFrom(completedPartJpaEntity)
                .where(conditionBuilder.completedPartSessionIdEq(sessionId))
                .orderBy(completedPartJpaEntity.partNumber.asc())
                .fetch();
    }
}
