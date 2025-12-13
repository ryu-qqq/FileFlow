package com.ryuqq.fileflow.domain.asset.service;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.event.FileProcessingRequestedEvent;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;

/**
 * FileAsset 생성 결과.
 *
 * <p>FileAssetCreationService에서 생성한 Aggregate들과 도메인 이벤트를 담는 결과 객체입니다.
 *
 * <p><strong>DDD 원칙</strong>:
 *
 * <ul>
 *   <li>도메인 이벤트는 도메인 라이프사이클에서 생성됨 (Facade가 아닌 Domain Service에서)
 *   <li>Application Layer(Facade)는 이벤트를 발행만 할 뿐, 생성하지 않음
 * </ul>
 *
 * @param fileAsset 생성된 FileAsset
 * @param statusHistory 초기 상태 이력
 * @param outbox 처리 요청 Outbox
 * @param domainEvent 파일 가공 요청 도메인 이벤트
 */
public record FileAssetCreationResult(
        FileAsset fileAsset,
        FileAssetStatusHistory statusHistory,
        FileProcessingOutbox outbox,
        FileProcessingRequestedEvent domainEvent) {

    public FileAssetCreationResult {
        if (fileAsset == null) {
            throw new IllegalArgumentException("FileAsset은 null일 수 없습니다.");
        }
        if (statusHistory == null) {
            throw new IllegalArgumentException("StatusHistory는 null일 수 없습니다.");
        }
        if (outbox == null) {
            throw new IllegalArgumentException("Outbox은 null일 수 없습니다.");
        }
        if (domainEvent == null) {
            throw new IllegalArgumentException("DomainEvent는 null일 수 없습니다.");
        }
    }

    /**
     * FileAsset ID를 반환합니다.
     *
     * @return FileAssetId
     */
    public FileAssetId fileAssetId() {
        return fileAsset.getId();
    }
}
