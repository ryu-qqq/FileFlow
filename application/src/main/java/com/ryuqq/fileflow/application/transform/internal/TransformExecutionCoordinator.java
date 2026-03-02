package com.ryuqq.fileflow.application.transform.internal;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformCompletionBundle;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformFailureBundle;
import com.ryuqq.fileflow.application.transform.dto.result.ImageTransformResult;
import com.ryuqq.fileflow.application.transform.factory.command.TransformCommandFactory;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCommandManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformReadManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransformExecutionCoordinator {

    private static final Logger log = LoggerFactory.getLogger(TransformExecutionCoordinator.class);

    private final TransformCommandFactory transformCommandFactory;
    private final ImageTransformFacade imageTransformFacade;
    private final TransformCommandManager transformCommandManager;
    private final TransformReadManager transformReadManager;
    private final TransformCompletionFacade transformCompletionFacade;

    public TransformExecutionCoordinator(
            TransformCommandFactory transformCommandFactory,
            ImageTransformFacade imageTransformFacade,
            TransformCommandManager transformCommandManager,
            TransformReadManager transformReadManager,
            TransformCompletionFacade transformCompletionFacade) {
        this.transformCommandFactory = transformCommandFactory;
        this.imageTransformFacade = imageTransformFacade;
        this.transformCommandManager = transformCommandManager;
        this.transformReadManager = transformReadManager;
        this.transformCompletionFacade = transformCompletionFacade;
    }

    public void execute(TransformRequest request, Asset sourceAsset) {
        StatusChangeContext<String> context =
                transformCommandFactory.createStartContext(request.idValue());
        request.start(context.changedAt());
        transformCommandManager.persist(request);

        log.info(
                "변환 시작 persist 완료: requestId={}, version={}", request.idValue(), request.version());

        try {
            ImageTransformResult result = imageTransformFacade.transform(sourceAsset, request);

            if (result.success()) {
                TransformCompletionBundle bundle =
                        transformCommandFactory.createCompletionBundle(
                                result, request, sourceAsset);
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
                log.error(
                        "변환 실패 처리: requestId={}, error={}",
                        request.idValue(),
                        result.errorMessage());
            }
        } catch (Exception e) {
            log.error("변환 중 예외 발생: requestId={}", request.idValue(), e);
            safeFailTransform(request, e.getMessage());
        }
    }

    private void safeFailTransform(TransformRequest request, String errorMessage) {
        try {
            TransformFailureBundle bundle =
                    transformCommandFactory.createFailureBundle(
                            request, ImageTransformResult.failure(errorMessage));
            transformCompletionFacade.fail(bundle);
        } catch (Exception failEx) {
            log.error("변환 실패 처리 자체도 실패, 직접 persist 시도: requestId={}", request.idValue(), failEx);
            try {
                TransformRequest freshRequest =
                        transformReadManager.getTransformRequest(request.idValue());
                if (freshRequest.status() != TransformStatus.FAILED
                        && freshRequest.status() != TransformStatus.COMPLETED) {
                    freshRequest.fail(errorMessage, Instant.now());
                    transformCommandManager.persist(freshRequest);
                    log.info(
                            "직접 persist 성공: requestId={}, version={}",
                            freshRequest.idValue(),
                            freshRequest.version());
                }
            } catch (Exception lastResort) {
                log.error(
                        "최종 persist도 실패, PROCESSING 상태로 stuck 예상: requestId={}",
                        request.idValue(),
                        lastResort);
            }
        }
    }
}
