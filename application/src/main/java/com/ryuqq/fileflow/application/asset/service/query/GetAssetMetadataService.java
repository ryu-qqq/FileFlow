package com.ryuqq.fileflow.application.asset.service.query;

import com.ryuqq.fileflow.application.asset.assembler.AssetMetadataAssembler;
import com.ryuqq.fileflow.application.asset.dto.response.AssetMetadataResponse;
import com.ryuqq.fileflow.application.asset.manager.query.AssetMetadataReadManager;
import com.ryuqq.fileflow.application.asset.port.in.query.GetAssetMetadataUseCase;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import org.springframework.stereotype.Service;

@Service
public class GetAssetMetadataService implements GetAssetMetadataUseCase {

    private final AssetMetadataReadManager assetMetadataReadManager;
    private final AssetMetadataAssembler assetMetadataAssembler;

    public GetAssetMetadataService(
            AssetMetadataReadManager assetMetadataReadManager,
            AssetMetadataAssembler assetMetadataAssembler) {
        this.assetMetadataReadManager = assetMetadataReadManager;
        this.assetMetadataAssembler = assetMetadataAssembler;
    }

    @Override
    public AssetMetadataResponse execute(String assetId) {
        AssetMetadata metadata = assetMetadataReadManager.getAssetMetadata(assetId);
        return assetMetadataAssembler.toResponse(metadata);
    }
}
