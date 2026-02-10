package com.ryuqq.fileflow.application.transform.service.query;

import com.ryuqq.fileflow.application.transform.assembler.TransformAssembler;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import com.ryuqq.fileflow.application.transform.manager.query.TransformReadManager;
import com.ryuqq.fileflow.application.transform.port.in.query.GetTransformRequestUseCase;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import org.springframework.stereotype.Service;

@Service
public class GetTransformRequestService implements GetTransformRequestUseCase {

    private final TransformReadManager transformReadManager;
    private final TransformAssembler transformAssembler;

    public GetTransformRequestService(
            TransformReadManager transformReadManager, TransformAssembler transformAssembler) {
        this.transformReadManager = transformReadManager;
        this.transformAssembler = transformAssembler;
    }

    @Override
    public TransformRequestResponse execute(String transformRequestId) {
        TransformRequest transformRequest =
                transformReadManager.getTransformRequest(transformRequestId);
        return transformAssembler.toResponse(transformRequest);
    }
}
