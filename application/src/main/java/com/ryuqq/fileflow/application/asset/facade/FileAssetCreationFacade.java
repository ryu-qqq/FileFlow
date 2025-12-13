package com.ryuqq.fileflow.application.asset.facade;

import com.ryuqq.fileflow.application.asset.manager.command.FileAssetStatusHistoryTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.command.FileAssetTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.command.FileProcessingOutboxTransactionManager;
import com.ryuqq.fileflow.application.asset.publisher.FileAssetEventPublisher;
import com.ryuqq.fileflow.domain.asset.service.FileAssetCreationResult;
import com.ryuqq.fileflow.domain.asset.service.FileAssetCreationService;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadFileCreatedEvent;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileAsset 생성 Facade.
 *
 * <p>FileAsset 생성 시 관련 엔티티(StatusHistory, Outbox)를 함께 저장하고 이벤트를 발행합니다.
 *
 * <p><strong>DDD 원칙</strong>:
 *
 * <ul>
 *   <li>이벤트 → FileAsset 변환은 Domain Service에서 처리 (Application Layer가 아닌)
 *   <li>도메인 이벤트는 Domain Service에서 생성됨 (Facade는 발행만 담당)
 *   <li>Facade는 인프라스트럭처 관심사(영속화, 이벤트 발행)만 처리
 * </ul>
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Domain Service 호출 (Aggregate + 도메인 이벤트 생성 위임)
 *   <li>영속화 (Manager들 호출)
 *   <li>도메인 이벤트 발행 (트랜잭션 커밋 후 SQS 발행 트리거)
 * </ul>
 *
 * <p><strong>트랜잭션 경계</strong>:
 *
 * <ul>
 *   <li>모든 저장 작업은 하나의 트랜잭션에서 처리
 *   <li>이벤트 발행 후 트랜잭션 커밋 시 AFTER_COMMIT 리스너에서 SQS 발행
 * </ul>
 */
@Component
public class FileAssetCreationFacade {

    private static final Logger log = LoggerFactory.getLogger(FileAssetCreationFacade.class);

    private final FileAssetCreationService fileAssetCreationService;
    private final FileAssetTransactionManager fileAssetTransactionManager;
    private final FileAssetStatusHistoryTransactionManager statusHistoryTransactionManager;
    private final FileProcessingOutboxTransactionManager outboxTransactionManager;
    private final FileAssetEventPublisher eventPublisher;

    public FileAssetCreationFacade(
            FileAssetCreationService fileAssetCreationService,
            FileAssetTransactionManager fileAssetTransactionManager,
            FileAssetStatusHistoryTransactionManager statusHistoryTransactionManager,
            FileProcessingOutboxTransactionManager outboxTransactionManager,
            FileAssetEventPublisher eventPublisher) {
        this.fileAssetCreationService = fileAssetCreationService;
        this.fileAssetTransactionManager = fileAssetTransactionManager;
        this.statusHistoryTransactionManager = statusHistoryTransactionManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 파일 업로드 완료 이벤트로부터 FileAsset을 생성하고 가공 요청을 등록합니다.
     *
     * @param event 파일 업로드 완료 이벤트
     * @return 생성된 FileAsset ID
     */
    @Transactional
    public FileAssetId createWithOutbox(FileUploadCompletedEvent event) {
        log.info(
                "FileAsset 생성 시작: sessionId={}, fileName={}",
                event.sessionId().getValue(),
                event.fileName().name());

        // 1. Domain Service에서 FileAsset + 관련 Aggregate + 도메인 이벤트 생성
        FileAssetCreationResult result = fileAssetCreationService.createFromUploadEvent(event);

        // 2. 영속화 (Managers)
        FileAssetId savedId = persistAll(result);

        // 3. 도메인 이벤트 발행 (Domain Service에서 생성된 이벤트를 발행만 함)
        publishDomainEvent(result);

        log.info(
                "FileAsset 생성 완료: fileAssetId={}, outboxId={}",
                savedId.getValue(),
                result.outbox().getId().value());

        return savedId;
    }

    /**
     * 외부 다운로드 완료 이벤트로부터 FileAsset을 생성하고 가공 요청을 등록합니다.
     *
     * @param event 외부 다운로드 파일 생성 이벤트
     * @return 생성된 FileAsset ID
     */
    @Transactional
    public FileAssetId createWithOutbox(ExternalDownloadFileCreatedEvent event) {
        log.info(
                "FileAsset 생성 시작 (ExternalDownload): downloadId={}, fileName={}",
                event.downloadId().value(),
                event.fileName().name());

        // 1. Domain Service에서 FileAsset + 관련 Aggregate + 도메인 이벤트 생성
        FileAssetCreationResult result =
                fileAssetCreationService.createFromExternalDownloadEvent(event);

        // 2. 영속화 (Managers)
        FileAssetId savedId = persistAll(result);

        // 3. 도메인 이벤트 발행 (Domain Service에서 생성된 이벤트를 발행만 함)
        publishDomainEvent(result);

        log.info(
                "FileAsset 생성 완료 (ExternalDownload): fileAssetId={}, outboxId={}",
                savedId.getValue(),
                result.outbox().getId().value());

        return savedId;
    }

    /**
     * 모든 Aggregate를 영속화합니다.
     *
     * @param result 생성 결과
     * @return 저장된 FileAsset ID
     */
    private FileAssetId persistAll(FileAssetCreationResult result) {
        // FileAsset 저장
        FileAssetId savedId = fileAssetTransactionManager.persist(result.fileAsset());
        log.debug("FileAsset 저장 완료: fileAssetId={}", savedId.getValue());

        // StatusHistory 저장
        statusHistoryTransactionManager.persist(result.statusHistory());
        log.debug("StatusHistory 저장 완료: fileAssetId={}", savedId.getValue());

        // Outbox 저장
        FileProcessingOutboxId outboxId = outboxTransactionManager.persist(result.outbox());
        log.debug(
                "Outbox 저장 완료: outboxId={}, fileAssetId={}", outboxId.value(), savedId.getValue());

        return savedId;
    }

    /**
     * Domain Service에서 생성된 도메인 이벤트를 발행합니다.
     *
     * <p>DDD 원칙에 따라 Facade는 이벤트를 생성하지 않고, Domain Service에서 생성된 이벤트를 발행만 합니다.
     *
     * <p>이벤트는 {@link FileAssetEventPublisher}를 통해 발행되며, 실제 SQS 발행은 트랜잭션 커밋 후 {@code
     * FileProcessingOutboxEventListener}에서 처리됩니다.
     *
     * <p><strong>래스터 이미지 조건</strong>: 리사이징 가능한 래스터 이미지(JPEG, PNG, GIF, WebP)인 경우에만 가공 이벤트를 발행합니다.
     * SVG 등 벡터 이미지는 제외됩니다. 추후 엑셀/문서 등 다른 파일 타입 처리가 필요한 경우 Worker에서 전략 패턴으로 분기합니다.
     *
     * @param result 생성 결과 (도메인 이벤트 포함)
     */
    private void publishDomainEvent(FileAssetCreationResult result) {
        if (!result.fileAsset().getContentType().isRasterImage()) {
            log.info(
                    "래스터 이미지가 아니므로 가공 이벤트 발행 생략: fileAssetId={}, contentType={}",
                    result.fileAsset().getIdValue(),
                    result.fileAsset().getContentTypeValue());
            return;
        }
        eventPublisher.publish(result.domainEvent());
    }
}
