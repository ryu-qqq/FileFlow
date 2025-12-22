package com.ryuqq.fileflow.application.asset.service.command;

import com.ryuqq.fileflow.application.asset.dto.command.BatchDeleteFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDeleteFileAssetResponse;
import com.ryuqq.fileflow.application.asset.factory.command.FileAssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.FileAssetTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.command.BatchDeleteFileAssetUseCase;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * FileAsset 일괄 삭제 Service.
 *
 * <p>BatchDeleteFileAssetUseCase 구현체입니다.
 *
 * <p><strong>처리 흐름:</strong>
 *
 * <ol>
 *   <li>각 FileAsset ID에 대해 조회
 *   <li>존재하는 경우 delete() 호출
 *   <li>실패한 경우 에러 정보 수집 (Partial Success)
 *   <li>결과 반환
 * </ol>
 */
@Service
public class BatchDeleteFileAssetService implements BatchDeleteFileAssetUseCase {

    private final FileAssetReadManager fileAssetReadManager;
    private final FileAssetTransactionManager fileAssetTransactionManager;
    private final FileAssetCommandFactory commandFactory;

    public BatchDeleteFileAssetService(
            FileAssetReadManager fileAssetReadManager,
            FileAssetTransactionManager fileAssetTransactionManager,
            FileAssetCommandFactory commandFactory) {
        this.fileAssetReadManager = fileAssetReadManager;
        this.fileAssetTransactionManager = fileAssetTransactionManager;
        this.commandFactory = commandFactory;
    }

    @Override
    public BatchDeleteFileAssetResponse execute(BatchDeleteFileAssetCommand command) {
        List<BatchDeleteFileAssetResponse.DeletedAsset> deletedAssets = new ArrayList<>();
        List<BatchDeleteFileAssetResponse.FailedDelete> failures = new ArrayList<>();

        for (String fileAssetIdStr : command.fileAssetIds()) {
            try {
                processDelete(fileAssetIdStr, command, deletedAssets);
            } catch (IllegalStateException e) {
                failures.add(
                        BatchDeleteFileAssetResponse.FailedDelete.of(
                                fileAssetIdStr, "INVALID_STATE", e.getMessage()));
            } catch (Exception e) {
                failures.add(
                        BatchDeleteFileAssetResponse.FailedDelete.of(
                                fileAssetIdStr, "DELETE_FAILED", e.getMessage()));
            }
        }

        return BatchDeleteFileAssetResponse.of(deletedAssets, failures);
    }

    private void processDelete(
            String fileAssetIdStr,
            BatchDeleteFileAssetCommand command,
            List<BatchDeleteFileAssetResponse.DeletedAsset> deletedAssets) {
        FileAssetId fileAssetId = FileAssetId.of(fileAssetIdStr);
        Optional<FileAsset> optionalFileAsset =
                fileAssetReadManager.findById(
                        fileAssetId, command.organizationId(), command.tenantId());

        if (optionalFileAsset.isEmpty()) {
            throw new IllegalStateException("FileAsset을 찾을 수 없습니다: " + fileAssetIdStr);
        }

        FileAsset fileAsset = optionalFileAsset.get();
        fileAsset.delete(commandFactory.getClock());
        fileAssetTransactionManager.persist(fileAsset);

        deletedAssets.add(
                BatchDeleteFileAssetResponse.DeletedAsset.of(
                        fileAssetIdStr, fileAsset.getDeletedAt()));
    }
}
