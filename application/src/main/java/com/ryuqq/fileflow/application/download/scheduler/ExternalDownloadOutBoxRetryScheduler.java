package com.ryuqq.fileflow.application.download.scheduler;

import com.ryuqq.fileflow.application.common.metrics.SchedulerMetrics;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadMessage;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadOutboxManager;
import com.ryuqq.fileflow.application.download.port.out.client.SqsPublishPort;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ExternalDownload Outbox 재시도 스케줄러.
 *
 * <p>SQS 발행에 실패한 Outbox를 주기적으로 조회하여 재시도합니다.
 *
 * <p><strong>실행 주기</strong>: 5분
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>미발행(published=false) 상태의 Outbox 조회
 *   <li>각 Outbox에 대해 SQS 메시지 발행 시도
 *   <li>성공 시 markAsPublished, 실패 시 로깅 후 다음 주기에 재시도
 * </ol>
 *
 * <p><strong>폴백 역할</strong>: {@link
 * com.ryuqq.fileflow.application.download.listener.ExternalDownloadRegisteredEventListener} 에서 SQS
 * 발행 실패 시 이 스케줄러가 재시도합니다.
 *
 * <p><strong>활성화 조건</strong>: {@code scheduler.outbox-retry.enabled=true}
 */
@Component
@ConditionalOnProperty(
        name = "scheduler.outbox-retry.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class ExternalDownloadOutBoxRetryScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(ExternalDownloadOutBoxRetryScheduler.class);

    private static final String JOB_NAME = "outbox-retry";
    private static final int BATCH_SIZE = 100;

    private final ExternalDownloadOutboxQueryPort outboxQueryPort;
    private final ExternalDownloadQueryPort downloadQueryPort;
    private final ExternalDownloadOutboxManager outboxManager;
    private final SqsPublishPort sqsPublishPort;
    private final SchedulerMetrics schedulerMetrics;

    public ExternalDownloadOutBoxRetryScheduler(
            ExternalDownloadOutboxQueryPort outboxQueryPort,
            ExternalDownloadQueryPort downloadQueryPort,
            ExternalDownloadOutboxManager outboxManager,
            SqsPublishPort sqsPublishPort,
            SchedulerMetrics schedulerMetrics) {
        this.outboxQueryPort = outboxQueryPort;
        this.downloadQueryPort = downloadQueryPort;
        this.outboxManager = outboxManager;
        this.sqsPublishPort = sqsPublishPort;
        this.schedulerMetrics = schedulerMetrics;
    }

    /**
     * 미발행 Outbox를 조회하여 SQS로 재발행합니다.
     *
     * <p>5분마다 실행됩니다.
     */
    @Scheduled(fixedRate = 300000) // 5분
    public void retryUnpublishedOutboxes() {
        log.info("Starting ExternalDownload outbox retry");
        Timer.Sample sample = schedulerMetrics.startJob(JOB_NAME);

        int totalRetried = 0;
        int totalSucceeded = 0;
        int totalFailed = 0;

        try {
            List<ExternalDownloadOutbox> unpublishedOutboxes =
                    outboxQueryPort.findUnpublished(BATCH_SIZE);

            while (!unpublishedOutboxes.isEmpty()) {
                for (ExternalDownloadOutbox outbox : unpublishedOutboxes) {
                    totalRetried++;

                    try {
                        boolean success = retryPublish(outbox);

                        if (success) {
                            outboxManager.markAsPublished(outbox);
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

                unpublishedOutboxes = outboxQueryPort.findUnpublished(BATCH_SIZE);
            }

            // 메트릭 기록: 처리된 항목 수
            schedulerMetrics.recordJobItemsProcessed(JOB_NAME, totalRetried);
            schedulerMetrics.recordJobSuccess(JOB_NAME, sample);

            log.info(
                    "ExternalDownload outbox retry completed."
                            + " Retried: {}, Succeeded: {}, Failed: {}",
                    totalRetried,
                    totalSucceeded,
                    totalFailed);

        } catch (Exception e) {
            schedulerMetrics.recordJobFailure(JOB_NAME, sample, e.getClass().getSimpleName());
            log.error("ExternalDownload outbox retry failed", e);
            throw e;
        }
    }

    private boolean retryPublish(ExternalDownloadOutbox outbox) {
        ExternalDownload download =
                downloadQueryPort.findById(outbox.getExternalDownloadId()).orElse(null);

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
                download.getId().value(),
                download.getSourceUrl().value(),
                download.getTenantId(),
                download.getOrganizationId());
    }
}
