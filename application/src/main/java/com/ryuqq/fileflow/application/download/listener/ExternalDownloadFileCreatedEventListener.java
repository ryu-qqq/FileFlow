package com.ryuqq.fileflow.application.download.listener;

import com.ryuqq.fileflow.application.asset.manager.FileAssetManager;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadFileCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 외부 다운로드 파일 생성 이벤트 리스너.
 *
 * <p>ExternalDownload 완료 트랜잭션 커밋 후 FileAsset을 생성하고 저장합니다.
 */
@Component
public class ExternalDownloadFileCreatedEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(ExternalDownloadFileCreatedEventListener.class);

    private final FileAssetManager fileAssetManager;

    public ExternalDownloadFileCreatedEventListener(FileAssetManager fileAssetManager) {
        this.fileAssetManager = fileAssetManager;
    }

    /**
     * 외부 다운로드 파일 생성 이벤트를 처리합니다.
     *
     * <p>원본 트랜잭션 커밋 후 실행됩니다.
     *
     * @param event 외부 다운로드 파일 생성 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ExternalDownloadFileCreatedEvent event) {
        log.info(
                "외부 다운로드 파일 생성 이벤트 처리 시작: downloadId={}, fileName={}",
                event.downloadId().value(),
                event.fileName().name());

        fileAssetManager.createAndPersist(event);

        log.info(
                "외부 다운로드 파일 생성 이벤트 처리 완료: downloadId={}, fileName={}",
                event.downloadId().value(),
                event.fileName().name());
    }
}
