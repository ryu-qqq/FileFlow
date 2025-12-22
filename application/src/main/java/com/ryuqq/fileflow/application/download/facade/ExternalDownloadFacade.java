package com.ryuqq.fileflow.application.download.facade;

import com.ryuqq.fileflow.application.common.config.TransactionEventRegistry;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadBundle;
import com.ryuqq.fileflow.application.download.manager.command.ExternalDownloadOutboxTransactionManager;
import com.ryuqq.fileflow.application.download.manager.command.ExternalDownloadTransactionManager;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 외부 다운로드 Facade.
 *
 * <p>ExternalDownload 저장과 이벤트 발행을 조율합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>ExternalDownloadBundle 저장 (Download + Outbox)
 *   <li>도메인 이벤트 발행 및 클리어
 * </ul>
 *
 * <p><strong>트랜잭션 경계</strong>:
 *
 * <ul>
 *   <li>저장 작업은 하나의 트랜잭션에서 처리
 *   <li>이벤트 발행은 TransactionEventRegistry를 통해 커밋 후 발행 (APP-ER-002, APP-ER-005)
 * </ul>
 */
@Component
public class ExternalDownloadFacade {

    private static final Logger log = LoggerFactory.getLogger(ExternalDownloadFacade.class);

    private final ExternalDownloadTransactionManager externalDownloadTransactionManager;
    private final ExternalDownloadOutboxTransactionManager outboxTransactionManager;
    private final TransactionEventRegistry transactionEventRegistry;

    public ExternalDownloadFacade(
            ExternalDownloadTransactionManager externalDownloadTransactionManager,
            ExternalDownloadOutboxTransactionManager outboxTransactionManager,
            TransactionEventRegistry transactionEventRegistry) {
        this.externalDownloadTransactionManager = externalDownloadTransactionManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.transactionEventRegistry = transactionEventRegistry;
    }

    /**
     * ExternalDownloadBundle을 저장하고 이벤트를 발행합니다.
     *
     * <p>저장 순서:
     *
     * <ol>
     *   <li>ExternalDownload 저장
     *   <li>Outbox 저장
     *   <li>등록된 도메인 이벤트 발행
     *   <li>도메인 이벤트 클리어
     * </ol>
     *
     * @param bundle 저장할 ExternalDownloadBundle (Download + Outbox + 이벤트)
     * @return 저장된 ExternalDownloadId
     */
    @Transactional
    public ExternalDownloadId saveAndPublishEvent(ExternalDownloadBundle bundle) {
        ExternalDownload download = bundle.download();

        log.info("ExternalDownload 저장 및 이벤트 발행 시작: sourceUrl={}", download.getSourceUrl().value());

        // 1. 도메인 이벤트 등록 (커밋 후 발행, APP-ER-002, APP-ER-005)
        // 이벤트는 원래 download 객체에서 가져옴 (persist 후 새 객체에는 이벤트 없음)
        int eventCount = download.getDomainEvents().size();
        for (DomainEvent event : download.getDomainEvents()) {
            log.debug("Registering event for publish: type={}", event.getClass().getSimpleName());
            transactionEventRegistry.registerForPublish(event);
        }
        download.clearDomainEvents();

        // 2. ExternalDownload 저장
        ExternalDownload savedDownload = externalDownloadTransactionManager.persist(download);
        ExternalDownloadId savedId = savedDownload.getId();
        log.debug("ExternalDownload 저장 완료: downloadId={}", savedId.value());

        // 3. Outbox 저장
        outboxTransactionManager.persist(bundle.outbox());
        log.debug("Outbox 저장 완료: downloadId={}", savedId.value());

        log.info(
                "ExternalDownload 저장 및 이벤트 발행 완료: downloadId={}, eventCount={}",
                savedId.value(),
                eventCount);
        return savedId;
    }
}
