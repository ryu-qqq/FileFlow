package com.ryuqq.fileflow.application.transform.validator;

import com.ryuqq.fileflow.application.asset.manager.query.AssetReadManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformReadManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import org.springframework.stereotype.Component;

@Component
public class TransformExecutionValidator {

    private final TransformReadManager transformReadManager;
    private final AssetReadManager assetReadManager;

    public TransformExecutionValidator(
            TransformReadManager transformReadManager, AssetReadManager assetReadManager) {
        this.transformReadManager = transformReadManager;
        this.assetReadManager = assetReadManager;
    }

    /** 변환 요청을 조회합니다. 없으면 TransformRequestNotFoundException을 던집니다. */
    public TransformRequest getTransformRequest(String transformRequestId) {
        return transformReadManager.getTransformRequest(transformRequestId);
    }

    /** 소스 에셋을 조회합니다. 없으면 AssetNotFoundException을 던집니다. */
    public Asset getSourceAsset(String assetId) {
        return assetReadManager.getAsset(assetId);
    }
}
