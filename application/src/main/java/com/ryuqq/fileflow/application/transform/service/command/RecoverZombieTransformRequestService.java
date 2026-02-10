package com.ryuqq.fileflow.application.transform.service.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.transform.dto.command.RecoverZombieTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.manager.client.TransformQueueManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformReadManager;
import com.ryuqq.fileflow.application.transform.port.in.command.RecoverZombieTransformRequestUseCase;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RecoverZombieTransformRequestService implements RecoverZombieTransformRequestUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverZombieTransformRequestService.class);

    private final TransformReadManager transformReadManager;
    private final TransformQueueManager transformQueueManager;

    public RecoverZombieTransformRequestService(
            TransformReadManager transformReadManager,
            TransformQueueManager transformQueueManager) {
        this.transformReadManager = transformReadManager;
        this.transformQueueManager = transformQueueManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(RecoverZombieTransformRequestCommand command) {
        List<TransformRequest> staleRequests =
                transformReadManager.getStaleQueuedRequests(
                        command.timeoutThreshold(), command.batchSize());

        int total = staleRequests.size();
        int successCount = 0;
        int failedCount = 0;

        for (TransformRequest request : staleRequests) {
            try {
                transformQueueManager.enqueue(request.idValue());
                successCount++;
            } catch (Exception e) {
                log.error(
                        "좀비 변환 요청 재큐잉 실패: requestId={}, error={}",
                        request.idValue(),
                        e.getMessage(),
                        e);
                failedCount++;
            }
        }

        return SchedulerBatchProcessingResult.of(total, successCount, failedCount);
    }
}
