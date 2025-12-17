package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.WebhookOutboxJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** WebhookOutbox JPA Repository. */
public interface WebhookOutboxJpaRepository extends JpaRepository<WebhookOutboxJpaEntity, UUID> {}
