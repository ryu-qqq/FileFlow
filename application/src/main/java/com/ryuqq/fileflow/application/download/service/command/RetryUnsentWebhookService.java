package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.download.manager.query.WebhookOutboxReadManager;
import com.ryuqq.fileflow.application.download.port.in.command.RetryUnsentWebhookUseCase;
import com.ryuqq.fileflow.application.download.service.WebhookSendService;
import com.ryuqq.fileflow.domain.download.aggregate.WebhookOutbox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 미발송 Webhook Outbox 재시도 서비스.
 *
 * <p>PENDING 상태의 WebhookOutbox를 조회하여 재시도합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>PENDING 상태, retryCount < MAX 인 Outbox 조회
 *   <li>각 Outbox에 대해 WebhookSendService 호출
 *   <li>성공/실패 카운트 집계
 *   <li>더 이상 처리할 Outbox가 없거나 MAX_ITERATIONS 도달 시 종료
 * </ol>
 */
@Service
public class RetryUnsentWebhookService implements RetryUnsentWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryUnsentWebhookService.class);

    private static final int BATCH_SIZE = 100;
    private static final int MAX_ITERATIONS = 10;

    private final WebhookOutboxReadManager webhookOutboxReadManager;
    private final WebhookSendService webhookSendService;

    public RetryUnsentWebhookService(
            WebhookOutboxReadManager webhookOutboxReadManager,
            WebhookSendService webhookSendService) {
        this.webhookOutboxReadManager = webhookOutboxReadManager;
        this.webhookSendService = webhookSendService;
    }

    @Override
    public RetryResult execute() {
        int totalRetried = 0;
        int succeeded = 0;
        int failed = 0;
        int iterations = 0;

        while (iterations < MAX_ITERATIONS) {
            iterations++;

            List<WebhookOutbox> outboxes = webhookOutboxReadManager.findPendingForRetry(BATCH_SIZE);

            if (outboxes.isEmpty()) {
                log.info("[WebhookRetry] 재시도 대상 없음, iteration={}", iterations);
                break;
            }

            log.info(
                    "[WebhookRetry] 배치 처리 시작: iteration={}, count={}", iterations, outboxes.size());

            for (WebhookOutbox outbox : outboxes) {
                totalRetried++;
                try {
                    webhookSendService.send(outbox);
                    succeeded++;
                } catch (Exception e) {
                    failed++;
                    log.warn(
                            "[WebhookRetry] 재시도 실패: outboxId={}, error={}",
                            outbox.getIdValue(),
                            e.getMessage());
                }
            }
        }

        return new RetryResult(totalRetried, succeeded, failed, iterations);
    }
}
