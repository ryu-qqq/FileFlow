package com.ryuqq.fileflow.application.asset.service;

import com.ryuqq.fileflow.application.asset.dto.command.BatchGenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDownloadUrlResponse.FailedDownloadUrl;
import com.ryuqq.fileflow.application.asset.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.port.in.command.BatchGenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Presigned Download URL 일괄 생성 Service.
 *
 * <p>BatchGenerateDownloadUrlUseCase 구현체입니다.
 *
 * <p>여러 파일에 대해 S3 Presigned URL을 일괄 발급하며, 부분 실패를 허용합니다.
 */
@Service
public class BatchGenerateDownloadUrlService implements BatchGenerateDownloadUrlUseCase {

    private static final int MAX_BATCH_SIZE = 100;
    private static final String ERROR_CODE_NOT_FOUND = "FILE_ASSET_NOT_FOUND";
    private static final String ERROR_CODE_BATCH_SIZE_EXCEEDED = "BATCH_SIZE_EXCEEDED";

    private final FileAssetQueryPort fileAssetQueryPort;
    private final S3ClientPort s3ClientPort;

    public BatchGenerateDownloadUrlService(
            FileAssetQueryPort fileAssetQueryPort, S3ClientPort s3ClientPort) {
        this.fileAssetQueryPort = fileAssetQueryPort;
        this.s3ClientPort = s3ClientPort;
    }

    @Override
    @Transactional(readOnly = true)
    public BatchDownloadUrlResponse execute(BatchGenerateDownloadUrlCommand command) {
        List<String> fileAssetIds = command.fileAssetIds();

        if (fileAssetIds.size() > MAX_BATCH_SIZE) {
            return createBatchSizeExceededResponse(fileAssetIds);
        }

        List<DownloadUrlResponse> successResponses = new ArrayList<>();
        List<FailedDownloadUrl> failures = new ArrayList<>();

        Duration duration = Duration.ofMinutes(command.expirationMinutes());
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(command.expirationMinutes());

        for (String fileAssetIdStr : fileAssetIds) {
            processFileAsset(
                    fileAssetIdStr,
                    command.organizationId(),
                    command.tenantId(),
                    duration,
                    expiresAt,
                    successResponses,
                    failures);
        }

        return BatchDownloadUrlResponse.of(successResponses, failures);
    }

    private void processFileAsset(
            String fileAssetIdStr,
            Long organizationId,
            Long tenantId,
            Duration duration,
            LocalDateTime expiresAt,
            List<DownloadUrlResponse> successResponses,
            List<FailedDownloadUrl> failures) {

        FileAssetId fileAssetId = FileAssetId.of(fileAssetIdStr);
        Optional<FileAsset> fileAssetOpt =
                fileAssetQueryPort.findById(fileAssetId, organizationId, tenantId);

        if (fileAssetOpt.isEmpty()) {
            failures.add(
                    FailedDownloadUrl.of(
                            fileAssetIdStr,
                            ERROR_CODE_NOT_FOUND,
                            "파일 자산을 찾을 수 없습니다: " + fileAssetIdStr));
            return;
        }

        FileAsset fileAsset = fileAssetOpt.get();
        String presignedUrl =
                s3ClientPort.generatePresignedGetUrl(
                        fileAsset.getBucket(), fileAsset.getS3Key(), duration);

        successResponses.add(
                DownloadUrlResponse.of(
                        fileAsset.getIdValue(),
                        presignedUrl,
                        fileAsset.getFileNameValue(),
                        fileAsset.getContentTypeValue(),
                        fileAsset.getFileSizeValue(),
                        expiresAt));
    }

    private BatchDownloadUrlResponse createBatchSizeExceededResponse(List<String> fileAssetIds) {
        List<FailedDownloadUrl> failures =
                fileAssetIds.stream()
                        .map(
                                id ->
                                        FailedDownloadUrl.of(
                                                id,
                                                ERROR_CODE_BATCH_SIZE_EXCEEDED,
                                                "일괄 처리 최대 건수(" + MAX_BATCH_SIZE + "개)를 초과했습니다."))
                        .toList();

        return BatchDownloadUrlResponse.of(List.of(), failures);
    }
}
