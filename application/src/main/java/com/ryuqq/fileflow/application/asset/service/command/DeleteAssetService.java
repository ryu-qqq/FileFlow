package com.ryuqq.fileflow.application.asset.service.command;

import com.ryuqq.fileflow.application.asset.dto.command.DeleteAssetCommand;
import com.ryuqq.fileflow.application.asset.factory.command.AssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.AssetCommandManager;
import com.ryuqq.fileflow.application.asset.port.in.command.DeleteAssetUseCase;
import com.ryuqq.fileflow.application.asset.validator.AssetPolicyValidator;
import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import org.springframework.stereotype.Service;

@Service
public class DeleteAssetService implements DeleteAssetUseCase {

    private final AssetCommandFactory assetCommandFactory;
    private final AssetPolicyValidator assetPolicyValidator;
    private final AssetCommandManager assetCommandManager;

    public DeleteAssetService(
            AssetCommandFactory assetCommandFactory,
            AssetPolicyValidator assetPolicyValidator,
            AssetCommandManager assetCommandManager) {
        this.assetCommandFactory = assetCommandFactory;
        this.assetPolicyValidator = assetPolicyValidator;
        this.assetCommandManager = assetCommandManager;
    }

    @Override
    public void execute(DeleteAssetCommand command) {
        StatusChangeContext<String> context = assetCommandFactory.createDeleteContext(command);

        Asset asset = assetPolicyValidator.validateCanDelete(context.id(), command.source());
        asset.delete(context.changedAt());

        assetCommandManager.persist(asset);
    }
}
