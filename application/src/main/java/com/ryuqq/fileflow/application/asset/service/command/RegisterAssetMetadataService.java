package com.ryuqq.fileflow.application.asset.service.command;

import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetMetadataCommand;
import com.ryuqq.fileflow.application.asset.factory.command.AssetMetadataCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.AssetMetadataCommandManager;
import com.ryuqq.fileflow.application.asset.port.in.command.RegisterAssetMetadataUseCase;
import com.ryuqq.fileflow.application.asset.validator.AssetExistenceValidator;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterAssetMetadataService implements RegisterAssetMetadataUseCase {

    private final AssetExistenceValidator assetExistenceValidator;
    private final AssetMetadataCommandFactory assetMetadataCommandFactory;
    private final AssetMetadataCommandManager assetMetadataCommandManager;

    public RegisterAssetMetadataService(
            AssetExistenceValidator assetExistenceValidator,
            AssetMetadataCommandFactory assetMetadataCommandFactory,
            AssetMetadataCommandManager assetMetadataCommandManager) {
        this.assetExistenceValidator = assetExistenceValidator;
        this.assetMetadataCommandFactory = assetMetadataCommandFactory;
        this.assetMetadataCommandManager = assetMetadataCommandManager;
    }

    @Transactional
    @Override
    public void execute(RegisterAssetMetadataCommand command) {
        assetExistenceValidator.validateExists(command.assetId());
        AssetMetadata metadata = assetMetadataCommandFactory.createAssetMetadata(command);
        assetMetadataCommandManager.persist(metadata);
    }
}
