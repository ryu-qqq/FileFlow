package com.ryuqq.fileflow.application.asset.assembler;

import com.ryuqq.fileflow.application.asset.dto.response.AssetMetadataResponse;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import org.springframework.stereotype.Component;

@Component
public class AssetMetadataAssembler {

    public AssetMetadataResponse toResponse(AssetMetadata metadata) {
        return new AssetMetadataResponse(
                metadata.idValue(),
                metadata.assetIdValue(),
                metadata.width(),
                metadata.height(),
                metadata.transformType(),
                metadata.createdAt());
    }
}
