package com.ryuqq.fileflow.application.asset.listener;

import com.ryuqq.fileflow.application.asset.dto.message.FileProcessingMessage;
import com.ryuqq.fileflow.application.asset.factory.command.FileAssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.FileProcessingOutboxTransactionManager;
import com.ryuqq.fileflow.application.asset.port.out.client.FileProcessingSqsPublishPort;
import com.ryuqq.fileflow.application.asset.port.out.query.FileProcessingOutboxQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.event.FileProcessingRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 파일 가공 요청 이벤트 리스너.
 *
 * <p>트랜잭션 커밋 후 SQS 메시지를 발행하고 Outbox 상태를 업데이트합니다.
 *
 * <p><strong>Transactional Outbox 패턴 구현</strong>:
 *
 * <ol>
 *   <li>FileAssetCreationFacade에서 Outbox 저장 + 이벤트 발행
 *   <li>트랜잭션 커밋 (AFTER_COMMIT)
 *   <li>이 리스너에서 SQS 발행
 *   <li>발행 성공/실패에 따라 Outbox 상태 업데이트
 * </ol>
 *
 * <p><strong>트랜잭션 전파</strong>:
 *
 * <ul>
 *   <li>REQUIRES_NEW: 독립적인 트랜잭션에서 Outbox 상태 업데이트
 *   <li>SQS 발행 실패 시에도 Outbox 상태는 FAILED로 기록됨
 * </ul>
 *
 * <p><strong>실패 처리</strong>:
 *
 * <ul>
 *   <li>SQS 발행 실패: Outbox 상태 FAILED 업데이트, 스케줄러에서 재시도
 *   <li>Outbox 조회 실패: 로그만 기록 (Outbox는 이미 DB에 있음)
 * </ul>
 *
 * <p><strong>조건부 등록</strong>:
 *
 * <ul>
 *   <li>FileProcessingSqsPublishPort 빈이 존재할 때만 등록됩니다.
 *   <li>download-worker 등 SQS 발행 기능이 없는 모듈에서는 비활성화됩니다.
 * </ul>
 */
@Component
@ConditionalOnBean(FileProcessingSqsPublishPort.class)
public class FileProcessingOutboxEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(FileProcessingOutboxEventListener.class);

    private final FileProcessingSqsPublishPort sqsPublishPort;
    private final FileProcessingOutboxQueryPort outboxQueryPort;
    private final FileProcessingOutboxTransactionManager outboxTransactionManager;
    private final FileAssetCommandFactory commandFactory;

    public FileProcessingOutboxEventListener(
            FileProcessingSqsPublishPort sqsPublishPort,
            FileProcessingOutboxQueryPort outboxQueryPort,
            FileProcessingOutboxTransactionManager outboxTransactionManager,
            FileAssetCommandFactory commandFactory) {
        this.sqsPublishPort = sqsPublishPort;
        this.outboxQueryPort = outboxQueryPort;
        this.outboxTransactionManager = outboxTransactionManager;
        this.commandFactory = commandFactory;
    }

    /**
     * 파일 가공 요청 이벤트를 처리합니다.
     *
     * <p>트랜잭션 커밋 후 실행되며, SQS 메시지를 발행하고 Outbox 상태를 업데이트합니다.
     *
     * @param event 파일 가공 요청 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(FileProcessingRequestedEvent event) {
        log.info(
                "파일 가공 요청 이벤트 처리 시작: outboxId={}, fileAssetId={}",
                event.outboxId().value(),
                event.fileAssetId().getValue());

        try {
            // SQS 메시지 생성
            FileProcessingMessage message =
                    FileProcessingMessage.of(
                            event.fileAssetId().getValue(),
                            event.outboxId().getValue(),
                            event.eventType());

            // SQS 발행
            boolean published = sqsPublishPort.publish(message);

            if (published) {
                handleSuccess(event);
            } else {
                handleFailure(event, "SQS 발행 실패");
            }

        } catch (Exception e) {
            log.error(
                    "파일 가공 요청 이벤트 처리 중 예외 발생: outboxId={}, error={}",
                    event.outboxId().value(),
                    e.getMessage(),
                    e);
            handleFailure(event, e.getMessage());
        }
    }

    /**
     * SQS 발행 성공 처리.
     *
     * @param event 이벤트
     */
    private void handleSuccess(FileProcessingRequestedEvent event) {
        log.info(
                "SQS 발행 성공: outboxId={}, fileAssetId={}",
                event.outboxId().value(),
                event.fileAssetId().getValue());

        try {
            // Outbox 조회 및 상태 업데이트
            FileProcessingOutbox outbox =
                    outboxQueryPort
                            .findById(event.outboxId())
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Outbox not found: "
                                                            + event.outboxId().value()));

            commandFactory.markOutboxAsSent(outbox);
            outboxTransactionManager.persist(outbox);

            log.debug("Outbox 상태 업데이트 완료: outboxId={}, status=SENT", event.outboxId().value());

        } catch (Exception e) {
            // Outbox 상태 업데이트 실패는 치명적이지 않음 (SQS 메시지는 이미 발행됨)
            log.warn(
                    "Outbox 상태 업데이트 실패 (SQS 발행은 성공): outboxId={}, error={}",
                    event.outboxId().value(),
                    e.getMessage());
        }
    }

    /**
     * SQS 발행 실패 처리.
     *
     * @param event 이벤트
     * @param errorMessage 에러 메시지
     */
    private void handleFailure(FileProcessingRequestedEvent event, String errorMessage) {
        log.warn(
                "SQS 발행 실패: outboxId={}, fileAssetId={}, error={}",
                event.outboxId().value(),
                event.fileAssetId().getValue(),
                errorMessage);

        try {
            // Outbox 조회 및 실패 상태 업데이트
            FileProcessingOutbox outbox =
                    outboxQueryPort
                            .findById(event.outboxId())
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Outbox not found: "
                                                            + event.outboxId().value()));

            outbox.markAsFailed(errorMessage);
            outboxTransactionManager.persist(outbox);

            log.debug(
                    "Outbox 실패 상태 업데이트 완료: outboxId={}, status=FAILED, retryCount={}",
                    event.outboxId().value(),
                    outbox.getRetryCount());

        } catch (Exception e) {
            log.error(
                    "Outbox 실패 상태 업데이트 중 예외 발생: outboxId={}, error={}",
                    event.outboxId().value(),
                    e.getMessage(),
                    e);
        }
    }
}
