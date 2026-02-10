package com.ryuqq.fileflow.application.asset.listener;

import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetMetadataCommand;
import com.ryuqq.fileflow.application.asset.factory.command.AssetMetadataCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.AssetMetadataCommandManager;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.transform.event.TransformCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AssetMetadataEventListener {

    private static final Logger log = LoggerFactory.getLogger(AssetMetadataEventListener.class);

    private final AssetMetadataCommandFactory assetMetadataCommandFactory;
    private final AssetMetadataCommandManager assetMetadataCommandManager;

    public AssetMetadataEventListener(
            AssetMetadataCommandFactory assetMetadataCommandFactory,
            AssetMetadataCommandManager assetMetadataCommandManager) {
        this.assetMetadataCommandFactory = assetMetadataCommandFactory;
        this.assetMetadataCommandManager = assetMetadataCommandManager;
    }

    @EventListener
    public void handleTransformCompleted(TransformCompletedEvent event) {
        try {
            RegisterAssetMetadataCommand command =
                    new RegisterAssetMetadataCommand(
                            event.resultAssetId(),
                            event.resultWidth(),
                            event.resultHeight(),
                            event.transformType());

            AssetMetadata metadata = assetMetadataCommandFactory.createAssetMetadata(command);
            assetMetadataCommandManager.persist(metadata);

            log.info(
                    "AssetMetadata 등록 완료: resultAssetId={}, transformType={}, {}x{}",
                    event.resultAssetId(),
                    event.transformType(),
                    event.resultWidth(),
                    event.resultHeight());
        } catch (Exception e) {
            log.error(
                    "AssetMetadata 등록 실패: resultAssetId={}, transformRequestId={}",
                    event.resultAssetId(),
                    event.transformRequestId(),
                    e);
        }
    }
}
