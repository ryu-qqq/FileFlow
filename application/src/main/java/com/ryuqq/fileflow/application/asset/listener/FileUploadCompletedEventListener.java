package com.ryuqq.fileflow.application.asset.listener;

import com.ryuqq.fileflow.application.asset.manager.FileAssetManager;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 파일 업로드 완료 이벤트 리스너.
 *
 * <p>트랜잭션 커밋 후 FileAsset을 생성하고 저장합니다.
 */
@Component
public class FileUploadCompletedEventListener {

    private final FileAssetManager fileAssetManager;

    public FileUploadCompletedEventListener(FileAssetManager fileAssetManager) {
        this.fileAssetManager = fileAssetManager;
    }

    /**
     * 파일 업로드 완료 이벤트를 처리합니다.
     *
     * <p>원본 트랜잭션 커밋 후 실행됩니다.
     *
     * @param event 파일 업로드 완료 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FileUploadCompletedEvent event) {
        fileAssetManager.createAndPersist(event);
    }
}
