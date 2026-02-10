package com.ryuqq.fileflow.application.asset.factory.command;

import com.ryuqq.fileflow.application.asset.dto.command.DeleteAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetCommand;
import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileInfo;
import com.ryuqq.fileflow.domain.common.vo.StorageInfo;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class AssetCommandFactory {

    private final IdGeneratorPort idGeneratorPort;
    private final TimeProvider timeProvider;

    public AssetCommandFactory(IdGeneratorPort idGeneratorPort, TimeProvider timeProvider) {
        this.idGeneratorPort = idGeneratorPort;
        this.timeProvider = timeProvider;
    }

    public Asset createAsset(RegisterAssetCommand command) {
        Instant now = timeProvider.now();
        String assetId = idGeneratorPort.generate();

        StorageInfo storageInfo =
                StorageInfo.of(command.bucket(), command.s3Key(), command.accessType());
        FileInfo fileInfo =
                FileInfo.of(
                        command.fileName(),
                        command.fileSize(),
                        command.contentType(),
                        command.etag(),
                        command.extension());

        return Asset.forNew(
                AssetId.of(assetId),
                storageInfo,
                fileInfo,
                command.origin(),
                command.originId(),
                command.purpose(),
                command.source(),
                now);
    }

    public StatusChangeContext<String> createDeleteContext(DeleteAssetCommand command) {
        return new StatusChangeContext<>(command.assetId(), timeProvider.now());
    }
}
