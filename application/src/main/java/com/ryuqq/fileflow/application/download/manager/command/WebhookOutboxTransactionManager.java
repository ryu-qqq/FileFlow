package com.ryuqq.fileflow.application.download.manager.command;

import com.ryuqq.fileflow.application.download.port.out.command.WebhookOutboxPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.WebhookOutbox;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * WebhookOutbox 영속화 TransactionManager.
 *
 * <p>Transaction 경계를 담당합니다.
 *
 * <p><strong>컨벤션</strong>:
 *
 * <ul>
 *   <li>단일 PersistencePort 의존성
 *   <li>persist*, update* 메서드만 허용
 *   <li>@Component + @Transactional 필수
 * </ul>
 */
@Component
@Transactional
public class WebhookOutboxTransactionManager {

    private final WebhookOutboxPersistencePort persistencePort;

    public WebhookOutboxTransactionManager(WebhookOutboxPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * WebhookOutbox를 저장합니다.
     *
     * @param webhookOutbox 저장할 WebhookOutbox
     * @return 저장된 WebhookOutboxId
     */
    public WebhookOutboxId persist(WebhookOutbox webhookOutbox) {
        return persistencePort.persist(webhookOutbox);
    }

    /**
     * WebhookOutbox를 업데이트합니다.
     *
     * @param webhookOutbox 업데이트할 WebhookOutbox
     */
    public void update(WebhookOutbox webhookOutbox) {
        persistencePort.update(webhookOutbox);
    }
}
