package com.ryuqq.fileflow.application.asset.service;

import com.ryuqq.fileflow.application.asset.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.port.in.command.GenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Presigned Download URL 생성 Service.
 *
 * <p>GenerateDownloadUrlUseCase 구현체입니다.
 *
 * <p>S3 Presigned URL을 발급하여 클라이언트가 직접 S3에서 파일을 다운로드할 수 있도록 합니다.
 */
@Service
public class GenerateDownloadUrlService implements GenerateDownloadUrlUseCase {

    private final FileAssetQueryPort fileAssetQueryPort;
    private final S3ClientPort s3ClientPort;

    public GenerateDownloadUrlService(
            FileAssetQueryPort fileAssetQueryPort, S3ClientPort s3ClientPort) {
        this.fileAssetQueryPort = fileAssetQueryPort;
        this.s3ClientPort = s3ClientPort;
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadUrlResponse execute(GenerateDownloadUrlCommand command) {
        FileAssetId fileAssetId = FileAssetId.of(command.fileAssetId());

        FileAsset fileAsset =
                fileAssetQueryPort
                        .findById(fileAssetId, command.organizationId(), command.tenantId())
                        .orElseThrow(() -> new FileAssetNotFoundException(command.fileAssetId()));

        Duration duration = Duration.ofMinutes(command.expirationMinutes());
        String presignedUrl =
                s3ClientPort.generatePresignedGetUrl(
                        fileAsset.getBucket(), fileAsset.getS3Key(), duration);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(command.expirationMinutes());

        return DownloadUrlResponse.of(
                fileAsset.getIdValue(),
                presignedUrl,
                fileAsset.getFileNameValue(),
                fileAsset.getContentTypeValue(),
                fileAsset.getFileSizeValue(),
                expiresAt);
    }
}
