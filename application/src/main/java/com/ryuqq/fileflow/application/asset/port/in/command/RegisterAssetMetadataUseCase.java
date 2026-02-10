package com.ryuqq.fileflow.application.asset.port.in.command;

import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetMetadataCommand;

public interface RegisterAssetMetadataUseCase {

    void execute(RegisterAssetMetadataCommand command);
}
