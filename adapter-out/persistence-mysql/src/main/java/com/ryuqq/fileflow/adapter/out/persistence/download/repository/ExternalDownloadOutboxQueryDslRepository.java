package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.QExternalDownloadOutboxJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * ExternalDownloadOutbox QueryDSL Repository.
 *
 * <p>ExternalDownloadOutbox 조회를 담당하는 QueryDSL Repository입니다.
 */
@Repository
public class ExternalDownloadOutboxQueryDslRepository {

    private static final QExternalDownloadOutboxJpaEntity outbox =
            QExternalDownloadOutboxJpaEntity.externalDownloadOutboxJpaEntity;

    private final JPAQueryFactory queryFactory;

    public ExternalDownloadOutboxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ExternalDownloadId로 Outbox를 조회한다.
     *
     * @param externalDownloadId 외부 다운로드 ID (UUID)
     * @return ExternalDownloadOutboxJpaEntity Optional
     */
    public Optional<ExternalDownloadOutboxJpaEntity> findByExternalDownloadId(
            UUID externalDownloadId) {
        ExternalDownloadOutboxJpaEntity result =
                queryFactory
                        .selectFrom(outbox)
                        .where(outbox.externalDownloadId.eq(externalDownloadId))
                        .fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * 미발행 Outbox 목록을 조회한다 (생성순 정렬).
     *
     * @param limit 최대 조회 수
     * @return 미발행 Outbox 목록
     */
    public List<ExternalDownloadOutboxJpaEntity> findUnpublished(int limit) {
        return queryFactory
                .selectFrom(outbox)
                .where(outbox.published.eq(false))
                .orderBy(outbox.createdAt.asc())
                .limit(limit)
                .fetch();
    }
}
