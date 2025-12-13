package com.ryuqq.fileflow.application.asset.manager.command;

import com.ryuqq.fileflow.application.asset.port.out.command.FileProcessingOutboxPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileProcessingOutbox 영속화 TransactionManager.
 *
 * <p>FileProcessingOutbox Aggregate의 영속화를 담당합니다.
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
public class FileProcessingOutboxTransactionManager {

    private final FileProcessingOutboxPersistencePort persistencePort;

    public FileProcessingOutboxTransactionManager(
            FileProcessingOutboxPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * FileProcessingOutbox를 저장합니다.
     *
     * @param outbox 저장할 FileProcessingOutbox
     * @return 저장된 FileProcessingOutbox ID
     */
    public FileProcessingOutboxId persist(FileProcessingOutbox outbox) {
        return persistencePort.persist(outbox);
    }
}
