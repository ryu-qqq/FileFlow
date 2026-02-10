package com.ryuqq.fileflow.application.asset.assembler;

import com.ryuqq.fileflow.application.asset.dto.response.AssetResponse;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import org.springframework.stereotype.Component;

@Component
public class AssetAssembler {

    public AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.idValue(),
                asset.s3Key(),
                asset.bucket(),
                asset.accessType(),
                asset.fileName(),
                asset.fileSize(),
                asset.contentType(),
                asset.etag(),
                asset.extension(),
                asset.origin(),
                asset.originId(),
                asset.purpose(),
                asset.source(),
                asset.createdAt());
    }
}
