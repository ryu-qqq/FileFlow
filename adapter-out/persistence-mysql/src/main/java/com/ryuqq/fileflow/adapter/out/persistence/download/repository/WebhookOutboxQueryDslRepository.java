package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.QWebhookOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.WebhookOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.WebhookOutboxJpaEntity.WebhookOutboxStatusJpa;
import com.ryuqq.fileflow.domain.download.aggregate.WebhookOutbox;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * WebhookOutbox QueryDSL Repository.
 *
 * <p>WebhookOutbox 조회를 담당하는 QueryDSL Repository입니다.
 */
@Repository
public class WebhookOutboxQueryDslRepository {

    private static final QWebhookOutboxJpaEntity outbox =
            QWebhookOutboxJpaEntity.webhookOutboxJpaEntity;

    private final JPAQueryFactory queryFactory;

    public WebhookOutboxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 WebhookOutbox를 조회한다.
     *
     * @param id 웹훅 아웃박스 ID (UUID)
     * @return WebhookOutboxJpaEntity Optional
     */
    public Optional<WebhookOutboxJpaEntity> findById(UUID id) {
        WebhookOutboxJpaEntity result =
                queryFactory.selectFrom(outbox).where(outbox.id.eq(id)).fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * 특정 상태의 재시도 대상 Outbox 목록을 조회한다 (retryCount < MAX, 생성순 정렬).
     *
     * @param status 조회할 상태
     * @param limit 최대 조회 수
     * @return 재시도 대상 Outbox 목록
     */
    public List<WebhookOutboxJpaEntity> findByStatusForRetry(
            WebhookOutboxStatus status, int limit) {
        return queryFactory
                .selectFrom(outbox)
                .where(
                        outbox.status.eq(toJpaStatus(status)),
                        outbox.retryCount.lt(WebhookOutbox.MAX_RETRY_COUNT))
                .orderBy(outbox.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    private WebhookOutboxStatusJpa toJpaStatus(WebhookOutboxStatus status) {
        return switch (status) {
            case PENDING -> WebhookOutboxStatusJpa.PENDING;
            case SENT -> WebhookOutboxStatusJpa.SENT;
            case FAILED -> WebhookOutboxStatusJpa.FAILED;
        };
    }
}
