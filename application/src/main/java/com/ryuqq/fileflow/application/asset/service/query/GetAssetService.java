package com.ryuqq.fileflow.application.asset.service.query;

import com.ryuqq.fileflow.application.asset.assembler.AssetAssembler;
import com.ryuqq.fileflow.application.asset.dto.response.AssetResponse;
import com.ryuqq.fileflow.application.asset.manager.query.AssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.query.GetAssetUseCase;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import org.springframework.stereotype.Service;

@Service
public class GetAssetService implements GetAssetUseCase {

    private final AssetReadManager assetReadManager;
    private final AssetAssembler assetAssembler;

    public GetAssetService(AssetReadManager assetReadManager, AssetAssembler assetAssembler) {
        this.assetReadManager = assetReadManager;
        this.assetAssembler = assetAssembler;
    }

    @Override
    public AssetResponse execute(String assetId) {
        Asset asset = assetReadManager.getAsset(assetId);
        return assetAssembler.toResponse(asset);
    }
}
