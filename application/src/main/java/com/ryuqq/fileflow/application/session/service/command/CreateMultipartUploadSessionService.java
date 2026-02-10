package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.session.assembler.SessionAssembler;
import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.internal.MultipartSessionCreationCoordinator;
import com.ryuqq.fileflow.application.session.port.in.command.CreateMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import org.springframework.stereotype.Service;

@Service
public class CreateMultipartUploadSessionService implements CreateMultipartUploadSessionUseCase {

    private final MultipartSessionCreationCoordinator multipartSessionCreationCoordinator;
    private final SessionAssembler sessionAssembler;

    public CreateMultipartUploadSessionService(
            MultipartSessionCreationCoordinator multipartSessionCreationCoordinator,
            SessionAssembler sessionAssembler) {
        this.multipartSessionCreationCoordinator = multipartSessionCreationCoordinator;
        this.sessionAssembler = sessionAssembler;
    }

    @Override
    public MultipartUploadSessionResponse execute(CreateMultipartUploadSessionCommand command) {
        MultipartUploadSession session = multipartSessionCreationCoordinator.create(command);
        return sessionAssembler.toResponse(session);
    }
}
