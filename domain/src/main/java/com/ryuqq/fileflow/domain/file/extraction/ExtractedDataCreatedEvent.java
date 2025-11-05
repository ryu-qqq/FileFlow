package com.ryuqq.fileflow.domain.file.extraction;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;

/**
 * ExtractedData Created Event
 * 데이터 추출이 완료되었을 때 발행되는 도메인 이벤트
 *
 * <p><strong>이벤트 발행 시점:</strong></p>
 * <ul>
 *   <li>ExtractedData.create() 호출 시</li>
 *   <li>트랜잭션 커밋 시점에 Spring Data가 자동 발행</li>
 * </ul>
 *
 * <p><strong>이벤트 처리:</strong></p>
 * <ul>
 *   <li>@TransactionalEventListener로 수신</li>
 *   <li>다른 Aggregate에 이벤트 전파</li>
 *   <li>외부 시스템 통보 (비동기)</li>
 *   <li>검색 인덱스 업데이트 (Elasticsearch)</li>
 * </ul>
 *
 * @param extractedDataId 생성된 ExtractedData ID (null 가능 - 생성 시점)
 * @param fileAssetId 원본 File Asset ID
 * @param extractionType 추출 유형 (METADATA, OCR, FORM, HTML, TABLE)
 * @param extractionMethod 추출 방법 (TIKA, TEXTRACT, TESSERACT, CUSTOM)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ExtractedDataCreatedEvent(
    ExtractedDataId extractedDataId,
    FileAssetId fileAssetId,
    String extractionType,
    String extractionMethod
) {

    /**
     * Compact Constructor
     * Null 검증
     *
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     */
    public ExtractedDataCreatedEvent {
        // extractedDataId는 null 가능 (생성 시점에 아직 할당 안 됨)
        if (fileAssetId == null) {
            throw new IllegalArgumentException("fileAssetId must not be null");
        }
        if (extractionType == null || extractionType.isBlank()) {
            throw new IllegalArgumentException("extractionType must not be null or blank");
        }
        if (extractionMethod == null || extractionMethod.isBlank()) {
            throw new IllegalArgumentException("extractionMethod must not be null or blank");
        }
    }

}
