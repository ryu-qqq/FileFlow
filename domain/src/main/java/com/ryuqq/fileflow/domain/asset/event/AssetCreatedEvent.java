package com.ryuqq.fileflow.domain.asset.event;

import java.time.Instant;

import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;

/**
 * Asset 생성 이벤트.
 * Asset이 생성되면 발행되어 메타데이터 추출 등 후속 작업을 트리거합니다.
 */
public record AssetCreatedEvent(
        String assetId,
        String s3Key,
        String bucket,
        String contentType,
        String purpose,
        String source,
        Instant occurredAt
) implements DomainEvent {

    public static AssetCreatedEvent from(Asset asset, Instant occurredAt) {
        return new AssetCreatedEvent(
                asset.id().value(),
                asset.s3Key(),
                asset.bucket(),
                asset.contentType(),
                asset.purpose(),
                asset.source(),
                occurredAt
        );
    }
}
