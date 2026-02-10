package com.ryuqq.fileflow.application.transform.internal;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformCompletionBundle;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformFailureBundle;
import com.ryuqq.fileflow.application.transform.dto.result.ImageTransformResult;
import com.ryuqq.fileflow.application.transform.factory.command.TransformCommandFactory;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCommandManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransformExecutionCoordinator {

    private static final Logger log = LoggerFactory.getLogger(TransformExecutionCoordinator.class);

    private final TransformCommandFactory transformCommandFactory;
    private final ImageTransformFacade imageTransformFacade;
    private final TransformCommandManager transformCommandManager;
    private final TransformCompletionFacade transformCompletionFacade;

    public TransformExecutionCoordinator(
            TransformCommandFactory transformCommandFactory,
            ImageTransformFacade imageTransformFacade,
            TransformCommandManager transformCommandManager,
            TransformCompletionFacade transformCompletionFacade) {
        this.transformCommandFactory = transformCommandFactory;
        this.imageTransformFacade = imageTransformFacade;
        this.transformCommandManager = transformCommandManager;
        this.transformCompletionFacade = transformCompletionFacade;
    }

    public void execute(TransformRequest request, Asset sourceAsset) {
        StatusChangeContext<String> context =
                transformCommandFactory.createStartContext(request.idValue());
        request.start(context.changedAt());
        transformCommandManager.persist(request);

        ImageTransformResult result = imageTransformFacade.transform(sourceAsset, request);

        if (result.success()) {
            TransformCompletionBundle bundle =
                    transformCommandFactory.createCompletionBundle(result, request, sourceAsset);
            transformCompletionFacade.complete(bundle);
            log.info(
                    "변환 완료: requestId={}, {}x{}",
                    request.idValue(),
                    result.dimension().width(),
                    result.dimension().height());
        } else {
            TransformFailureBundle bundle =
                    transformCommandFactory.createFailureBundle(request, result);
            transformCompletionFacade.fail(bundle);
            log.error("변환 실패 처리: requestId={}, error={}", request.idValue(), result.errorMessage());
        }
    }
}
