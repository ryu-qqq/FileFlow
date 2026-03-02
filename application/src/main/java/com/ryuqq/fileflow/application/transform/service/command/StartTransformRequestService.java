package com.ryuqq.fileflow.application.transform.service.command;

import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.transform.internal.TransformExecutionCoordinator;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCommandManager;
import com.ryuqq.fileflow.application.transform.port.in.command.StartTransformRequestUseCase;
import com.ryuqq.fileflow.application.transform.validator.TransformExecutionValidator;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StartTransformRequestService implements StartTransformRequestUseCase {

    private static final Logger log = LoggerFactory.getLogger(StartTransformRequestService.class);

    private final TransformExecutionValidator transformExecutionValidator;
    private final TransformExecutionCoordinator transformExecutionCoordinator;
    private final TransformCommandManager transformCommandManager;
    private final TimeProvider timeProvider;

    public StartTransformRequestService(
            TransformExecutionValidator transformExecutionValidator,
            TransformExecutionCoordinator transformExecutionCoordinator,
            TransformCommandManager transformCommandManager,
            TimeProvider timeProvider) {
        this.transformExecutionValidator = transformExecutionValidator;
        this.transformExecutionCoordinator = transformExecutionCoordinator;
        this.transformCommandManager = transformCommandManager;
        this.timeProvider = timeProvider;
    }

    @Override
    public void execute(String transformRequestId) {
        TransformRequest request =
                transformExecutionValidator.getTransformRequest(transformRequestId);
        TransformStatus currentStatus = request.status();

        if (currentStatus == TransformStatus.COMPLETED || currentStatus == TransformStatus.FAILED) {
            log.warn(
                    "이미 완료/실패 상태의 변환 요청, 처리 건너뜀: requestId={}, status={}",
                    transformRequestId,
                    currentStatus);
            return;
        }

        try {
            Asset sourceAsset =
                    transformExecutionValidator.getSourceAsset(request.sourceAssetIdValue());
            transformExecutionCoordinator.execute(request, sourceAsset);
        } catch (Exception e) {
            safeFailRequest(request, e);
            throw e;
        }
    }

    private void safeFailRequest(TransformRequest request, Exception e) {
        try {
            if (request.status() == TransformStatus.QUEUED) {
                request.fail(e.getMessage(), timeProvider.now());
                transformCommandManager.persist(request);
                log.warn(
                        "변환 시작 전 실패 처리: requestId={}, error={}",
                        request.idValue(),
                        e.getMessage());
            }
        } catch (Exception failEx) {
            log.error(
                    "변환 실패 처리 자체도 실패: requestId={}",
                    request.idValue(),
                    failEx);
        }
    }
}
