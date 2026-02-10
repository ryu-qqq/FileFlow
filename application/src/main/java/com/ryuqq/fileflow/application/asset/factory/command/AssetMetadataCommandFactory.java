package com.ryuqq.fileflow.application.asset.factory.command;

import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetMetadataCommand;
import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.asset.id.AssetMetadataId;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class AssetMetadataCommandFactory {

    private final IdGeneratorPort idGeneratorPort;
    private final TimeProvider timeProvider;

    public AssetMetadataCommandFactory(IdGeneratorPort idGeneratorPort, TimeProvider timeProvider) {
        this.idGeneratorPort = idGeneratorPort;
        this.timeProvider = timeProvider;
    }

    public AssetMetadata createAssetMetadata(RegisterAssetMetadataCommand command) {
        Instant now = timeProvider.now();
        String metadataId = idGeneratorPort.generate();

        return AssetMetadata.forNew(
                AssetMetadataId.of(metadataId),
                AssetId.of(command.assetId()),
                command.width(),
                command.height(),
                command.transformType(),
                now);
    }
}
