package com.ryuqq.fileflow.application.download.factory.command;

import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetCommand;
import com.ryuqq.fileflow.application.asset.factory.command.AssetCommandFactory;
import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadCompletionBundle;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadFailureBundle;
import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.dto.response.FileDownloadResult;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.StorageInfo;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.id.CallbackOutboxId;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.CallbackInfo;
import com.ryuqq.fileflow.domain.download.vo.DownloadedFileInfo;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.session.service.S3PathResolver;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class DownloadCommandFactory {

    private final IdGeneratorPort idGeneratorPort;
    private final TimeProvider timeProvider;
    private final AssetCommandFactory assetCommandFactory;

    public DownloadCommandFactory(
            IdGeneratorPort idGeneratorPort,
            TimeProvider timeProvider,
            AssetCommandFactory assetCommandFactory) {
        this.idGeneratorPort = idGeneratorPort;
        this.timeProvider = timeProvider;
        this.assetCommandFactory = assetCommandFactory;
    }

    public DownloadTask create(CreateDownloadTaskCommand command) {
        Instant now = timeProvider.now();
        String id = idGeneratorPort.generate();

        return DownloadTask.forNew(
                DownloadTaskId.of(id),
                SourceUrl.of(command.sourceUrl()),
                StorageInfo.of(command.bucket(), command.s3Key(), command.accessType()),
                command.purpose(),
                command.source(),
                CallbackInfo.of(command.callbackUrl()),
                now);
    }

    public StatusChangeContext<String> createStartContext(String downloadTaskId) {
        return new StatusChangeContext<>(downloadTaskId, timeProvider.now());
    }

    public DownloadCompletionBundle createCompletionBundle(
            DownloadTask downloadTask, FileDownloadResult result) {
        Instant now = timeProvider.now();

        DownloadedFileInfo fileInfo =
                DownloadedFileInfo.of(
                        result.fileName(),
                        result.contentType(),
                        result.fileSize(),
                        result.etag(),
                        now);

        downloadTask.complete(fileInfo);

        String extension = S3PathResolver.extractExtension(result.fileName());
        RegisterAssetCommand assetCommand =
                new RegisterAssetCommand(
                        downloadTask.s3Key(),
                        downloadTask.bucket(),
                        downloadTask.accessType(),
                        result.fileName(),
                        result.fileSize(),
                        result.contentType(),
                        result.etag(),
                        extension,
                        AssetOrigin.EXTERNAL_DOWNLOAD,
                        downloadTask.idValue(),
                        downloadTask.purpose(),
                        downloadTask.source());

        Asset asset = assetCommandFactory.createAsset(assetCommand);

        CallbackOutbox callbackOutbox = null;
        if (downloadTask.hasCallback()) {
            callbackOutbox =
                    createCallbackOutbox(
                            downloadTask.idValue(),
                            downloadTask.callbackUrl(),
                            downloadTask.status().name());
        }

        return new DownloadCompletionBundle(downloadTask, asset, callbackOutbox);
    }

    public DownloadFailureBundle createFailureBundle(
            DownloadTask downloadTask, String errorMessage) {
        Instant now = timeProvider.now();
        downloadTask.fail(errorMessage, now);

        CallbackOutbox callbackOutbox = null;
        if (!downloadTask.canRetry() && downloadTask.hasCallback()) {
            callbackOutbox =
                    createCallbackOutbox(
                            downloadTask.idValue(),
                            downloadTask.callbackUrl(),
                            downloadTask.status().name());
        }

        return new DownloadFailureBundle(downloadTask, callbackOutbox);
    }

    public CallbackOutbox createCallbackOutbox(
            String downloadTaskId, String callbackUrl, String taskStatus) {
        String id = idGeneratorPort.generate();
        Instant now = timeProvider.now();
        return CallbackOutbox.forNew(
                CallbackOutboxId.of(id), downloadTaskId, callbackUrl, taskStatus, now);
    }
}
