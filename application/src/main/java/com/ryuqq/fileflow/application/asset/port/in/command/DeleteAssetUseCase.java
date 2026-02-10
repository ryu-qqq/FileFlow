package com.ryuqq.fileflow.application.asset.port.in.command;

import com.ryuqq.fileflow.application.asset.dto.command.DeleteAssetCommand;

public interface DeleteAssetUseCase {

    void execute(DeleteAssetCommand command);
}
