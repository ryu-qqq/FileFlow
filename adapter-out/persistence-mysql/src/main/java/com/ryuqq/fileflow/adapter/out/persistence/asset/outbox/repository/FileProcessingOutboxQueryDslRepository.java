package com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.entity.FileProcessingOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.entity.FileProcessingOutboxJpaEntity.OutboxStatusEnum;
import com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.entity.QFileProcessingOutboxJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * FileProcessingOutbox QueryDSL Repository.
 *
 * <p>FileProcessingOutbox 조회를 담당하는 QueryDSL Repository입니다.
 */
@Repository
public class FileProcessingOutboxQueryDslRepository {

    private static final QFileProcessingOutboxJpaEntity outbox =
            QFileProcessingOutboxJpaEntity.fileProcessingOutboxJpaEntity;

    private final JPAQueryFactory queryFactory;

    public FileProcessingOutboxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 Outbox를 조회한다.
     *
     * @param id Outbox ID (UUID)
     * @return FileProcessingOutboxJpaEntity Optional
     */
    public Optional<FileProcessingOutboxJpaEntity> findById(UUID id) {
        FileProcessingOutboxJpaEntity result =
                queryFactory.selectFrom(outbox).where(outbox.id.eq(id)).fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * 대기 중인(PENDING) Outbox 목록을 조회한다 (생성순 정렬).
     *
     * @param limit 최대 조회 수
     * @return 대기 중인 Outbox 목록
     */
    public List<FileProcessingOutboxJpaEntity> findPendingEvents(int limit) {
        return queryFactory
                .selectFrom(outbox)
                .where(outbox.status.eq(OutboxStatusEnum.PENDING))
                .orderBy(outbox.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 재시도 가능한 실패 Outbox 목록을 조회한다 (생성순 정렬).
     *
     * @param limit 최대 조회 수
     * @return 재시도 가능한 실패 Outbox 목록
     */
    public List<FileProcessingOutboxJpaEntity> findRetryableFailedEvents(int limit) {
        return queryFactory
                .selectFrom(outbox)
                .where(
                        outbox.status.eq(OutboxStatusEnum.FAILED),
                        outbox.retryCount.lt(FileProcessingOutbox.MAX_RETRY_COUNT))
                .orderBy(outbox.createdAt.asc())
                .limit(limit)
                .fetch();
    }
}
