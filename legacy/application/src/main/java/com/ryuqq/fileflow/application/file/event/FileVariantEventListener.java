package com.ryuqq.fileflow.application.file.event;

import com.ryuqq.fileflow.domain.file.variant.FileVariantCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * FileVariant Domain Event Listener
 *
 * <p><strong>역할</strong>: FileVariant 이벤트 처리</p>
 * <p><strong>위치</strong>: application/file/event/</p>
 *
 * <h3>이벤트 처리 전략</h3>
 * <ul>
 *   <li>✅ {@code @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)}</li>
 *   <li>✅ 트랜잭션 커밋 후 실행 (DB 저장 완료 후)</li>
 *   <li>✅ 외부 API 호출, 비동기 작업에 안전</li>
 * </ul>
 *
 * <h3>사용 시나리오</h3>
 * <ul>
 *   <li>다른 Aggregate 업데이트 (예: FileAsset 통계)</li>
 *   <li>외부 시스템 통보 (비동기)</li>
 *   <li>검색 인덱스 업데이트 (Elasticsearch)</li>
 *   <li>캐시 무효화</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class FileVariantEventListener {

    private static final Logger log = LoggerFactory.getLogger(FileVariantEventListener.class);

    /**
     * FileVariantCreatedEvent 처리
     *
     * <p>트랜잭션 커밋 후 실행됩니다.</p>
     *
     * <p><strong>처리 순서:</strong></p>
     * <ol>
     *   <li>FileVariant 저장 (트랜잭션 커밋)</li>
     *   <li>이 메서드 실행 (트랜잭션 밖)</li>
     *   <li>외부 시스템 통보, 비동기 작업 수행</li>
     * </ol>
     *
     * @param event FileVariantCreatedEvent
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFileVariantCreated(FileVariantCreatedEvent event) {
        log.info(
            "FileVariant created: fileVariantId={}, fileAssetId={}, variantType={}",
            event.fileVariantId() != null ? event.fileVariantId().value() : "null",
            event.fileAssetId().value(),
            event.variantType()
        );

        // ✅ 추가 작업 (필요 시):
        // - 다른 Aggregate 업데이트
        // - 외부 시스템 통보 (비동기)
        // - 통계 업데이트
        // - 검색 인덱스 업데이트
        // - 캐시 무효화
    }
}


