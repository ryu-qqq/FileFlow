package com.ryuqq.fileflow.application.file.port.in;

import com.ryuqq.fileflow.application.file.dto.command.UploadCompletedCommand;

/**
 * Upload 완료 이벤트 소비 UseCase (Input Port)
 *
 * <p>Upload Session이 완료되었을 때 FileAsset Aggregate를 생성합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession 완료 이벤트 수신</li>
 *   <li>FileAsset Aggregate 생성 (Event-Driven)</li>
 *   <li>FileAssetCreatedEvent 발행</li>
 * </ul>
 *
 * <p><strong>Event-Driven Architecture:</strong></p>
 * <pre>
 * CompleteMultipartUploadService
 *     → UploadSession.complete()
 *         → Domain Event 발행 (Spring Events)
 *             → @TransactionalEventListener(AFTER_COMMIT)
 *                 → ConsumeUploadCompletedUseCase
 *                     → FileAsset.fromUploadCompleted()
 *                         → FileAssetCreatedEvent 발행
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface ConsumeUploadCompletedUseCase {

    /**
     * Upload 완료 이벤트 소비 및 FileAsset 생성
     *
     * @param command Upload 완료 Command
     */
    void execute(UploadCompletedCommand command);
}
