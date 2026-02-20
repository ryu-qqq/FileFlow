package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.MultipartUploadManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.RecoverExpiredMultipartSessionUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RecoverExpiredMultipartSessionService
        implements RecoverExpiredMultipartSessionUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverExpiredMultipartSessionService.class);

    private final SessionReadManager sessionReadManager;
    private final MultipartSessionCommandFactory multipartSessionCommandFactory;
    private final SessionCommandManager sessionCommandManager;
    private final MultipartUploadManager multipartUploadManager;

    public RecoverExpiredMultipartSessionService(
            SessionReadManager sessionReadManager,
            MultipartSessionCommandFactory multipartSessionCommandFactory,
            SessionCommandManager sessionCommandManager,
            MultipartUploadManager multipartUploadManager) {
        this.sessionReadManager = sessionReadManager;
        this.multipartSessionCommandFactory = multipartSessionCommandFactory;
        this.sessionCommandManager = sessionCommandManager;
        this.multipartUploadManager = multipartUploadManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(int batchSize) {
        Instant now = Instant.now();
        int successCount = 0;
        int failedCount = 0;

        List<MultipartUploadSession> expired =
                sessionReadManager.findExpiredMultipartSessions(now, batchSize);

        for (MultipartUploadSession session : expired) {
            try {
                multipartUploadManager.abortMultipartUpload(session.s3Key(), session.uploadId());

                StatusChangeContext<String> context =
                        multipartSessionCommandFactory.createExpireContext(session.idValue());
                session.expire(context.changedAt());
                sessionCommandManager.persist(session);
                successCount++;
            } catch (Exception e) {
                log.error(
                        "고아 멀티파트 세션 만료 처리 실패: sessionId={}, error={}",
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
