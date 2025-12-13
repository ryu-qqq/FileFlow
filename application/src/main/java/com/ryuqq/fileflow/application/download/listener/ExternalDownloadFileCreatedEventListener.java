package com.ryuqq.fileflow.application.download.listener;

import com.ryuqq.fileflow.application.asset.facade.FileAssetCreationFacade;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadFileCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 외부 다운로드 파일 생성 이벤트 리스너.
 *
 * <p>ExternalDownload 완료 트랜잭션 커밋 후 FileAsset, StatusHistory, Outbox를 저장하고 SQS 발행을 트리거합니다.
 *
 * <p><strong>Transactional Outbox 패턴</strong>:
 *
 * <ol>
 *   <li>FileAsset + StatusHistory + Outbox 원자적 저장 (Facade)
 *   <li>FileProcessingRequestedEvent 발행
 *   <li>트랜잭션 커밋 후 AFTER_COMMIT 리스너에서 SQS 발행
 * </ol>
 */
@Component
public class ExternalDownloadFileCreatedEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(ExternalDownloadFileCreatedEventListener.class);

    private final FileAssetCreationFacade fileAssetCreationFacade;

    public ExternalDownloadFileCreatedEventListener(
            FileAssetCreationFacade fileAssetCreationFacade) {
        this.fileAssetCreationFacade = fileAssetCreationFacade;
    }

    /**
     * 외부 다운로드 파일 생성 이벤트를 처리합니다.
     *
     * <p>원본 트랜잭션 커밋 후 실행됩니다.
     *
     * <p><strong>트랜잭션 전파</strong>: REQUIRES_NEW를 사용하여 독립적인 트랜잭션에서 실행됩니다.
     *
     * <p><strong>처리 내용</strong>:
     *
     * <ul>
     *   <li>FileAsset 생성 및 저장
     *   <li>FileAssetStatusHistory 저장 (PENDING)
     *   <li>FileProcessingOutbox 저장
     *   <li>FileProcessingRequestedEvent 발행 (SQS 발행 트리거)
     * </ul>
     *
     * @param event 외부 다운로드 파일 생성 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(ExternalDownloadFileCreatedEvent event) {
        log.info(
                "외부 다운로드 파일 생성 이벤트 수신: downloadId={}, fileName={}",
                event.downloadId().value(),
                event.fileName().name());

        try {
            FileAssetId fileAssetId = fileAssetCreationFacade.createWithOutbox(event);
            log.info(
                    "외부 다운로드 파일 생성 완료: downloadId={}, fileAssetId={}",
                    event.downloadId().value(),
                    fileAssetId.getValue());
        } catch (Exception e) {
            log.error(
                    "외부 다운로드 파일 생성 실패: downloadId={}, error={}",
                    event.downloadId().value(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}
