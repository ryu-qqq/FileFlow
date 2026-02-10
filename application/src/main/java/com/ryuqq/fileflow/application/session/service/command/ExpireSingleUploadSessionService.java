package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.session.factory.command.SingleSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.ExpireSingleUploadSessionUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpireSingleUploadSessionService implements ExpireSingleUploadSessionUseCase {

    private final SingleSessionCommandFactory singleSessionCommandFactory;
    private final SessionReadManager sessionReadManager;
    private final SessionCommandManager sessionCommandManager;

    public ExpireSingleUploadSessionService(
            SingleSessionCommandFactory singleSessionCommandFactory,
            SessionReadManager sessionReadManager,
            SessionCommandManager sessionCommandManager) {
        this.singleSessionCommandFactory = singleSessionCommandFactory;
        this.sessionReadManager = sessionReadManager;
        this.sessionCommandManager = sessionCommandManager;
    }

    @Transactional
    @Override
    public void execute(String sessionId) {
        StatusChangeContext<String> context =
                singleSessionCommandFactory.createExpireContext(sessionId);

        SingleUploadSession session = sessionReadManager.getSingle(context.id());
        session.expire(context.changedAt());

        sessionCommandManager.persist(session);
    }
}
