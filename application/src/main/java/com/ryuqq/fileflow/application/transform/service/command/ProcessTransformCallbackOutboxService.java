package com.ryuqq.fileflow.application.transform.service.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.exception.PermanentCallbackFailureException;
import com.ryuqq.fileflow.application.transform.dto.response.TransformCallbackPayload;
import com.ryuqq.fileflow.application.transform.manager.client.TransformCallbackNotificationManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformCallbackOutboxReadManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformReadManager;
import com.ryuqq.fileflow.application.transform.port.in.command.ProcessTransformCallbackOutboxUseCase;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessTransformCallbackOutboxService
        implements ProcessTransformCallbackOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ProcessTransformCallbackOutboxService.class);

    private final TransformCallbackOutboxReadManager transformCallbackOutboxReadManager;
    private final TransformCallbackOutboxCommandManager transformCallbackOutboxCommandManager;
    private final TransformCallbackNotificationManager transformCallbackNotificationManager;
    private final TransformReadManager transformReadManager;

    public ProcessTransformCallbackOutboxService(
            TransformCallbackOutboxReadManager transformCallbackOutboxReadManager,
            TransformCallbackOutboxCommandManager transformCallbackOutboxCommandManager,
            TransformCallbackNotificationManager transformCallbackNotificationManager,
            TransformReadManager transformReadManager) {
        this.transformCallbackOutboxReadManager = transformCallbackOutboxReadManager;
        this.transformCallbackOutboxCommandManager = transformCallbackOutboxCommandManager;
        this.transformCallbackNotificationManager = transformCallbackNotificationManager;
        this.transformReadManager = transformReadManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(int batchSize) {
        List<TransformCallbackOutbox> pending =
                transformCallbackOutboxReadManager.findPendingMessages(batchSize);
        if (pending.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        int success = 0;
        int failed = 0;

        Instant now = Instant.now();
        for (TransformCallbackOutbox outbox : pending) {
            try {
                TransformCallbackPayload payload = buildPayload(outbox);
                transformCallbackNotificationManager.notify(outbox.callbackUrl(), payload);
                outbox.markSent(now);
                success++;
            } catch (PermanentCallbackFailureException e) {
                log.warn(
                        "변환 콜백 영구 실패 (재시도 불가): outboxId={}, transformRequestId={},"
                                + " callbackUrl={}",
                        outbox.idValue(),
                        outbox.transformRequestId(),
                        outbox.callbackUrl(),
                        e);
                outbox.markFailedPermanently(e.getMessage(), now);
                failed++;
            } catch (Exception e) {
                log.error(
                        "변환 콜백 전송 실패 (재시도 예정): outboxId={}, transformRequestId={},"
                                + " callbackUrl={}, retry={}/{}",
                        outbox.idValue(),
                        outbox.transformRequestId(),
                        outbox.callbackUrl(),
                        outbox.retryCount(),
                        outbox.maxRetries(),
                        e);
                outbox.markFailed(e.getMessage(), now);
                failed++;
            }
            transformCallbackOutboxCommandManager.persist(outbox);
        }

        return SchedulerBatchProcessingResult.of(pending.size(), success, failed);
    }

    private TransformCallbackPayload buildPayload(TransformCallbackOutbox outbox) {
        TransformRequest request =
                transformReadManager.getTransformRequest(outbox.transformRequestId());

        if ("COMPLETED".equals(outbox.taskStatus())) {
            return TransformCallbackPayload.ofCompleted(
                    request.idValue(),
                    request.sourceAssetIdValue(),
                    request.resultAssetIdValue(),
                    request.type().name(),
                    request.params().width(),
                    request.params().height(),
                    request.params().quality(),
                    request.params().targetFormat());
        }

        return TransformCallbackPayload.ofFailed(
                request.idValue(), request.sourceAssetIdValue(), request.lastError());
    }
}
