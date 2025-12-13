package com.ryuqq.fileflow.application.asset.service.command;

import com.ryuqq.fileflow.application.asset.dto.command.UpdateFileAssetStatusCommand;
import com.ryuqq.fileflow.application.asset.dto.response.UpdateFileAssetStatusResponse;
import com.ryuqq.fileflow.application.asset.facade.FileAssetProcessingFacade;
import com.ryuqq.fileflow.application.asset.factory.command.FileAssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.command.UpdateFileAssetStatusUseCase;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * FileAsset 상태 변경 Service.
 *
 * <p>UpdateFileAssetStatusUseCase 구현체입니다.
 *
 * <p><strong>상태 전이 규칙</strong>:
 *
 * <ul>
 *   <li>RESIZED → N8N_PROCESSING
 *   <li>N8N_PROCESSING → N8N_COMPLETED
 *   <li>N8N_COMPLETED → COMPLETED
 *   <li>* → FAILED
 * </ul>
 */
@Service
public class UpdateFileAssetStatusService implements UpdateFileAssetStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateFileAssetStatusService.class);

    private final FileAssetReadManager fileAssetReadManager;
    private final FileAssetProcessingFacade processingFacade;
    private final FileAssetCommandFactory commandFactory;

    public UpdateFileAssetStatusService(
            FileAssetReadManager fileAssetReadManager,
            FileAssetProcessingFacade processingFacade,
            FileAssetCommandFactory commandFactory) {
        this.fileAssetReadManager = fileAssetReadManager;
        this.processingFacade = processingFacade;
        this.commandFactory = commandFactory;
    }

    @Override
    public UpdateFileAssetStatusResponse execute(UpdateFileAssetStatusCommand command) {
        log.info(
                "상태 변경 요청: fileAssetId={}, toStatus={}", command.fileAssetId(), command.toStatus());

        FileAssetId fileAssetId = FileAssetId.of(command.fileAssetId());

        // 1. FileAsset 조회 (조직/테넌트 검증 없이 - N8N 워크플로우용)
        FileAsset fileAsset =
                fileAssetReadManager
                        .findById(fileAssetId)
                        .orElseThrow(() -> new FileAssetNotFoundException(command.fileAssetId()));

        // 2. 이전 상태 저장
        FileAssetStatus fromStatus = fileAsset.getStatus();
        FileAssetStatus toStatus = FileAssetStatus.valueOf(command.toStatus());

        // 3. 상태 변경
        fileAsset.changeStatus(toStatus, commandFactory.getClock());

        // 4. 상태 이력 생성
        FileAssetStatusHistory history =
                commandFactory.createStatusHistory(
                        fileAsset.getId(), fromStatus, toStatus, command.reason());

        // 5. 저장 (트랜잭션 내에서)
        processingFacade.updateStatusWithHistory(fileAsset, history);

        log.info(
                "상태 변경 완료: fileAssetId={}, fromStatus={}, toStatus={}",
                command.fileAssetId(),
                fromStatus,
                toStatus);

        return new UpdateFileAssetStatusResponse(
                command.fileAssetId(), fromStatus.name(), toStatus.name(), Instant.now());
    }
}
