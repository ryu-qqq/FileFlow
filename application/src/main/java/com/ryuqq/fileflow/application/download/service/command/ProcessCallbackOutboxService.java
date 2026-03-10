package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.dto.response.CallbackPayload;
import com.ryuqq.fileflow.application.download.exception.PermanentCallbackFailureException;
import com.ryuqq.fileflow.application.download.manager.client.CallbackNotificationManager;
import com.ryuqq.fileflow.application.download.manager.command.CallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.download.manager.query.DownloadReadManager;
import com.ryuqq.fileflow.application.download.port.in.command.ProcessCallbackOutboxUseCase;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessCallbackOutboxService implements ProcessCallbackOutboxUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessCallbackOutboxService.class);

    private final CallbackOutboxCommandManager callbackOutboxCommandManager;
    private final CallbackNotificationManager callbackNotificationManager;
    private final DownloadReadManager downloadReadManager;

    public ProcessCallbackOutboxService(
            CallbackOutboxCommandManager callbackOutboxCommandManager,
            CallbackNotificationManager callbackNotificationManager,
            DownloadReadManager downloadReadManager) {
        this.callbackOutboxCommandManager = callbackOutboxCommandManager;
        this.callbackNotificationManager = callbackNotificationManager;
        this.downloadReadManager = downloadReadManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(int batchSize) {
        List<CallbackOutbox> claimed = callbackOutboxCommandManager.claimPendingMessages(batchSize);
        if (claimed.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        List<String> claimedOutboxIds = claimed.stream().map(CallbackOutbox::idValue).toList();

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
                                                            CallbackPayload payload =
                                                                    buildPayload(outbox);
                                                            callbackNotificationManager.notify(
                                                                    outbox.callbackUrl(), payload);
                                                            successIds.add(outbox.idValue());
                                                        } catch (
                                                                PermanentCallbackFailureException
                                                                        e) {
                                                            log.warn(
                                                                    "콜백 영구 실패: outboxId={}, url={}",
                                                                    outbox.idValue(),
                                                                    outbox.callbackUrl(),
                                                                    e);
                                                            permanentFailedIds.add(
                                                                    outbox.idValue());
                                                        } catch (Exception e) {
                                                            log.error(
                                                                    "콜백 전송 실패: outboxId={}, url={}",
                                                                    outbox.idValue(),
                                                                    outbox.callbackUrl(),
                                                                    e);
                                                            failedIds.add(outbox.idValue());
                                                        }
                                                    }))
                            .toList();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

            Instant now = Instant.now();

            callbackOutboxCommandManager.bulkMarkSent(new ArrayList<>(successIds), now);
            callbackOutboxCommandManager.bulkMarkFailed(new ArrayList<>(failedIds), now);

            for (String permFailedId : permanentFailedIds) {
                CallbackOutbox outbox =
                        claimed.stream()
                                .filter(o -> o.idValue().equals(permFailedId))
                                .findFirst()
                                .orElse(null);
                if (outbox != null) {
                    outbox.markFailedPermanently("Permanent callback failure (4xx)", now);
                    callbackOutboxCommandManager.persist(outbox);
                }
            }

            int totalFailed = failedIds.size() + permanentFailedIds.size();
            return SchedulerBatchProcessingResult.of(
                    claimed.size(), successIds.size(), totalFailed);
        } catch (Exception e) {
            log.error(
                    "콜백 배치 처리 중 예외 발생, PROCESSING → FAILED 복귀: count={}",
                    claimedOutboxIds.size(),
                    e);
            callbackOutboxCommandManager.bulkMarkFailed(claimedOutboxIds, Instant.now());
            return SchedulerBatchProcessingResult.of(claimed.size(), 0, claimed.size());
        }
    }

    private CallbackPayload buildPayload(CallbackOutbox outbox) {
        DownloadTask task = downloadReadManager.getDownloadTask(outbox.downloadTaskId());

        if ("COMPLETED".equals(outbox.taskStatus())) {
            return CallbackPayload.ofCompleted(
                    task.idValue(),
                    task.assetId(),
                    task.sourceUrlValue(),
                    task.s3Key(),
                    task.bucket(),
                    extractFileName(task.s3Key()),
                    null,
                    0);
        }

        return CallbackPayload.ofFailed(task.idValue(), task.sourceUrlValue(), task.lastError());
    }

    private String extractFileName(String s3Key) {
        int lastSlash = s3Key.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < s3Key.length() - 1) {
            return s3Key.substring(lastSlash + 1);
        }
        return s3Key;
    }
}
