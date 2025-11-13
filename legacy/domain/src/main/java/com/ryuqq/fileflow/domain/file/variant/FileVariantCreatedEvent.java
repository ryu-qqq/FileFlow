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
 * @param variantType Variant 타입 (THUMBNAIL, PREVIEW, COMPRESSED 등)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record FileVariantCreatedEvent(
    FileVariantId fileVariantId,
    FileAssetId fileAssetId,
    VariantType variantType
) {

    /**
     * Compact Constructor
     * Null 검증
     *
     * <p><strong>주의:</strong></p>
     * <ul>
     *   <li>fileVariantId는 생성 시점(null)과 발행 시점(실제 ID) 모두 허용</li>
     *   <li>Adapter에서 실제 ID로 이벤트를 재생성하여 발행</li>
     * </ul>
     *
     * @throws IllegalArgumentException 필수 필드가 null인 경우 (fileVariantId 제외)
     */
    public FileVariantCreatedEvent {
        // fileVariantId는 null 허용 (생성 시점에는 아직 ID가 할당되지 않음)
        // Adapter에서 실제 ID로 이벤트를 재생성하여 발행
        if (fileAssetId == null) {
            throw new IllegalArgumentException("fileAssetId must not be null");
        }
        if (variantType == null) {
            throw new IllegalArgumentException("variantType must not be null");
        }
    }

}
