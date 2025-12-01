package com.ryuqq.fileflow.application.asset.listener;

import com.ryuqq.fileflow.application.asset.manager.FileAssetManager;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 파일 업로드 완료 이벤트 리스너.
 *
 * <p>트랜잭션 커밋 후 FileAsset을 생성하고 저장합니다.
 */
@Component
public class FileUploadCompletedEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(FileUploadCompletedEventListener.class);

    private final FileAssetManager fileAssetManager;

    public FileUploadCompletedEventListener(FileAssetManager fileAssetManager) {
        this.fileAssetManager = fileAssetManager;
    }

    /**
     * 파일 업로드 완료 이벤트를 처리합니다.
     *
     * <p>원본 트랜잭션 커밋 후 실행됩니다.
     *
     * <p><strong>트랜잭션 전파</strong>: REQUIRES_NEW를 사용하여 독립적인 트랜잭션에서 실행됩니다.
     *
     * @param event 파일 업로드 완료 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(FileUploadCompletedEvent event) {
        log.info(
                "Received FileUploadCompletedEvent: sessionId={}, bucket={}, key={}",
                event.sessionId().getValue(),
                event.bucket().bucketName(),
                event.s3Key().key());
        try {
            fileAssetManager.createAndPersist(event);
            log.info(
                    "FileAsset created successfully for sessionId={}",
                    event.sessionId().getValue());
        } catch (Exception e) {
            log.error(
                    "Failed to create FileAsset for sessionId={}: {}",
                    event.sessionId().getValue(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}
