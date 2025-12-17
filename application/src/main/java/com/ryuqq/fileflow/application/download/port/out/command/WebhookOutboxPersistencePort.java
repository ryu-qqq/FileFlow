package com.ryuqq.fileflow.application.download.port.out.command;

import com.ryuqq.fileflow.domain.download.aggregate.WebhookOutbox;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxId;

/**
 * WebhookOutbox 영속화 포트.
 *
 * <p>CQRS Command Side - Webhook Outbox 저장/수정
 */
public interface WebhookOutboxPersistencePort {

    /**
     * WebhookOutbox를 저장합니다.
     *
     * @param webhookOutbox 저장할 WebhookOutbox
     * @return 생성된 WebhookOutboxId
     */
    WebhookOutboxId persist(WebhookOutbox webhookOutbox);

    /**
     * WebhookOutbox를 업데이트합니다.
     *
     * @param webhookOutbox 업데이트할 WebhookOutbox
     */
    void update(WebhookOutbox webhookOutbox);
}
