package com.ryuqq.fileflow.domain.file.variant;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;

/**
 * File Variant Created Event
 * File Variant가 생성되었을 때 발행되는 도메인 이벤트
 *
 * <p><strong>이벤트 발행 시점:</strong></p>
 * <ul>
 *   <li>FileVariant.create() 호출 시</li>
 *   <li>트랜잭션 커밋 시점에 Spring Data가 자동 발행</li>
 * </ul>
 *
 * <p><strong>이벤트 처리:</strong></p>
 * <ul>
 *   <li>@TransactionalEventListener로 수신</li>
 *   <li>다른 Aggregate에 이벤트 전파</li>
 *   <li>외부 시스템 통보 (비동기)</li>
 * </ul>
 *
 * @param fileVariantId 생성된 File Variant ID
 * @param fileAssetId 부모 File Asset ID
 * @param variantType Variant 타입 (예: thumbnail, preview)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record FileVariantCreatedEvent(
    FileVariantId fileVariantId,
    FileAssetId fileAssetId,
    String variantType
) {

    /**
     * Compact Constructor
     * Null 검증
     *
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     */
    public FileVariantCreatedEvent {
        if (fileVariantId == null) {
            throw new IllegalArgumentException("fileVariantId must not be null");
        }
        if (fileAssetId == null) {
            throw new IllegalArgumentException("fileAssetId must not be null");
        }
        if (variantType == null || variantType.isBlank()) {
            throw new IllegalArgumentException("variantType must not be null or blank");
        }
    }

}
