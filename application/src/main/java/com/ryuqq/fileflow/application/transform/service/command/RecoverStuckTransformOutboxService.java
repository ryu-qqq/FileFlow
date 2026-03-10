package com.ryuqq.fileflow.application.transform.service.command;

import com.ryuqq.fileflow.application.transform.manager.command.TransformCallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformQueueOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.port.in.command.RecoverStuckTransformOutboxUseCase;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RecoverStuckTransformOutboxService implements RecoverStuckTransformOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverStuckTransformOutboxService.class);

    private final TransformQueueOutboxCommandManager transformQueueOutboxCommandManager;
    private final TransformCallbackOutboxCommandManager transformCallbackOutboxCommandManager;

    public RecoverStuckTransformOutboxService(
            TransformQueueOutboxCommandManager transformQueueOutboxCommandManager,
            TransformCallbackOutboxCommandManager transformCallbackOutboxCommandManager) {
        this.transformQueueOutboxCommandManager = transformQueueOutboxCommandManager;
        this.transformCallbackOutboxCommandManager = transformCallbackOutboxCommandManager;
    }

    @Override
    public int execute(int stuckMinutes) {
        Instant cutoff = Instant.now().minus(stuckMinutes, ChronoUnit.MINUTES);

        int transformQueueRecovered =
                transformQueueOutboxCommandManager.recoverStuckProcessing(cutoff);
        int transformCallbackRecovered =
                transformCallbackOutboxCommandManager.recoverStuckProcessing(cutoff);

        int totalRecovered = transformQueueRecovered + transformCallbackRecovered;

        if (totalRecovered > 0) {
            log.info(
                    "변환 Outbox PROCESSING 복구 완료: transformQueue={}, transformCallback={}, total={}",
                    transformQueueRecovered,
                    transformCallbackRecovered,
                    totalRecovered);
        }

        return totalRecovered;
    }
}
