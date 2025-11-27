package com.ryuqq.fileflow.adapter.in.sqs.listener;

import com.ryuqq.fileflow.adapter.in.sqs.metrics.ExternalDownloadDlqMetrics;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadMessage;
import com.ryuqq.fileflow.application.download.port.in.command.MarkExternalDownloadAsFailedUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * External Download DLQ Listener.
 *
 * <p>Dead Letter Queue 메시지를 수신하여 최종 실패 처리를 수행합니다.
 *
 * <p><strong>DLQ 도착 조건</strong>:
 *
 * <ul>
 *   <li>메인 큐에서 3회 처리 실패 (maxReceiveCount = 3)
 *   <li>Visibility Timeout 내 ACK 미전송
 * </ul>
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>DLQ 메시지 수신 (ExternalDownloadMessage)
 *   <li>ExternalDownload 상태를 FAILED로 변경
 *   <li>디폴트 이미지 할당
 *   <li>ACK 전송
 * </ol>
 *
 * <p><strong>메트릭 수집</strong>:
 *
 * <ul>
 *   <li>external.download.dlq.total - DLQ 메시지 수신 수
 *   <li>external.download.dlq.processed - 실패 처리 완료 수
 *   <li>external.download.dlq.error - DLQ 처리 중 에러 발생 수
 *   <li>external.download.dlq.duration - DLQ 처리 시간
 * </ul>
 *
 * <p><strong>에러 메시지</strong>: "DLQ: 최대 재시도 횟수 초과"
 */
@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.external-download-dlq-listener-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ExternalDownloadDlqListener {

    private static final Logger log = LoggerFactory.getLogger(ExternalDownloadDlqListener.class);

    private static final String DLQ_ERROR_MESSAGE = "DLQ: 최대 재시도 횟수 초과";

    private final MarkExternalDownloadAsFailedUseCase markAsFailedUseCase;
    private final ExternalDownloadDlqMetrics metrics;

    public ExternalDownloadDlqListener(
            MarkExternalDownloadAsFailedUseCase markAsFailedUseCase,
            ExternalDownloadDlqMetrics metrics) {
        this.markAsFailedUseCase = markAsFailedUseCase;
        this.metrics = metrics;
    }

    /**
     * DLQ 메시지를 수신하고 최종 실패 처리를 수행합니다.
     *
     * <p>MANUAL acknowledgement 모드를 사용하여 처리 완료 후 명시적으로 ACK를 전송합니다.
     *
     * @param payload DLQ 메시지 페이로드
     * @param acknowledgement ACK 핸들러
     */
    @SqsListener(
            value = "${aws.sqs.listener.external-download-dlq-url}",
            acknowledgementMode = "MANUAL")
    public void handleMessage(
            @Payload ExternalDownloadMessage payload, Acknowledgement acknowledgement) {

        Long downloadId = payload.externalDownloadId();
        long startTime = System.currentTimeMillis();

        log.warn(
                "[ExternalDownload:DLQ] 메시지 수신 (최종 실패): id={}, sourceUrl={}",
                downloadId,
                payload.sourceUrl());
        metrics.recordMessageReceived();

        try {
            markAsFailed(payload);
            handleSuccess(downloadId, startTime);
        } catch (Exception e) {
            handleError(downloadId, startTime, e);
        }

        // DLQ는 무한 재시도 방지를 위해 항상 ACK
        acknowledgement.acknowledge();
    }

    /** ExternalDownload를 FAILED 상태로 변경합니다. */
    private void markAsFailed(ExternalDownloadMessage payload) {
        markAsFailedUseCase.markAsFailed(payload.externalDownloadId(), DLQ_ERROR_MESSAGE);
    }

    /** 성공 처리. */
    private void handleSuccess(Long downloadId, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        metrics.recordProcessed();
        metrics.recordDuration(duration);

        log.info(
                "[ExternalDownload:DLQ] FAILED 상태 변경 완료: id={}, durationMs={}",
                downloadId,
                duration);
    }

    /** 에러 처리. */
    private void handleError(Long downloadId, long startTime, Exception e) {
        long duration = System.currentTimeMillis() - startTime;
        metrics.recordError();
        metrics.recordDuration(duration);

        log.error(
                "[ExternalDownload:DLQ] 실패 처리 중 오류 (ACK 처리): id={}, durationMs={}, error={}",
                downloadId,
                duration,
                e.getMessage(),
                e);
    }
}
