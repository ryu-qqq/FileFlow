package com.ryuqq.fileflow.application.asset.service;

import com.ryuqq.fileflow.application.asset.dto.command.DeleteFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.response.DeleteFileAssetResponse;
import com.ryuqq.fileflow.application.asset.port.in.command.DeleteFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.port.out.command.FileAssetPersistencePort;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileAsset 삭제 Service.
 *
 * <p>DeleteFileAssetUseCase 구현체입니다.
 */
@Service
public class DeleteFileAssetService implements DeleteFileAssetUseCase {

    private final FileAssetQueryPort fileAssetQueryPort;
    private final FileAssetPersistencePort fileAssetPersistencePort;

    public DeleteFileAssetService(
            FileAssetQueryPort fileAssetQueryPort,
            FileAssetPersistencePort fileAssetPersistencePort) {
        this.fileAssetQueryPort = fileAssetQueryPort;
        this.fileAssetPersistencePort = fileAssetPersistencePort;
    }

    @Override
    @Transactional
    public DeleteFileAssetResponse execute(DeleteFileAssetCommand command) {
        FileAssetId fileAssetId = FileAssetId.of(command.fileAssetId());
        FileAsset fileAsset =
                fileAssetQueryPort
                        .findById(fileAssetId, command.organizationId(), command.tenantId())
                        .orElseThrow(() -> new FileAssetNotFoundException(command.fileAssetId()));

        fileAsset.delete();
        fileAssetPersistencePort.persist(fileAsset);

        return DeleteFileAssetResponse.of(command.fileAssetId(), fileAsset.getDeletedAt());
    }
}
