package com.ryuqq.fileflow.application.asset.service.query;

import com.ryuqq.fileflow.application.asset.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.command.GenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.asset.port.out.client.AssetS3ClientPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;

/**
 * Presigned Download URL 생성 Service.
 *
 * <p>GenerateDownloadUrlUseCase 구현체입니다.
 *
 * <p>S3 Presigned URL을 발급하여 클라이언트가 직접 S3에서 파일을 다운로드할 수 있도록 합니다.
 */
@Service
public class GenerateDownloadUrlService implements GenerateDownloadUrlUseCase {

    private final FileAssetReadManager fileAssetReadManager;
    private final AssetS3ClientPort assetS3ClientPort;

    public GenerateDownloadUrlService(
            FileAssetReadManager fileAssetReadManager, AssetS3ClientPort assetS3ClientPort) {
        this.fileAssetReadManager = fileAssetReadManager;
        this.assetS3ClientPort = assetS3ClientPort;
    }

    @Override
    public DownloadUrlResponse execute(GenerateDownloadUrlCommand command) {
        FileAssetId fileAssetId = FileAssetId.of(command.fileAssetId());

        FileAsset fileAsset =
                fileAssetReadManager
                        .findById(fileAssetId, command.organizationId(), command.tenantId())
                        .orElseThrow(() -> new FileAssetNotFoundException(command.fileAssetId()));

        Duration duration = Duration.ofMinutes(command.expirationMinutes());
        String presignedUrl =
                assetS3ClientPort.generatePresignedGetUrl(
                        fileAsset.getBucket(), fileAsset.getS3Key(), duration);

        Instant expiresAt = Instant.now().plus(duration);

        return DownloadUrlResponse.of(
                fileAsset.getIdValue(),
                presignedUrl,
                fileAsset.getFileNameValue(),
                fileAsset.getContentTypeValue(),
                fileAsset.getFileSizeValue(),
                expiresAt);
    }
}
