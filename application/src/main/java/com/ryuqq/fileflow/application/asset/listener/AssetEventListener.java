package com.ryuqq.fileflow.application.asset.listener;

import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetCommand;
import com.ryuqq.fileflow.application.asset.factory.command.AssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.AssetCommandManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.session.event.UploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.service.S3PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AssetEventListener {

    private static final Logger log = LoggerFactory.getLogger(AssetEventListener.class);

    private final AssetCommandFactory assetCommandFactory;
    private final AssetCommandManager assetCommandManager;

    public AssetEventListener(
            AssetCommandFactory assetCommandFactory, AssetCommandManager assetCommandManager) {
        this.assetCommandFactory = assetCommandFactory;
        this.assetCommandManager = assetCommandManager;
    }

    @EventListener
    public void handleUploadCompleted(UploadCompletedEvent event) {
        try {
            RegisterAssetCommand command = toRegisterAssetCommand(event);
            Asset asset = assetCommandFactory.createAsset(command);
            assetCommandManager.persist(asset);

            log.info("Asset 등록 완료: sessionId={}, assetId={}", event.sessionId(), asset.idValue());
        } catch (Exception e) {
            log.error(
                    "Asset 등록 실패: sessionId={}, sessionType={}",
                    event.sessionId(),
                    event.sessionType(),
                    e);
        }
    }

    private RegisterAssetCommand toRegisterAssetCommand(UploadCompletedEvent event) {
        AssetOrigin origin = resolveOrigin(event.sessionType());
        String extension = S3PathResolver.extractExtension(event.fileName());

        return new RegisterAssetCommand(
                event.s3Key(),
                event.bucket(),
                event.accessType(),
                event.fileName(),
                event.fileSize(),
                event.contentType(),
                event.etag(),
                extension,
                origin,
                event.sessionId(),
                event.purpose(),
                event.source());
    }

    private AssetOrigin resolveOrigin(String sessionType) {
        return switch (sessionType) {
            case "SINGLE" -> AssetOrigin.SINGLE_UPLOAD;
            case "MULTIPART" -> AssetOrigin.MULTIPART_UPLOAD;
            default -> throw new IllegalArgumentException("Unknown session type: " + sessionType);
        };
    }
}
