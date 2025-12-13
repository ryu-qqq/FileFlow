package com.ryuqq.fileflow.application.asset.service.command;

import com.ryuqq.fileflow.application.asset.factory.command.FileAssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.FileAssetStatusHistoryTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.command.FileAssetTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.command.MarkFileAssetAsFailedUseCase;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.service.FileAssetUpdateResult;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * FileAsset 실패 처리 서비스.
 *
 * <p>DLQ에서 호출되어 최종 실패 처리를 수행합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>FileAsset 조회 (QueryPort)
 *   <li>이미 처리된 경우 skip (COMPLETED, FAILED)
 *   <li>도메인 비즈니스 로직 실행 (FileAssetUpdateService.markFailed)
 *   <li>영속화 (Manager)
 * </ul>
 *
 * <p><strong>도메인 로직</strong>은 {@link FileAssetUpdateService#markFailed}에 캡슐화되어 있습니다.
 */
@Service
public class MarkFileAssetAsFailedService implements MarkFileAssetAsFailedUseCase {

    private static final Logger log = LoggerFactory.getLogger(MarkFileAssetAsFailedService.class);

    private final FileAssetReadManager fileAssetReadManager;
    private final FileAssetTransactionManager fileAssetTransactionManager;
    private final FileAssetStatusHistoryTransactionManager statusHistoryTransactionManager;
    private final FileAssetCommandFactory commandFactory;

    public MarkFileAssetAsFailedService(
            FileAssetReadManager fileAssetReadManager,
            FileAssetTransactionManager fileAssetTransactionManager,
            FileAssetStatusHistoryTransactionManager statusHistoryTransactionManager,
            FileAssetCommandFactory commandFactory) {
        this.fileAssetReadManager = fileAssetReadManager;
        this.fileAssetTransactionManager = fileAssetTransactionManager;
        this.statusHistoryTransactionManager = statusHistoryTransactionManager;
        this.commandFactory = commandFactory;
    }

    @Override
    public void markAsFailed(String fileAssetId, String errorMessage) {
        // 1. FileAsset 조회
        Optional<FileAsset> fileAssetOpt =
                fileAssetReadManager.findById(FileAssetId.of(fileAssetId));

        if (fileAssetOpt.isEmpty()) {
            log.warn("FileAsset not found for DLQ processing: id={}", fileAssetId);
            return;
        }

        FileAsset fileAsset = fileAssetOpt.get();

        // 2. 이미 처리된 경우 skip
        if (isAlreadyProcessed(fileAsset)) {
            log.info(
                    "FileAsset 이미 처리됨, DLQ 처리 skip: id={}, status={}",
                    fileAssetId,
                    fileAsset.getStatus());
            return;
        }

        // 3. 도메인 비즈니스 로직 실행 (상태 변경 + StatusHistory 생성)
        FileAssetUpdateResult updateResult = commandFactory.markFailed(fileAsset, errorMessage);

        // 4. 영속화
        fileAssetTransactionManager.persist(updateResult.fileAsset());
        statusHistoryTransactionManager.persist(updateResult.statusHistory());

        log.info("FileAsset FAILED 처리 완료: id={}, errorMessage={}", fileAssetId, errorMessage);
    }

    /**
     * 이미 처리된 상태인지 확인합니다.
     *
     * @param fileAsset FileAsset
     * @return COMPLETED 또는 FAILED 상태인 경우 true
     */
    private boolean isAlreadyProcessed(FileAsset fileAsset) {
        FileAssetStatus status = fileAsset.getStatus();
        return status == FileAssetStatus.COMPLETED || status == FileAssetStatus.FAILED;
    }
}
