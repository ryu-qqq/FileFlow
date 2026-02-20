package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.session.entity.QSingleUploadSessionJpaEntity.singleUploadSessionJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.session.condition.SessionConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.session.vo.SingleSessionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class SingleUploadSessionQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final SessionConditionBuilder conditionBuilder;

    public SingleUploadSessionQueryDslRepository(
            JPAQueryFactory queryFactory, SessionConditionBuilder conditionBuilder) {
        this.queryFactory = queryFactory;
        this.conditionBuilder = conditionBuilder;
    }

    public Optional<SingleUploadSessionJpaEntity> findById(String id) {
        SingleUploadSessionJpaEntity entity =
                queryFactory
                        .selectFrom(singleUploadSessionJpaEntity)
                        .where(conditionBuilder.singleSessionIdEq(id))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    public List<SingleUploadSessionJpaEntity> findExpiredSessions(Instant now, int limit) {
        return queryFactory
                .selectFrom(singleUploadSessionJpaEntity)
                .where(
                        singleUploadSessionJpaEntity.status.eq(SingleSessionStatus.CREATED),
                        singleUploadSessionJpaEntity.expiresAt.before(now))
                .orderBy(singleUploadSessionJpaEntity.expiresAt.asc())
                .limit(limit)
                .fetch();
    }
}
