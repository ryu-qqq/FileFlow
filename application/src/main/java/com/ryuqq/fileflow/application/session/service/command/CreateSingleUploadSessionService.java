package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.session.assembler.SessionAssembler;
import com.ryuqq.fileflow.application.session.dto.command.CreateSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;
import com.ryuqq.fileflow.application.session.internal.SingleSessionCreationCoordinator;
import com.ryuqq.fileflow.application.session.port.in.command.CreateSingleUploadSessionUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import org.springframework.stereotype.Service;

@Service
public class CreateSingleUploadSessionService implements CreateSingleUploadSessionUseCase {

    private final SingleSessionCreationCoordinator singleSessionCreationCoordinator;
    private final SessionAssembler sessionAssembler;

    public CreateSingleUploadSessionService(
            SingleSessionCreationCoordinator singleSessionCreationCoordinator,
            SessionAssembler sessionAssembler) {
        this.singleSessionCreationCoordinator = singleSessionCreationCoordinator;
        this.sessionAssembler = sessionAssembler;
    }

    @Override
    public SingleUploadSessionResponse execute(CreateSingleUploadSessionCommand command) {
        SingleUploadSession session = singleSessionCreationCoordinator.create(command);
        return sessionAssembler.toResponse(session);
    }
}
