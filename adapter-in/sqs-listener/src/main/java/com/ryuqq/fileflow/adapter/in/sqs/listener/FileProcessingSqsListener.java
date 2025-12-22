package com.ryuqq.fileflow.adapter.in.sqs.listener;

import com.ryuqq.fileflow.adapter.in.sqs.metrics.FileProcessingMetrics;
import com.ryuqq.fileflow.application.asset.dto.command.ProcessFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.message.FileProcessingMessage;
import com.ryuqq.fileflow.application.asset.port.in.command.ProcessFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.port.in.command.RecordFileAssetErrorUseCase;
import com.ryuqq.fileflow.application.common.lock.DistributedLockExecutor;
import com.ryuqq.fileflow.application.common.lock.LockType;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * File Processing SQS Listener.
 *
 * <p>SQS 메시지를 수신하여 이미지 리사이징 처리를 실행합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>SQS 메시지 수신 (FileProcessingMessage)
 *   <li>분산락 획득 시도 (중복 처리 방지)
 *   <li>락 획득 성공: ProcessFileAssetUseCase 실행
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
 *   <li>file.processing.total - 총 메시지 수신 수
 *   <li>file.processing.success - 성공 처리 수
 *   <li>file.processing.failure - 실패 처리 수
 *   <li>file.processing.lock.skipped - 락 스킵 수
 *   <li>file.processing.duration - 처리 시간
 *   <li>file.processing.lock.duration - 락 점유 시간
 * </ul>
 *
 * <p><strong>분산락 설정</strong>:
 *
 * <ul>
 *   <li>waitTime: 0ms (즉시 반환)
 *   <li>leaseTime: 600초 (10분 - 이미지 리사이징 최대 시간)
 * </ul>
 */
@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.file-processing-listener-enabled",
        havingValue = "true",
        matchIfMissing = false)
@ConditionalOnBean(ProcessFileAssetUseCase.class)
public class FileProcessingSqsListener {

    private static final Logger log = LoggerFactory.getLogger(FileProcessingSqsListener.class);

    private final DistributedLockExecutor lockExecutor;
    private final ProcessFileAssetUseCase processFileAssetUseCase;
    private final RecordFileAssetErrorUseCase recordFileAssetErrorUseCase;
    private final FileProcessingMetrics metrics;

    public FileProcessingSqsListener(
            DistributedLockExecutor lockExecutor,
            ProcessFileAssetUseCase processFileAssetUseCase,
            RecordFileAssetErrorUseCase recordFileAssetErrorUseCase,
            FileProcessingMetrics metrics) {
        this.lockExecutor = lockExecutor;
        this.processFileAssetUseCase = processFileAssetUseCase;
        this.recordFileAssetErrorUseCase = recordFileAssetErrorUseCase;
        this.metrics = metrics;
    }

    /**
     * SQS 메시지를 수신하고 파일 가공을 실행합니다.
     *
     * <p>MANUAL acknowledgement 모드를 사용하여 처리 완료 후 명시적으로 ACK를 전송합니다.
     *
     * @param payload SQS 메시지 페이로드
     * @param acknowledgement ACK 핸들러
     */
    @SqsListener(
            value = "${aws.sqs.listener.file-processing-queue-url}",
            acknowledgementMode = "MANUAL")
    public void handleMessage(
            @Payload FileProcessingMessage payload, Acknowledgement acknowledgement) {

        String fileAssetId = payload.fileAssetId();
        long startTime = System.currentTimeMillis();

        log.info(
                "[FileProcessing] 메시지 수신: fileAssetId={}, outboxId={}, eventType={}",
                fileAssetId,
                payload.outboxId(),
                payload.eventType());
        metrics.recordMessageReceived();

        try {
            processWithLock(payload, acknowledgement, fileAssetId, startTime);
        } catch (Exception e) {
            handleException(fileAssetId, startTime, e);
        }
    }

