package com.ryuqq.fileflow.application.transform.service.command;

import com.ryuqq.fileflow.application.transform.internal.TransformExecutionCoordinator;
import com.ryuqq.fileflow.application.transform.port.in.command.StartTransformRequestUseCase;
import com.ryuqq.fileflow.application.transform.validator.TransformExecutionValidator;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import org.springframework.stereotype.Service;

@Service
public class StartTransformRequestService implements StartTransformRequestUseCase {

    private final TransformExecutionValidator transformExecutionValidator;
    private final TransformExecutionCoordinator transformExecutionCoordinator;

    public StartTransformRequestService(
            TransformExecutionValidator transformExecutionValidator,
            TransformExecutionCoordinator transformExecutionCoordinator) {
        this.transformExecutionValidator = transformExecutionValidator;
        this.transformExecutionCoordinator = transformExecutionCoordinator;
    }

    @Override
    public void execute(String transformRequestId) {
        TransformRequest request =
                transformExecutionValidator.getTransformRequest(transformRequestId);
        Asset sourceAsset =
                transformExecutionValidator.getSourceAsset(request.sourceAssetIdValue());
        transformExecutionCoordinator.execute(request, sourceAsset);
    }
}
