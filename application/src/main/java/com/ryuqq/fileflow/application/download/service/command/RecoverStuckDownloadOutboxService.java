package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.download.manager.command.CallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadQueueOutboxCommandManager;
import com.ryuqq.fileflow.application.download.port.in.command.RecoverStuckOutboxUseCase;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RecoverStuckDownloadOutboxService implements RecoverStuckOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverStuckDownloadOutboxService.class);

    private final DownloadQueueOutboxCommandManager downloadQueueOutboxCommandManager;
    private final CallbackOutboxCommandManager callbackOutboxCommandManager;

    public RecoverStuckDownloadOutboxService(
            DownloadQueueOutboxCommandManager downloadQueueOutboxCommandManager,
            CallbackOutboxCommandManager callbackOutboxCommandManager) {
        this.downloadQueueOutboxCommandManager = downloadQueueOutboxCommandManager;
        this.callbackOutboxCommandManager = callbackOutboxCommandManager;
    }

    @Override
    public int execute(int stuckMinutes) {
        Instant cutoff = Instant.now().minus(stuckMinutes, ChronoUnit.MINUTES);

        int downloadQueueRecovered =
                downloadQueueOutboxCommandManager.recoverStuckProcessing(cutoff);
        int callbackRecovered = callbackOutboxCommandManager.recoverStuckProcessing(cutoff);

        int totalRecovered = downloadQueueRecovered + callbackRecovered;

        if (totalRecovered > 0) {
            log.info(
                    "다운로드 Outbox PROCESSING 복구 완료: downloadQueue={}, callback={}, total={}",
                    downloadQueueRecovered,
                    callbackRecovered,
                    totalRecovered);
        }

        return totalRecovered;
    }
}
