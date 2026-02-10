package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.component.TransactionEventRegistry;
import com.ryuqq.fileflow.application.common.dto.command.UpdateContext;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.SessionExpirationManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.MultipartUploadSessionUpdateData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteMultipartUploadSessionService
        implements CompleteMultipartUploadSessionUseCase {

    private final MultipartSessionCommandFactory multipartSessionCommandFactory;
    private final SessionReadManager sessionReadManager;
    private final SessionCommandManager sessionCommandManager;
    private final SessionExpirationManager sessionExpirationManager;
    private final TransactionEventRegistry transactionEventRegistry;

    public CompleteMultipartUploadSessionService(
            MultipartSessionCommandFactory multipartSessionCommandFactory,
            SessionReadManager sessionReadManager,
            SessionCommandManager sessionCommandManager,
            SessionExpirationManager sessionExpirationManager,
            TransactionEventRegistry transactionEventRegistry) {
        this.multipartSessionCommandFactory = multipartSessionCommandFactory;
        this.sessionReadManager = sessionReadManager;
        this.sessionCommandManager = sessionCommandManager;
        this.sessionExpirationManager = sessionExpirationManager;
        this.transactionEventRegistry = transactionEventRegistry;
    }

    @Transactional
    @Override
    public void execute(CompleteMultipartUploadSessionCommand command) {
        UpdateContext<String, MultipartUploadSessionUpdateData> context =
                multipartSessionCommandFactory.createCompleteContext(command);

        MultipartUploadSession session = sessionReadManager.getMultipart(context.id());
        session.complete(context.updateData(), context.changedAt());

        sessionCommandManager.persist(session);
        sessionExpirationManager.removeExpiration("MULTIPART", context.id());
        transactionEventRegistry.registerAllForPublish(session.pollEvents());
    }
}
