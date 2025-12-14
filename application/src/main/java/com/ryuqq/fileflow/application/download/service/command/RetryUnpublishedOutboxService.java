package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.download.dto.ExternalDownloadMessage;
import com.ryuqq.fileflow.application.download.factory.command.ExternalDownloadCommandFactory;
import com.ryuqq.fileflow.application.download.manager.command.ExternalDownloadOutboxTransactionManager;
import com.ryuqq.fileflow.application.download.manager.query.ExternalDownloadOutboxReadManager;
import com.ryuqq.fileflow.application.download.manager.query.ExternalDownloadReadManager;
import com.ryuqq.fileflow.application.download.port.in.command.RetryUnpublishedOutboxUseCase;
import com.ryuqq.fileflow.application.download.port.out.client.ExternalDownloadSqsPublishPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * 미발행 Outbox 재시도 서비스.
 *
 * <p>SQS 발행에 실패한 Outbox를 조회하여 재발행을 시도합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>미발행 상태의 Outbox 조회
 *   <li>각 Outbox에 대해 SQS 메시지 발행 시도
 *   <li>성공 시 markAsPublished, 실패 시 로깅 후 다음 시도에 재시도
 * </ol>
 *
 * <p><strong>조건부 등록</strong>:
 *
 * <ul>
 *   <li>ExternalDownloadSqsPublishPort 빈이 존재할 때만 등록됩니다.
 *   <li>resizing-worker 등 SQS 발행 기능이 없는 모듈에서는 비활성화됩니다.
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
@Service
@ConditionalOnBean(ExternalDownloadSqsPublishPort.class)
public class RetryUnpublishedOutboxService implements RetryUnpublishedOutboxUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryUnpublishedOutboxService.class);

    private static final int BATCH_SIZE = 100;
    private static final int MAX_ITERATIONS = 100;

    private final ExternalDownloadOutboxReadManager outboxReadManager;
    private final ExternalDownloadReadManager downloadReadManager;
    private final ExternalDownloadOutboxTransactionManager outboxTransactionManager;
    private final ExternalDownloadCommandFactory commandFactory;
    private final ExternalDownloadSqsPublishPort sqsPublishPort;

    public RetryUnpublishedOutboxService(
            ExternalDownloadOutboxReadManager outboxReadManager,
            ExternalDownloadReadManager downloadReadManager,
            ExternalDownloadOutboxTransactionManager outboxTransactionManager,
            ExternalDownloadCommandFactory commandFactory,
            ExternalDownloadSqsPublishPort sqsPublishPort) {
        this.outboxReadManager = outboxReadManager;
        this.downloadReadManager = downloadReadManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.commandFactory = commandFactory;
        this.sqsPublishPort = sqsPublishPort;
    }

    @Override
    public RetryResult execute() {
        int totalRetried = 0;
        int totalSucceeded = 0;
        int totalFailed = 0;
        int iteration = 0;

        List<ExternalDownloadOutbox> unpublishedOutboxes =
                outboxReadManager.findUnpublished(BATCH_SIZE);

        while (!unpublishedOutboxes.isEmpty() && iteration < MAX_ITERATIONS) {
            iteration++;

            for (ExternalDownloadOutbox outbox : unpublishedOutboxes) {
                totalRetried++;

                try {
                    boolean success = retryPublish(outbox);

                    if (success) {
                        commandFactory.markAsPublished(outbox);
                        outboxTransactionManager.persist(outbox);
                        totalSucceeded++;
                        log.debug(
                                "Successfully retried outbox: externalDownloadId={}",
                                outbox.getExternalDownloadId().value());
                    } else {
                        totalFailed++;
                        log.warn(
                                "Failed to retry outbox (publish returned false):"
                                        + " externalDownloadId={}",
                                outbox.getExternalDownloadId().value());
                    }
                } catch (Exception e) {
                    totalFailed++;
                    log.warn(
                            "Failed to retry outbox: externalDownloadId={}, error={}",
                            outbox.getExternalDownloadId().value(),
                            e.getMessage());
                }
            }

            if (unpublishedOutboxes.size() < BATCH_SIZE) {
                break;
            }

            unpublishedOutboxes = outboxReadManager.findUnpublished(BATCH_SIZE);
        }

        if (iteration >= MAX_ITERATIONS) {
            log.warn("[OutboxRetry] MAX_ITERATIONS({}) 도달, 다음 주기에 계속", MAX_ITERATIONS);
        }

        return new RetryResult(totalRetried, totalSucceeded, totalFailed, iteration);
    }

    private boolean retryPublish(ExternalDownloadOutbox outbox) {
        ExternalDownload download =
                downloadReadManager.findById(outbox.getExternalDownloadId()).orElse(null);

        if (download == null) {
            log.warn(
                    "ExternalDownload not found for outbox: externalDownloadId={}",
                    outbox.getExternalDownloadId().value());
            return false;
        }

        ExternalDownloadMessage message = toMessage(download);
        return sqsPublishPort.publish(message);
    }

    private ExternalDownloadMessage toMessage(ExternalDownload download) {
        return new ExternalDownloadMessage(
                download.getId().value().toString(),
                download.getSourceUrl().value(),
                download.getTenantId().value(),
                download.getOrganizationId().value());
    }
}
