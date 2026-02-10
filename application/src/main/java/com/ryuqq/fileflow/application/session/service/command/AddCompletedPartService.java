package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.session.dto.command.AddCompletedPartCommand;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.AddCompletedPartUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddCompletedPartService implements AddCompletedPartUseCase {

    private final MultipartSessionCommandFactory multipartSessionCommandFactory;
    private final SessionReadManager sessionReadManager;
    private final SessionCommandManager sessionCommandManager;

    public AddCompletedPartService(
            MultipartSessionCommandFactory multipartSessionCommandFactory,
            SessionReadManager sessionReadManager,
            SessionCommandManager sessionCommandManager) {
        this.multipartSessionCommandFactory = multipartSessionCommandFactory;
        this.sessionReadManager = sessionReadManager;
        this.sessionCommandManager = sessionCommandManager;
    }

    @Transactional
    @Override
    public void execute(AddCompletedPartCommand command) {
        CompletedPart completedPart = multipartSessionCommandFactory.createCompletedPart(command);

        MultipartUploadSession session = sessionReadManager.getMultipart(command.sessionId());
        session.addCompletedPart(completedPart);

        sessionCommandManager.persist(session);
    }
}
