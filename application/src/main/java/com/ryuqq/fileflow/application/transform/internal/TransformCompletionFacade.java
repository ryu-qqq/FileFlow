package com.ryuqq.fileflow.application.transform.internal;

import com.ryuqq.fileflow.application.asset.manager.command.AssetCommandManager;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformCompletionBundle;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformFailureBundle;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCommandManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.vo.ImageDimension;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransformCompletionFacade {

    private final AssetCommandManager assetCommandManager;
    private final TransformCommandManager transformCommandManager;
    private final TransformCallbackOutboxCommandManager transformCallbackOutboxCommandManager;

    public TransformCompletionFacade(
            AssetCommandManager assetCommandManager,
            TransformCommandManager transformCommandManager,
            TransformCallbackOutboxCommandManager transformCallbackOutboxCommandManager) {
        this.assetCommandManager = assetCommandManager;
        this.transformCommandManager = transformCommandManager;
        this.transformCallbackOutboxCommandManager = transformCallbackOutboxCommandManager;
    }

    @Transactional
    public void complete(TransformCompletionBundle bundle) {
        Asset resultAsset = bundle.resultAsset();
        TransformRequest request = bundle.request();
        ImageDimension dimension = bundle.dimension();

        assetCommandManager.persist(resultAsset);
        request.complete(
                resultAsset.id(), dimension.width(), dimension.height(), bundle.completedAt());
        transformCommandManager.persist(request);

        if (bundle.hasCallback()) {
            transformCallbackOutboxCommandManager.persist(bundle.callbackOutbox());
        }
    }

    @Transactional
    public void fail(TransformFailureBundle bundle) {
        TransformRequest request = bundle.request();

        request.fail(bundle.errorMessage(), bundle.failedAt());
        transformCommandManager.persist(request);

        if (bundle.hasCallback()) {
            transformCallbackOutboxCommandManager.persist(bundle.callbackOutbox());
        }
    }
}
