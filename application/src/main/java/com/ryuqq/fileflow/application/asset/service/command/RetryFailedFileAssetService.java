package com.ryuqq.fileflow.application.asset.service.command;

import com.ryuqq.fileflow.application.asset.dto.command.RetryFailedFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.response.RetryFailedFileAssetResponse;
import com.ryuqq.fileflow.application.asset.factory.command.FileAssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.FileAssetTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.command.FileProcessingOutboxTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.command.RetryFailedFileAssetUseCase;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import org.springframework.stereotype.Service;

/**
 * 실패한 FileAsset 재처리 Service.
 *
 * <p>RetryFailedFileAssetUseCase 구현체입니다.
 *
 * <p><strong>처리 흐름:</strong>
 *
 * <ol>
 *   <li>FileAsset 조회 (FAILED 상태 검증)
 *   <li>FileAsset.retry() 호출 → PENDING 상태로 변경
 *   <li>FileProcessingOutbox 재처리 메시지 생성
 *   <li>영속화
 * </ol>
 */
@Service
public class RetryFailedFileAssetService implements RetryFailedFileAssetUseCase {

    private final FileAssetReadManager fileAssetReadManager;
    private final FileAssetTransactionManager fileAssetTransactionManager;
    private final FileProcessingOutboxTransactionManager outboxTransactionManager;
    private final FileAssetCommandFactory commandFactory;

    public RetryFailedFileAssetService(
            FileAssetReadManager fileAssetReadManager,
            FileAssetTransactionManager fileAssetTransactionManager,
            FileProcessingOutboxTransactionManager outboxTransactionManager,
            FileAssetCommandFactory commandFactory) {
        this.fileAssetReadManager = fileAssetReadManager;
        this.fileAssetTransactionManager = fileAssetTransactionManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.commandFactory = commandFactory;
    }

    @Override
    public RetryFailedFileAssetResponse execute(RetryFailedFileAssetCommand command) {
        FileAssetId fileAssetId = FileAssetId.of(command.fileAssetId());
        FileAsset fileAsset =
                fileAssetReadManager
                        .findById(fileAssetId, command.organizationId(), command.tenantId())
                        .orElseThrow(() -> new FileAssetNotFoundException(command.fileAssetId()));

        fileAsset.retry();
        fileAssetTransactionManager.persist(fileAsset);

        FileProcessingOutbox outbox =
                FileProcessingOutbox.forRetryRequest(
                        fileAssetId,
                        "Admin retry request",
                        buildRetryPayload(fileAsset),
                        commandFactory.getClock());
        outboxTransactionManager.persist(outbox);

        return RetryFailedFileAssetResponse.of(
                command.fileAssetId(), FileAssetStatus.PENDING.name());
    }

    private String buildRetryPayload(FileAsset fileAsset) {
        return String.format(
                "{\"fileAssetId\":\"%s\",\"fileName\":\"%s\",\"bucket\":\"%s\",\"s3Key\":\"%s\"}",
                fileAsset.getIdValue(),
                fileAsset.getFileNameValue(),
                fileAsset.getBucketValue(),
                fileAsset.getS3KeyValue());
    }
}
