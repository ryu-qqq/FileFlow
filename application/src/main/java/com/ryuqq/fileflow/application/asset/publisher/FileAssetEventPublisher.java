package com.ryuqq.fileflow.application.asset.publisher;

import com.ryuqq.fileflow.application.common.config.TransactionEventRegistry;
import com.ryuqq.fileflow.domain.asset.event.FileProcessingRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * FileAsset 관련 도메인 이벤트 발행자.
 *
 * <p>트랜잭션 커밋 후 이벤트 발행을 보장합니다 (APP-ER-002, APP-ER-005).
 *
 * <p><strong>Transactional Outbox 패턴</strong>:
 *
 * <ul>
 *   <li>이벤트 등록은 {@link TransactionEventRegistry}를 통해 수행
 *   <li>트랜잭션 커밋 후 이벤트 발행
 *   <li>트랜잭션 롤백 시 이벤트가 발행되지 않음 (안전성 보장)
 * </ul>
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>도메인 이벤트 발행 추상화
 *   <li>이벤트 발행 로깅
 *   <li>향후 이벤트 발행 전략 변경 시 단일 변경점 제공
 * </ul>
 */
@Component
public class FileAssetEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(FileAssetEventPublisher.class);

    private final TransactionEventRegistry transactionEventRegistry;

    public FileAssetEventPublisher(TransactionEventRegistry transactionEventRegistry) {
        this.transactionEventRegistry = transactionEventRegistry;
    }

    /**
     * 파일 가공 요청 이벤트를 트랜잭션 커밋 후 발행하도록 등록합니다.
     *
     * <p>등록된 이벤트는 트랜잭션 커밋 후 발행됩니다.
     *
     * @param event 파일 가공 요청 이벤트
     */
    public void publish(FileProcessingRequestedEvent event) {
        log.debug(
                "FileProcessingRequestedEvent 등록: outboxId={}, fileAssetId={}",
                event.outboxId().value(),
                event.fileAssetId().getValue());

        transactionEventRegistry.registerObjectForPublish(event);
    }
}
