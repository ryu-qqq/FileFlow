package com.ryuqq.fileflow.adapter.in.sqs.listener;

import com.ryuqq.fileflow.adapter.in.sqs.metrics.ExternalDownloadMetrics;
import com.ryuqq.fileflow.application.common.lock.DistributedLockExecutor;
import com.ryuqq.fileflow.application.common.lock.LockType;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadMessage;
import com.ryuqq.fileflow.application.download.dto.command.ExecuteExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.port.in.command.ExecuteExternalDownloadUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * External Download SQS Listener.
 *
 * <p>SQS 메시지를 수신하여 외부 URL 다운로드를 실행합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>SQS 메시지 수신 (ExternalDownloadMessage)
 *   <li>분산락 획득 시도 (중복 처리 방지)
 *   <li>락 획득 성공: ExecuteExternalDownloadUseCase 실행
 *   <li>락 획득 실패: ACK 후 skip (다른 워커 처리 중)
 * </ol>
 *
 * <p><strong>실패 처리</strong>:
 *
 * <ul>
 *   <li>예외 발생 시: ACK 미전송 → SQS 재시도
 *   <li>3회 실패: DLQ로 이동
 * </ul>
 *
 * <p><strong>메트릭 수집</strong>:
 *
 * <ul>
 *   <li>external.download.total - 총 메시지 수신 수
 *   <li>external.download.success - 성공 처리 수
 *   <li>external.download.failure - 실패 처리 수
 *   <li>external.download.lock.skipped - 락 스킵 수
 *   <li>external.download.duration - 처리 시간
 *   <li>external.download.lock.duration - 락 점유 시간
 * </ul>
 *
 * <p><strong>분산락 설정</strong>:
 *
 * <ul>
 *   <li>waitTime: 0ms (즉시 반환)
 *   <li>leaseTime: 300초 (5분)
 * </ul>
 */
@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.external-download-listener-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ExternalDownloadSqsListener {

    private static final Logger log = LoggerFactory.getLogger(ExternalDownloadSqsListener.class);

    private final DistributedLockExecutor lockExecutor;
    private final ExecuteExternalDownloadUseCase downloadUseCase;
    private final ExternalDownloadMetrics metrics;

    public ExternalDownloadSqsListener(
            DistributedLockExecutor lockExecutor,
            ExecuteExternalDownloadUseCase downloadUseCase,
            ExternalDownloadMetrics metrics) {
        this.lockExecutor = lockExecutor;
        this.downloadUseCase = downloadUseCase;
        this.metrics = metrics;
    }

    /**
     * SQS 메시지를 수신하고 다운로드를 실행합니다.
     *
     * <p>MANUAL acknowledgement 모드를 사용하여 처리 완료 후 명시적으로 ACK를 전송합니다.
     *
     * @param payload SQS 메시지 페이로드
     * @param acknowledgement ACK 핸들러
     */
    @SqsListener(
            value = "${aws.sqs.listener.external-download-queue-url}",
            acknowledgementMode = "MANUAL")
    public void handleMessage(
            @Payload ExternalDownloadMessage payload, Acknowledgement acknowledgement) {

        Long downloadId = payload.externalDownloadId();
        long startTime = System.currentTimeMillis();

        log.info("[ExternalDownload] 메시지 수신: id={}, sourceUrl={}", downloadId, payload.sourceUrl());
        metrics.recordMessageReceived();

        try {
            processWithLock(payload, acknowledgement, downloadId, startTime);
        } catch (Exception e) {
            handleException(downloadId, startTime, e);
        }
    }

    /** 분산락을 획득하고 다운로드를 처리합니다. */
    private void processWithLock(
            ExternalDownloadMessage payload,
            Acknowledgement acknowledgement,
            Long downloadId,
            long startTime) {

        long lockStartTime = System.currentTimeMillis();

        boolean executed =
                lockExecutor.tryExecuteWithLock(
                        LockType.EXTERNAL_DOWNLOAD,
                        downloadId,
                        () -> executeDownloadWithMetrics(payload, startTime));

        long lockDuration = System.currentTimeMillis() - lockStartTime;

        if (!executed) {
            handleLockSkipped(acknowledgement, downloadId);
            return;
        }

        handleSuccess(acknowledgement, downloadId, startTime, lockDuration);
    }

    /** 다운로드를 실행하고 메트릭을 기록합니다. */
    private void executeDownloadWithMetrics(ExternalDownloadMessage payload, long startTime) {
        ExecuteExternalDownloadCommand command =
                new ExecuteExternalDownloadCommand(payload.externalDownloadId());

        log.debug(
                "[ExternalDownload] UseCase 실행 시작: id={}, elapsedMs={}",
                payload.externalDownloadId(),
                System.currentTimeMillis() - startTime);

        downloadUseCase.execute(command);
    }

    /** 락 획득 실패 시 처리. */
    private void handleLockSkipped(Acknowledgement acknowledgement, Long downloadId) {
        acknowledgement.acknowledge();
        metrics.recordLockSkipped();

        log.info("[ExternalDownload] 스킵 (다른 워커 처리 중): id={}, reason=LOCK_CONTENTION", downloadId);
    }

    /** 성공 처리. */
    private void handleSuccess(
            Acknowledgement acknowledgement, Long downloadId, long startTime, long lockDuration) {

        acknowledgement.acknowledge();

        long totalDuration = System.currentTimeMillis() - startTime;
        metrics.recordSuccess();
        metrics.recordDownloadDuration(totalDuration);
        metrics.recordLockDuration(lockDuration);

        log.info(
                "[ExternalDownload] 완료: id={}, totalDurationMs={}, lockDurationMs={}",
                downloadId,
                totalDuration,
                lockDuration);
    }

    /**
     * 예외 처리.
     *
     * <p>ACK를 전송하지 않아 SQS가 재시도하도록 합니다. 3회 실패 시 DLQ로 이동됩니다.
     */
    private void handleException(Long downloadId, long startTime, Exception e) {
        long totalDuration = System.currentTimeMillis() - startTime;
        metrics.recordFailure();
        metrics.recordDownloadDuration(totalDuration);

        log.error(
                "[ExternalDownload] 실패 (SQS 재시도 예정): id={}, durationMs={}, error={}",
                downloadId,
                totalDuration,
                e.getMessage(),
                e);

        // ACK 미전송 → SQS visibility_timeout 후 재전달
        // 3회 실패 시 DLQ로 이동
        throw new ExternalDownloadProcessingException(
                "External download failed: id=" + downloadId, e);
    }
}
