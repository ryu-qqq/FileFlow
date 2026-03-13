package com.ryuqq.fileflow.application.transform.service.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.exception.PermanentCallbackFailureException;
import com.ryuqq.fileflow.application.transform.dto.response.TransformCallbackPayload;
import com.ryuqq.fileflow.application.transform.manager.client.TransformCallbackNotificationManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformReadManager;
import com.ryuqq.fileflow.application.transform.port.in.command.ProcessTransformCallbackOutboxUseCase;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessTransformCallbackOutboxService
        implements ProcessTransformCallbackOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ProcessTransformCallbackOutboxService.class);

    private final TransformCallbackOutboxCommandManager transformCallbackOutboxCommandManager;
    private final TransformCallbackNotificationManager transformCallbackNotificationManager;
    private final TransformReadManager transformReadManager;

    public ProcessTransformCallbackOutboxService(
            TransformCallbackOutboxCommandManager transformCallbackOutboxCommandManager,
            TransformCallbackNotificationManager transformCallbackNotificationManager,
            TransformReadManager transformReadManager) {
        this.transformCallbackOutboxCommandManager = transformCallbackOutboxCommandManager;
        this.transformCallbackNotificationManager = transformCallbackNotificationManager;
        this.transformReadManager = transformReadManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(int batchSize) {
        List<TransformCallbackOutbox> claimed =
                transformCallbackOutboxCommandManager.claimPendingMessages(batchSize);
        if (claimed.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        List<String> claimedOutboxIds =
                claimed.stream().map(TransformCallbackOutbox::idValue).toList();

        try {
            ConcurrentLinkedQueue<String> successIds = new ConcurrentLinkedQueue<>();
            ConcurrentLinkedQueue<String> failedIds = new ConcurrentLinkedQueue<>();
            ConcurrentLinkedQueue<String> permanentFailedIds = new ConcurrentLinkedQueue<>();

            List<CompletableFuture<Void>> futures =
                    claimed.stream()
                            .map(
                                    outbox ->
                                            CompletableFuture.runAsync(
                                                    () -> {
                                                        try {
                                                            TransformCallbackPayload payload =
                                                                    buildPayload(outbox);
                                                            transformCallbackNotificationManager
                                                                    .notify(
                                                                            outbox.callbackUrl(),
                                                                            payload);
                                                            successIds.add(outbox.idValue());
                                                        } catch (
                                                                PermanentCallbackFailureException
                                                                        e) {
                                                            log.warn(
                                                                    "변환 콜백 영구 실패: outboxId={},"
                                                                            + " url={}",
                                                                    outbox.idValue(),
                                                                    outbox.callbackUrl(),
                                                                    e);
                                                            permanentFailedIds.add(
                                                                    outbox.idValue());
                                                        } catch (Exception e) {
                                                            log.error(
                                                                    "변환 콜백 전송 실패: outboxId={},"
                                                                            + " url={}",
                                                                    outbox.idValue(),
                                                                    outbox.callbackUrl(),
                                                                    e);
                                                            failedIds.add(outbox.idValue());
                                                        }
                                                    }))
                            .toList();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

            Instant now = Instant.now();

            transformCallbackOutboxCommandManager.bulkMarkSent(new ArrayList<>(successIds), now);
            transformCallbackOutboxCommandManager.bulkMarkFailed(
                    new ArrayList<>(failedIds), now, "Callback notification failed");

            for (String permFailedId : permanentFailedIds) {
                TransformCallbackOutbox outbox =
                        claimed.stream()
                                .filter(o -> o.idValue().equals(permFailedId))
                                .findFirst()
                                .orElse(null);
                if (outbox != null) {
                    outbox.markFailedPermanently("Permanent callback failure (4xx)", now);
                    transformCallbackOutboxCommandManager.persist(outbox);
                }
            }

            int totalFailed = failedIds.size() + permanentFailedIds.size();
            return SchedulerBatchProcessingResult.of(
                    claimed.size(), successIds.size(), totalFailed);
        } catch (Exception e) {
            log.error(
                    "변환 콜백 배치 처리 중 예외 발생, PROCESSING → FAILED 복귀: count={}",
                    claimedOutboxIds.size(),
                    e);
            transformCallbackOutboxCommandManager.bulkMarkFailed(
                    claimedOutboxIds, Instant.now(), e.getMessage());
            return SchedulerBatchProcessingResult.of(claimed.size(), 0, claimed.size());
        }
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
