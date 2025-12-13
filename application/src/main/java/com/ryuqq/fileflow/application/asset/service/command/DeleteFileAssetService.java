package com.ryuqq.fileflow.application.asset.service.command;

import com.ryuqq.fileflow.application.asset.dto.command.DeleteFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.response.DeleteFileAssetResponse;
import com.ryuqq.fileflow.application.asset.factory.command.FileAssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.FileAssetTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.command.DeleteFileAssetUseCase;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import org.springframework.stereotype.Service;

/**
 * FileAsset 삭제 Service.
 *
 * <p>DeleteFileAssetUseCase 구현체입니다.
 */
@Service
public class DeleteFileAssetService implements DeleteFileAssetUseCase {

    private final FileAssetReadManager fileAssetReadManager;
    private final FileAssetTransactionManager fileAssetTransactionManager;
    private final FileAssetCommandFactory commandFactory;

    public DeleteFileAssetService(
            FileAssetReadManager fileAssetReadManager,
            FileAssetTransactionManager fileAssetTransactionManager,
            FileAssetCommandFactory commandFactory) {
        this.fileAssetReadManager = fileAssetReadManager;
        this.fileAssetTransactionManager = fileAssetTransactionManager;
        this.commandFactory = commandFactory;
    }

    @Override
    public DeleteFileAssetResponse execute(DeleteFileAssetCommand command) {
        FileAssetId fileAssetId = FileAssetId.of(command.fileAssetId());
        FileAsset fileAsset =
                fileAssetReadManager
                        .findById(fileAssetId, command.organizationId(), command.tenantId())
                        .orElseThrow(() -> new FileAssetNotFoundException(command.fileAssetId()));

        fileAsset.delete(commandFactory.getClock());
        fileAssetTransactionManager.persist(fileAsset);

        return DeleteFileAssetResponse.of(command.fileAssetId(), fileAsset.getDeletedAt());
    }
}
