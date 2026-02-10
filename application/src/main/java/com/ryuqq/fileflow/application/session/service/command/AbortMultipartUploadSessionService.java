package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.session.dto.command.AbortMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.MultipartUploadManager;
import com.ryuqq.fileflow.application.session.manager.client.SessionExpirationManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.AbortMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AbortMultipartUploadSessionService implements AbortMultipartUploadSessionUseCase {

    private final MultipartSessionCommandFactory multipartSessionCommandFactory;
    private final SessionReadManager sessionReadManager;
    private final SessionCommandManager sessionCommandManager;
    private final SessionExpirationManager sessionExpirationManager;
    private final MultipartUploadManager multipartUploadManager;

    public AbortMultipartUploadSessionService(
            MultipartSessionCommandFactory multipartSessionCommandFactory,
            SessionReadManager sessionReadManager,
            SessionCommandManager sessionCommandManager,
            SessionExpirationManager sessionExpirationManager,
            MultipartUploadManager multipartUploadManager) {
        this.multipartSessionCommandFactory = multipartSessionCommandFactory;
        this.sessionReadManager = sessionReadManager;
        this.sessionCommandManager = sessionCommandManager;
        this.sessionExpirationManager = sessionExpirationManager;
        this.multipartUploadManager = multipartUploadManager;
    }

    @Transactional
    @Override
    public void execute(AbortMultipartUploadSessionCommand command) {
        StatusChangeContext<String> context =
                multipartSessionCommandFactory.createAbortContext(command.sessionId());

        MultipartUploadSession session = sessionReadManager.getMultipart(context.id());

        multipartUploadManager.abortMultipartUpload(session.s3Key(), session.uploadId());

        session.abort(context.changedAt());

        sessionCommandManager.persist(session);
        sessionExpirationManager.removeExpiration("MULTIPART", context.id());
    }
}
