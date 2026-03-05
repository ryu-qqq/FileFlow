package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.dto.response.CallbackPayload;
import com.ryuqq.fileflow.application.download.exception.PermanentCallbackFailureException;
import com.ryuqq.fileflow.application.download.manager.client.CallbackNotificationManager;
import com.ryuqq.fileflow.application.download.manager.command.CallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.download.manager.query.CallbackOutboxReadManager;
import com.ryuqq.fileflow.application.download.manager.query.DownloadReadManager;
import com.ryuqq.fileflow.application.download.port.in.command.ProcessCallbackOutboxUseCase;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessCallbackOutboxService implements ProcessCallbackOutboxUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessCallbackOutboxService.class);

    private final CallbackOutboxReadManager callbackOutboxReadManager;
    private final CallbackOutboxCommandManager callbackOutboxCommandManager;
    private final CallbackNotificationManager callbackNotificationManager;
    private final DownloadReadManager downloadReadManager;

    public ProcessCallbackOutboxService(
            CallbackOutboxReadManager callbackOutboxReadManager,
            CallbackOutboxCommandManager callbackOutboxCommandManager,
            CallbackNotificationManager callbackNotificationManager,
            DownloadReadManager downloadReadManager) {
        this.callbackOutboxReadManager = callbackOutboxReadManager;
        this.callbackOutboxCommandManager = callbackOutboxCommandManager;
        this.callbackNotificationManager = callbackNotificationManager;
        this.downloadReadManager = downloadReadManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(int batchSize) {
        List<CallbackOutbox> pending = callbackOutboxReadManager.findPendingMessages(batchSize);
        if (pending.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        int success = 0;
        int failed = 0;

        Instant now = Instant.now();
        for (CallbackOutbox outbox : pending) {
            try {
                CallbackPayload payload = buildPayload(outbox);
                callbackNotificationManager.notify(outbox.callbackUrl(), payload);
                outbox.markSent(now);
                success++;
            } catch (PermanentCallbackFailureException e) {
                log.warn(
                        "콜백 영구 실패 (재시도 불가): outboxId={}, downloadTaskId={}, callbackUrl={}",
                        outbox.idValue(),
                        outbox.downloadTaskId(),
                        outbox.callbackUrl(),
                        e);
                outbox.markFailedPermanently(e.getMessage(), now);
                failed++;
            } catch (Exception e) {
                log.error(
                        "콜백 전송 실패 (재시도 예정): outboxId={}, downloadTaskId={}, callbackUrl={},"
                                + " retry={}/{}",
                        outbox.idValue(),
                        outbox.downloadTaskId(),
                        outbox.callbackUrl(),
                        outbox.retryCount(),
                        outbox.maxRetries(),
                        e);
                outbox.markFailed(e.getMessage(), now);
                failed++;
            }
            callbackOutboxCommandManager.persist(outbox);
        }

        return SchedulerBatchProcessingResult.of(pending.size(), success, failed);
    }

    private CallbackPayload buildPayload(CallbackOutbox outbox) {
        DownloadTask task = downloadReadManager.getDownloadTask(outbox.downloadTaskId());

        if ("COMPLETED".equals(outbox.taskStatus())) {
            return CallbackPayload.ofCompleted(
                    task.idValue(),
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
