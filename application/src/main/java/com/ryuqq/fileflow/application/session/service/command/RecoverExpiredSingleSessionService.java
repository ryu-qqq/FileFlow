package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.session.factory.command.SingleSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.RecoverExpiredSingleSessionUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RecoverExpiredSingleSessionService implements RecoverExpiredSingleSessionUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverExpiredSingleSessionService.class);

    private final SessionReadManager sessionReadManager;
    private final SingleSessionCommandFactory singleSessionCommandFactory;
    private final SessionCommandManager sessionCommandManager;

    public RecoverExpiredSingleSessionService(
            SessionReadManager sessionReadManager,
            SingleSessionCommandFactory singleSessionCommandFactory,
            SessionCommandManager sessionCommandManager) {
        this.sessionReadManager = sessionReadManager;
        this.singleSessionCommandFactory = singleSessionCommandFactory;
        this.sessionCommandManager = sessionCommandManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(int batchSize) {
        Instant now = Instant.now();
        int successCount = 0;
        int failedCount = 0;

        List<SingleUploadSession> expired =
                sessionReadManager.findExpiredSingleSessions(now, batchSize);

        for (SingleUploadSession session : expired) {
            try {
                StatusChangeContext<String> context =
                        singleSessionCommandFactory.createExpireContext(session.idValue());
                session.expire(context.changedAt());
                sessionCommandManager.persist(session);
                successCount++;
            } catch (Exception e) {
                log.error(
                        "고아 단건 세션 만료 처리 실패: sessionId={}, error={}",
                        session.idValue(),
                        e.getMessage(),
                        e);
                failedCount++;
            }
        }

        int total = successCount + failedCount;
        return SchedulerBatchProcessingResult.of(total, successCount, failedCount);
    }
}
