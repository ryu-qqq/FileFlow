package com.ryuqq.fileflow.domain.transform.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import java.time.Instant;

/** 이미지 변환 완료 이벤트. 변환이 완료되면 발행되어 후속 작업(메타데이터 등록 등)을 트리거합니다. */
public record TransformCompletedEvent(
        String transformRequestId,
        String sourceAssetId,
        String resultAssetId,
        String transformType,
        int resultWidth,
        int resultHeight,
        Instant occurredAt)
        implements DomainEvent {

    public static TransformCompletedEvent of(
            String transformRequestId,
            String sourceAssetId,
            String resultAssetId,
            String transformType,
            int resultWidth,
            int resultHeight,
            Instant occurredAt) {
        return new TransformCompletedEvent(
                transformRequestId,
                sourceAssetId,
                resultAssetId,
                transformType,
                resultWidth,
                resultHeight,
                occurredAt);
    }
}
