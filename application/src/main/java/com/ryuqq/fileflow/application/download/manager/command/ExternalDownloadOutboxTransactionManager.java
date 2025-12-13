package com.ryuqq.fileflow.application.download.manager.command;

import com.ryuqq.fileflow.application.download.port.out.command.ExternalDownloadOutboxPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ExternalDownloadOutbox 영속화 TransactionManager.
 *
 * <p>Transaction 경계를 담당합니다.
 *
 * <p><strong>컨벤션</strong>:
 *
 * <ul>
 *   <li>단일 PersistencePort 의존성
 *   <li>persist* 메서드만 허용
 *   <li>@Component + @Transactional 필수
 * </ul>
 */
@Component
@Transactional
public class ExternalDownloadOutboxTransactionManager {

    private final ExternalDownloadOutboxPersistencePort persistencePort;

    public ExternalDownloadOutboxTransactionManager(
            ExternalDownloadOutboxPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * ExternalDownloadOutbox를 저장합니다.
     *
     * @param outbox 저장할 ExternalDownloadOutbox
     * @return 저장된 ExternalDownloadOutboxId
     */
    public ExternalDownloadOutboxId persist(ExternalDownloadOutbox outbox) {
        return persistencePort.persist(outbox);
    }
}
