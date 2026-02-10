package com.ryuqq.fileflow.application.transform.factory.command;

import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetCommand;
import com.ryuqq.fileflow.application.asset.factory.command.AssetCommandFactory;
import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformCompletionBundle;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformFailureBundle;
import com.ryuqq.fileflow.application.transform.dto.command.CreateTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.dto.result.ImageTransformResult;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.asset.vo.FileInfo;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.id.TransformRequestId;
import com.ryuqq.fileflow.domain.transform.vo.TransformParams;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class TransformCommandFactory {

    private final IdGeneratorPort idGeneratorPort;
    private final TimeProvider timeProvider;
    private final AssetCommandFactory assetCommandFactory;

    public TransformCommandFactory(
            IdGeneratorPort idGeneratorPort,
            TimeProvider timeProvider,
            AssetCommandFactory assetCommandFactory) {
        this.idGeneratorPort = idGeneratorPort;
        this.timeProvider = timeProvider;
        this.assetCommandFactory = assetCommandFactory;
    }

    public StatusChangeContext<String> createStartContext(String transformRequestId) {
        return new StatusChangeContext<>(transformRequestId, timeProvider.now());
    }

    public TransformCompletionBundle createCompletionBundle(
            ImageTransformResult result, TransformRequest request, Asset sourceAsset) {
        Asset resultAsset = createResultAsset(result, request, sourceAsset);
        Instant completedAt = timeProvider.now();
        return new TransformCompletionBundle(resultAsset, request, result.dimension(), completedAt);
    }

    public TransformFailureBundle createFailureBundle(
            TransformRequest request, ImageTransformResult result) {
        Instant failedAt = timeProvider.now();
        return new TransformFailureBundle(request, result.errorMessage(), failedAt);
    }

    public TransformRequest createTransformRequest(
            CreateTransformRequestCommand command, String sourceContentType) {
        Instant now = timeProvider.now();
        String id = idGeneratorPort.generate();

        TransformType type = TransformType.valueOf(command.transformType());
        TransformParams params =
                new TransformParams(
                        command.width(),
                        command.height(),
                        false,
                        command.targetFormat(),
                        command.quality());

        return TransformRequest.forNew(
                TransformRequestId.of(id),
                AssetId.of(command.sourceAssetId()),
                sourceContentType,
                type,
                params,
                now);
    }

    private Asset createResultAsset(
            ImageTransformResult result, TransformRequest request, Asset sourceAsset) {
        FileInfo fileInfo = result.fileInfo();
        RegisterAssetCommand command =
                new RegisterAssetCommand(
                        result.s3Key(),
                        result.bucket(),
                        sourceAsset.accessType(),
                        fileInfo.fileName(),
                        fileInfo.fileSize(),
                        fileInfo.contentType(),
                        fileInfo.etag(),
                        fileInfo.extension(),
                        AssetOrigin.TRANSFORM,
                        request.idValue(),
                        sourceAsset.purpose(),
                        sourceAsset.source());
        return assetCommandFactory.createAsset(command);
    }
}
