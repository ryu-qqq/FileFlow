package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.dto.command.UploadCompletedCommand;
import com.ryuqq.fileflow.application.file.manager.FileCommandManager;
import com.ryuqq.fileflow.application.file.port.in.ConsumeUploadCompletedUseCase;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Upload 완료 처리 Service
 *
 * <p>Upload Session 완료 시 FileAsset Aggregate를 생성하는 UseCase입니다.</p>
 *
 * <p><strong>실행 흐름:</strong></p>
 * <ol>
 *   <li>Upload 완료 Command 수신</li>
 *   <li>FileAsset.create() 호출</li>
 *   <li>FileAsset 저장</li>
 * </ol>
 *
 * <p><strong>트랜잭션 전략:</strong></p>
 * <ul>
 *   <li>✅ @Transactional - 새 트랜잭션 생성</li>
 *   <li>✅ CompleteMultipartUploadService에서 호출됨</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class ConsumeUploadCompletedService implements ConsumeUploadCompletedUseCase {

    private final FileCommandManager fileCommandManager;

    public ConsumeUploadCompletedService(FileCommandManager fileCommandManager) {
        this.fileCommandManager = fileCommandManager;
    }

    @Transactional
    @Override
    public void execute(UploadCompletedCommand command) {
        // FileAsset Aggregate 생성
        FileAsset fileAsset = FileAsset.forNew(
            command.tenantId(),
            command.organizationId(),
            command.ownerUserId(),
            command.fileName(),
            command.fileSize(),
            command.mimeType(),
            command.storageKey(),
            command.checksum(),
            command.uploadSessionId()
        );

        // FileAsset 저장
        fileCommandManager.save(fileAsset);
    }
}
