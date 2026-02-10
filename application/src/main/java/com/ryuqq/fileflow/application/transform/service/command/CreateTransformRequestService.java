package com.ryuqq.fileflow.application.transform.service.command;

import com.ryuqq.fileflow.application.transform.assembler.TransformAssembler;
import com.ryuqq.fileflow.application.transform.dto.command.CreateTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import com.ryuqq.fileflow.application.transform.factory.command.TransformCommandFactory;
import com.ryuqq.fileflow.application.transform.manager.client.TransformQueueManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCommandManager;
import com.ryuqq.fileflow.application.transform.port.in.command.CreateTransformRequestUseCase;
import com.ryuqq.fileflow.application.transform.validator.SourceAssetValidator;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import org.springframework.stereotype.Service;

@Service
public class CreateTransformRequestService implements CreateTransformRequestUseCase {

    private final SourceAssetValidator sourceAssetValidator;
    private final TransformCommandFactory transformCommandFactory;
    private final TransformCommandManager transformCommandManager;
    private final TransformQueueManager transformQueueManager;
    private final TransformAssembler transformAssembler;

    public CreateTransformRequestService(
            SourceAssetValidator sourceAssetValidator,
            TransformCommandFactory transformCommandFactory,
            TransformCommandManager transformCommandManager,
            TransformQueueManager transformQueueManager,
            TransformAssembler transformAssembler) {
        this.sourceAssetValidator = sourceAssetValidator;
        this.transformCommandFactory = transformCommandFactory;
        this.transformCommandManager = transformCommandManager;
        this.transformQueueManager = transformQueueManager;
        this.transformAssembler = transformAssembler;
    }

    @Override
    public TransformRequestResponse execute(CreateTransformRequestCommand command) {
        String sourceContentType =
                sourceAssetValidator.validateAndGetContentType(command.sourceAssetId());

        TransformRequest transformRequest =
                transformCommandFactory.createTransformRequest(command, sourceContentType);
        transformCommandManager.persist(transformRequest);
        transformQueueManager.enqueue(transformRequest.idValue());

        return transformAssembler.toResponse(transformRequest);
    }
}
