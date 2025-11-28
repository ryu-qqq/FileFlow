package com.ryuqq.fileflow.application.download.listener;

import com.ryuqq.fileflow.application.download.manager.ExternalDownloadMessageManager;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadOutboxManager;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadRegisteredEvent;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 외부 다운로드 등록 이벤트 리스너.
 *
 * <p>트랜잭션 커밋 후 SQS 메시지를 발행합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>ExternalDownloadRegisteredEvent 수신 (커밋 후)
 *   <li>Outbox 조회
 *   <li>SQS 메시지 발행
 *   <li>발행 성공 시 Outbox 상태 업데이트 (markAsPublished)
 *   <li>발행 실패 시 로그 기록 (재시도 스케줄러에서 처리)
 * </ol>
 *
 * <p><strong>활성화 조건</strong>: {@code sqs.publish.enabled=true}
 */
@Component
@ConditionalOnProperty(name = "sqs.publish.enabled", havingValue = "true")
public class ExternalDownloadRegisteredEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(ExternalDownloadRegisteredEventListener.class);

    private final ExternalDownloadOutboxQueryPort outboxQueryPort;
    private final ExternalDownloadOutboxManager outboxManager;
    private final ExternalDownloadMessageManager messageManager;

    public ExternalDownloadRegisteredEventListener(
            ExternalDownloadOutboxQueryPort outboxQueryPort,
            ExternalDownloadOutboxManager outboxManager,
            ExternalDownloadMessageManager messageManager) {
        this.outboxQueryPort = outboxQueryPort;
        this.outboxManager = outboxManager;
        this.messageManager = messageManager;
    }

    /**
     * 외부 다운로드 등록 이벤트 처리.
     *
     * <p>트랜잭션 커밋 후 실행되어 SQS 메시지를 발행합니다.
     *
     * @param event ExternalDownloadRegisteredEvent
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleExternalDownloadRegistered(ExternalDownloadRegisteredEvent event) {
        ExternalDownloadId downloadId = event.downloadId();

        log.info(
                "ExternalDownload 등록 이벤트 처리 시작: downloadId={}, sourceUrl={}",
                downloadId.value(),
                event.sourceUrl().value());

        ExternalDownloadOutbox outbox =
                outboxQueryPort.findByExternalDownloadId(downloadId).orElse(null);

        if (outbox == null) {
            log.warn("Outbox를 찾을 수 없습니다: downloadId={}", downloadId.value());
            return;
        }

        try {
            // SQS 메시지 발행
            boolean published = messageManager.publishFromEvent(event);

            if (published) {
                // 성공 시 Outbox 상태 업데이트
                outboxManager.markAsPublished(outbox);

                log.info("ExternalDownload SQS 발행 완료: downloadId={}", downloadId.value());
            } else {
                log.warn(
                        "ExternalDownload SQS 발행 실패 (반환값 false): downloadId={}",
                        downloadId.value());
                outboxManager.markAsFailed(outbox);
            }
        } catch (Exception e) {
            // 실패 시 로그 기록 (재시도 스케줄러에서 처리)
            log.error(
                    "ExternalDownload SQS 발행 실패: downloadId={}, error={}",
                    downloadId.value(),
                    e.getMessage());
            outboxManager.markAsFailed(outbox);
        }
    }
}
