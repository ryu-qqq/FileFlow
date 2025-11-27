package com.ryuqq.fileflow.application.download.facade;

import com.ryuqq.fileflow.application.download.dto.ExternalDownloadBundle;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadManager;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadOutboxManager;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import org.springframework.context.ApplicationEventPublisher;
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
 *   <li>이벤트 발행은 ApplicationEventPublisher를 통해 동기 발행
 *   <li>@TransactionalEventListener를 통해 커밋 후 처리 가능
 * </ul>
 */
@Component
public class ExternalDownloadFacade {

    private final ExternalDownloadManager externalDownloadManager;
    private final ExternalDownloadOutboxManager externalDownloadOutboxManager;
    private final ApplicationEventPublisher eventPublisher;

    public ExternalDownloadFacade(
            ExternalDownloadManager externalDownloadManager,
            ExternalDownloadOutboxManager externalDownloadOutboxManager,
            ApplicationEventPublisher eventPublisher) {
        this.externalDownloadManager = externalDownloadManager;
        this.externalDownloadOutboxManager = externalDownloadOutboxManager;
        this.eventPublisher = eventPublisher;
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

        // 1. ExternalDownload 저장
        ExternalDownloadId savedId = externalDownloadManager.save(download);

        // 2. Outbox 저장
        externalDownloadOutboxManager.save(bundle.outbox());

        // 3. 도메인 이벤트 발행
        for (DomainEvent event : download.getDomainEvents()) {
            eventPublisher.publishEvent(event);
        }

        // 4. 도메인 이벤트 클리어
        download.clearDomainEvents();

        return savedId;
    }
}
