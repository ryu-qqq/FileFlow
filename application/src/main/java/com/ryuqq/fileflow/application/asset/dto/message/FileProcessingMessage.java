package com.ryuqq.fileflow.application.asset.dto.message;

/**
 * 파일 가공 SQS 메시지 DTO.
 *
 * <p>Resizing Worker로 전달되는 메시지입니다.
 *
 * @param fileAssetId FileAsset ID
 * @param outboxId Outbox ID (UUID 문자열, 발행 추적용)
 * @param eventType 이벤트 타입 (PROCESS_REQUEST, RETRY_REQUEST 등)
 */
public record FileProcessingMessage(String fileAssetId, String outboxId, String eventType) {

    /**
     * 팩토리 메서드.
     *
     * @param fileAssetId FileAsset ID
     * @param outboxId Outbox ID (UUID 문자열)
     * @param eventType 이벤트 타입
     * @return FileProcessingMessage
     */
    public static FileProcessingMessage of(String fileAssetId, String outboxId, String eventType) {
        return new FileProcessingMessage(fileAssetId, outboxId, eventType);
    }
}
