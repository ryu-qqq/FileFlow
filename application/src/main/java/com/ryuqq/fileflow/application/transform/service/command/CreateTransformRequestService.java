package com.ryuqq.fileflow.application.transform.service.command;

import com.ryuqq.fileflow.application.transform.assembler.TransformAssembler;
import com.ryuqq.fileflow.application.transform.dto.command.CreateTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import com.ryuqq.fileflow.application.transform.factory.command.TransformCommandFactory;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCommandManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformQueueOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.port.in.command.CreateTransformRequestUseCase;
import com.ryuqq.fileflow.application.transform.validator.SourceAssetValidator;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTransformRequestService implements CreateTransformRequestUseCase {

    private final SourceAssetValidator sourceAssetValidator;
    private final TransformCommandFactory transformCommandFactory;
    private final TransformCommandManager transformCommandManager;
    private final TransformQueueOutboxCommandManager transformQueueOutboxCommandManager;
    private final TransformAssembler transformAssembler;

    public CreateTransformRequestService(
            SourceAssetValidator sourceAssetValidator,
            TransformCommandFactory transformCommandFactory,
            TransformCommandManager transformCommandManager,
            TransformQueueOutboxCommandManager transformQueueOutboxCommandManager,
            TransformAssembler transformAssembler) {
        this.sourceAssetValidator = sourceAssetValidator;
        this.transformCommandFactory = transformCommandFactory;
        this.transformCommandManager = transformCommandManager;
        this.transformQueueOutboxCommandManager = transformQueueOutboxCommandManager;
        this.transformAssembler = transformAssembler;
    }

    @Transactional
    @Override
    public TransformRequestResponse execute(CreateTransformRequestCommand command) {
        String sourceContentType =
                sourceAssetValidator.validateAndGetContentType(command.sourceAssetId());

        TransformRequest transformRequest =
                transformCommandFactory.createTransformRequest(command, sourceContentType);
        transformCommandManager.persist(transformRequest);

        TransformQueueOutbox outbox =
                transformCommandFactory.createQueueOutbox(transformRequest.idValue());
        transformQueueOutboxCommandManager.persist(outbox);

        return transformAssembler.toResponse(transformRequest);
    }
}
