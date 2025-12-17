package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.WebhookOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.WebhookOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.WebhookOutboxJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.command.WebhookOutboxPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.WebhookOutbox;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxId;
import org.springframework.stereotype.Repository;

/**
 * WebhookOutbox Persistence Adapter.
 *
 * <p>WebhookOutbox의 저장/수정을 담당합니다.
 */
@Repository
public class WebhookOutboxPersistenceAdapter implements WebhookOutboxPersistencePort {

    private final WebhookOutboxJpaRepository jpaRepository;
    private final WebhookOutboxJpaMapper mapper;

    public WebhookOutboxPersistenceAdapter(
            WebhookOutboxJpaRepository jpaRepository, WebhookOutboxJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public WebhookOutboxId persist(WebhookOutbox outbox) {
        WebhookOutboxJpaEntity entity = mapper.toEntity(outbox);
        WebhookOutboxJpaEntity savedEntity = jpaRepository.save(entity);
        return WebhookOutboxId.of(savedEntity.getId());
    }

    @Override
    public void update(WebhookOutbox outbox) {
        WebhookOutboxJpaEntity entity = mapper.toEntity(outbox);
        jpaRepository.save(entity);
    }
}
