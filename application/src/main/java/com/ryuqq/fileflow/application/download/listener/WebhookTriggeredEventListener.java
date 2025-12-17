package com.ryuqq.fileflow.application.download.listener;

import com.ryuqq.fileflow.application.download.manager.command.WebhookOutboxTransactionManager;
import com.ryuqq.fileflow.application.download.service.WebhookSendService;
import com.ryuqq.fileflow.domain.download.aggregate.WebhookOutbox;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadWebhookTriggeredEvent;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxId;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Webhook 트리거 이벤트 리스너.
 *
 * <p>ExternalDownload 완료/실패 시 발생하는 WebhookTriggeredEvent를 처리합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>원본 트랜잭션 커밋 후 이벤트 수신 (AFTER_COMMIT)
 *   <li>WebhookOutbox 생성 및 저장 (PENDING)
 *   <li>WebhookSendService를 통해 비동기로 webhook 호출
 * </ol>
 */
@Component
public class WebhookTriggeredEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebhookTriggeredEventListener.class);

    private final WebhookOutboxTransactionManager webhookOutboxTransactionManager;
    private final WebhookSendService webhookSendService;
    private final Clock clock;

    public WebhookTriggeredEventListener(
            WebhookOutboxTransactionManager webhookOutboxTransactionManager,
            WebhookSendService webhookSendService,
            Clock clock) {
        this.webhookOutboxTransactionManager = webhookOutboxTransactionManager;
        this.webhookSendService = webhookSendService;
        this.clock = clock;
    }

    /**
     * Webhook 트리거 이벤트를 처리합니다.
     *
     * <p>원본 트랜잭션 커밋 후 실행됩니다.
     *
     * <p><strong>트랜잭션 전파</strong>: REQUIRES_NEW를 사용하여 독립적인 트랜잭션에서 실행됩니다.
     *
     * <p><strong>처리 내용</strong>:
     *
     * <ul>
     *   <li>WebhookOutbox 생성 및 저장
     *   <li>WebhookSendService를 통해 webhook 호출 (비동기)
     * </ul>
     *
     * @param event Webhook 트리거 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(ExternalDownloadWebhookTriggeredEvent event) {
        log.info(
                "Webhook 트리거 이벤트 수신: downloadId={}, webhookUrl={}, status={}",
                event.downloadId().value(),
                event.webhookUrl().value(),
                event.status());

        try {
            // WebhookOutbox 생성 및 저장
            WebhookOutbox outbox =
                    WebhookOutbox.forNew(
                            event.downloadId(),
                            event.webhookUrl(),
                            event.status(),
                            event.fileAssetId(),
                            event.errorMessage(),
                            clock);

            WebhookOutboxId outboxId = webhookOutboxTransactionManager.persist(outbox);

            log.info(
                    "WebhookOutbox 생성 완료: outboxId={}, downloadId={}",
                    outboxId.value(),
                    event.downloadId().value());

            // 비동기로 webhook 호출 시도
            sendWebhookAsync(outbox);

        } catch (Exception e) {
            log.error(
                    "WebhookOutbox 생성 실패: downloadId={}, error={}",
                    event.downloadId().value(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }

    /**
     * Webhook을 비동기로 호출합니다.
     *
     * @param outbox WebhookOutbox
     */
    @Async
    public void sendWebhookAsync(WebhookOutbox outbox) {
        webhookSendService.send(outbox);
    }
}
