package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.download.entity.QDownloadTaskJpaEntity.downloadTaskJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.download.condition.DownloadConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class DownloadTaskQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final DownloadConditionBuilder conditionBuilder;

    public DownloadTaskQueryDslRepository(
            JPAQueryFactory queryFactory, DownloadConditionBuilder conditionBuilder) {
        this.queryFactory = queryFactory;
        this.conditionBuilder = conditionBuilder;
    }

    public Optional<DownloadTaskJpaEntity> findById(String id) {
        DownloadTaskJpaEntity entity =
                queryFactory
                        .selectFrom(downloadTaskJpaEntity)
                        .where(conditionBuilder.idEq(id))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    public List<DownloadTaskJpaEntity> findByStatusAndCreatedBefore(
            DownloadTaskStatus status, Instant createdBefore, int limit) {
        return queryFactory
                .selectFrom(downloadTaskJpaEntity)
                .where(
                        conditionBuilder.statusEq(status),
                        conditionBuilder.createdBefore(createdBefore))
                .orderBy(downloadTaskJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }
}
