package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.component.TransactionEventRegistry;
import com.ryuqq.fileflow.application.common.dto.command.UpdateContext;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.factory.command.SingleSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.SessionExpirationManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteSingleUploadSessionUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.SingleUploadSessionUpdateData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteSingleUploadSessionService implements CompleteSingleUploadSessionUseCase {

    private final SingleSessionCommandFactory singleSessionCommandFactory;
    private final SessionReadManager sessionReadManager;
    private final SessionCommandManager sessionCommandManager;
    private final SessionExpirationManager sessionExpirationManager;
    private final TransactionEventRegistry transactionEventRegistry;

    public CompleteSingleUploadSessionService(
            SingleSessionCommandFactory singleSessionCommandFactory,
            SessionReadManager sessionReadManager,
            SessionCommandManager sessionCommandManager,
            SessionExpirationManager sessionExpirationManager,
            TransactionEventRegistry transactionEventRegistry) {
        this.singleSessionCommandFactory = singleSessionCommandFactory;
        this.sessionReadManager = sessionReadManager;
        this.sessionCommandManager = sessionCommandManager;
        this.sessionExpirationManager = sessionExpirationManager;
        this.transactionEventRegistry = transactionEventRegistry;
    }

    @Transactional
    @Override
    public void execute(CompleteSingleUploadSessionCommand command) {
        UpdateContext<String, SingleUploadSessionUpdateData> context =
                singleSessionCommandFactory.createCompleteContext(command);

        SingleUploadSession session = sessionReadManager.getSingle(context.id());
        session.complete(context.updateData(), context.changedAt());

        sessionCommandManager.persist(session);
        sessionExpirationManager.removeExpiration("SINGLE", context.id());
        transactionEventRegistry.registerAllForPublish(session.pollEvents());
    }
}
