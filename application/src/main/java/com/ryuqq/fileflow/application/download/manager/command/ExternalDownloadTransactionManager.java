package com.ryuqq.fileflow.application.download.manager.command;

import com.ryuqq.fileflow.application.common.config.TransactionEventRegistry;
import com.ryuqq.fileflow.application.download.port.out.command.ExternalDownloadPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ExternalDownload 영속화 TransactionManager.
 *
 * <p>ExternalDownload Aggregate의 영속화를 담당합니다.
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
public class ExternalDownloadTransactionManager {

    private final ExternalDownloadPersistencePort persistencePort;

    public ExternalDownloadTransactionManager(ExternalDownloadPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * ExternalDownload를 저장합니다.
     *
     * @param externalDownload 저장할 ExternalDownload
     * @return 저장된 ExternalDownload (version 갱신됨)
     */
    public ExternalDownload persist(ExternalDownload externalDownload) {
        return persistencePort.persist(externalDownload);
    }

    /**
     * ExternalDownload를 저장하고 도메인 이벤트를 트랜잭션 커밋 후 발행하도록 등록합니다.
     *
     * <p>이벤트 등록이 persist 트랜잭션 내에서 이루어져 AFTER_COMMIT 리스너가 정상 작동합니다.
     *
     * @param externalDownload 저장할 ExternalDownload (도메인 이벤트 포함)
     * @param eventRegistry 이벤트 레지스트리
     * @return 저장된 ExternalDownload (version 갱신됨)
     */
    public ExternalDownload persistWithEvents(
            ExternalDownload externalDownload, TransactionEventRegistry eventRegistry) {
        // 트랜잭션 내에서 이벤트 등록 (AFTER_COMMIT 시 발행됨)
        externalDownload.getDomainEvents().forEach(eventRegistry::registerForPublish);
        externalDownload.clearDomainEvents();

        return persistencePort.persist(externalDownload);
    }
}