    /** 분산락을 획득하고 파일 가공을 처리합니다. */
    private void processWithLock(
            FileProcessingMessage payload,
            Acknowledgement acknowledgement,
            String fileAssetId,
            long startTime) {

        long lockStartTime = System.currentTimeMillis();

        boolean executed =
                lockExecutor.tryExecuteWithLock(
                        LockType.FILE_PROCESSING,
                        fileAssetId,
                        () -> executeProcessingWithMetrics(payload, startTime));

        long lockDuration = System.currentTimeMillis() - lockStartTime;

        if (!executed) {
            handleLockSkipped(acknowledgement, fileAssetId);
            return;
        }

        handleSuccess(acknowledgement, fileAssetId, startTime, lockDuration);
    }

    /** 파일 가공을 실행하고 메트릭을 기록합니다. */
    private void executeProcessingWithMetrics(FileProcessingMessage payload, long startTime) {
        ProcessFileAssetCommand command = new ProcessFileAssetCommand(payload.fileAssetId());

        log.debug(
                "[FileProcessing] UseCase 실행 시작: fileAssetId={}, elapsedMs={}",
                payload.fileAssetId(),
                System.currentTimeMillis() - startTime);

        processFileAssetUseCase.execute(command);
    }

    /** 락 획득 실패 시 처리. */
    private void handleLockSkipped(Acknowledgement acknowledgement, String fileAssetId) {
        acknowledgement.acknowledge();
        metrics.recordLockSkipped();

        log.info(
                "[FileProcessing] 스킵 (다른 워커 처리 중): fileAssetId={}, reason=LOCK_CONTENTION",
                fileAssetId);
    }

    /** 성공 처리. */
    private void handleSuccess(
            Acknowledgement acknowledgement,
            String fileAssetId,
            long startTime,
            long lockDuration) {

        acknowledgement.acknowledge();

        long totalDuration = System.currentTimeMillis() - startTime;
        metrics.recordSuccess();
        metrics.recordProcessingDuration(totalDuration);
        metrics.recordLockDuration(lockDuration);

        log.info(
                "[FileProcessing] 완료: fileAssetId={}, totalDurationMs={}, lockDurationMs={}",
                fileAssetId,
                totalDuration,
                lockDuration);
    }

    /**
     * 예외 처리.
     *
     * <p>ACK를 전송하지 않아 SQS가 재시도하도록 합니다. 3회 실패 시 DLQ로 이동됩니다.
     *
     * <p>에러 메시지를 FileAsset에 저장하여 디버깅에 활용합니다.
     */
    private void handleException(String fileAssetId, long startTime, Exception e) {
        long totalDuration = System.currentTimeMillis() - startTime;
        metrics.recordFailure();
        metrics.recordProcessingDuration(totalDuration);

        // 에러 메시지를 FileAsset에 저장 (디버깅용)
        String errorMessage = buildErrorMessage(e);
        try {
            recordFileAssetErrorUseCase.recordError(fileAssetId, errorMessage);
        } catch (Exception recordError) {
            log.warn(
                    "[FileProcessing] 에러 메시지 저장 실패: fileAssetId={}, recordError={}",
                    fileAssetId,
                    recordError.getMessage());
        }

        log.error(
                "[FileProcessing] 실패 (SQS 재시도 예정): fileAssetId={}, durationMs={}, error={}",
                fileAssetId,
                totalDuration,
                e.getMessage(),
                e);

        throw new FileProcessingException("File processing failed: fileAssetId=" + fileAssetId, e);
    }

    /**
     * 예외로부터 상세 에러 메시지를 생성합니다.
     *
     * <p>root cause와 stack trace 정보를 포함합니다.
     */
    private String buildErrorMessage(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());

        Throwable cause = e.getCause();
        if (cause != null) {
            sb.append(" | Caused by: ")
                    .append(cause.getClass().getSimpleName())
                    .append(": ")
                    .append(cause.getMessage());
        }

        // Stack trace의 첫 3줄만 포함 (디버깅에 도움)
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length > 0) {
            sb.append(" | at ");
            int limit = Math.min(3, stackTrace.length);
            for (int i = 0; i < limit; i++) {
                if (i > 0) {
                    sb.append(" -> ");
                }
                sb.append(stackTrace[i].getClassName())
                        .append(".")
                        .append(stackTrace[i].getMethodName())
                        .append(":")
                        .append(stackTrace[i].getLineNumber());
            }
        }

        return sb.toString();
    }
}
