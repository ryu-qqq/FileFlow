package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.MultipartUploadManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.ExpireMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpireMultipartUploadSessionService implements ExpireMultipartUploadSessionUseCase {

    private final MultipartSessionCommandFactory multipartSessionCommandFactory;
    private final SessionReadManager sessionReadManager;
    private final SessionCommandManager sessionCommandManager;
    private final MultipartUploadManager multipartUploadManager;

    public ExpireMultipartUploadSessionService(
            MultipartSessionCommandFactory multipartSessionCommandFactory,
            SessionReadManager sessionReadManager,
            SessionCommandManager sessionCommandManager,
            MultipartUploadManager multipartUploadManager) {
        this.multipartSessionCommandFactory = multipartSessionCommandFactory;
        this.sessionReadManager = sessionReadManager;
        this.sessionCommandManager = sessionCommandManager;
        this.multipartUploadManager = multipartUploadManager;
    }

    @Transactional
    @Override
    public void execute(String sessionId) {
        StatusChangeContext<String> context =
                multipartSessionCommandFactory.createExpireContext(sessionId);

        MultipartUploadSession session = sessionReadManager.getMultipart(context.id());

        multipartUploadManager.abortMultipartUpload(session.s3Key(), session.uploadId());

        session.expire(context.changedAt());

        sessionCommandManager.persist(session);
    }
}
