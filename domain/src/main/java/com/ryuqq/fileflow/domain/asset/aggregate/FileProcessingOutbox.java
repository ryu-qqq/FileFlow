package com.ryuqq.fileflow.domain.asset.aggregate;

import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import com.ryuqq.fileflow.domain.asset.vo.OutboxStatus;
import java.time.LocalDateTime;

/**
 * FileProcessingOutbox Aggregate.
 *
 * <p>Transactional Outbox 패턴을 위한 메시지 큐.
 *
 * <ul>
 *   <li>파일 가공 요청 메시지 (PROCESS_REQUEST)
 *   <li>상태 변경 알림 메시지 (STATUS_CHANGE)
 *   <li>재처리 요청 메시지 (RETRY_REQUEST)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class FileProcessingOutbox {

    // ===== 식별 정보 =====
    private final FileProcessingOutboxId id;
    private final Long fileAssetId;

    // ===== 메시지 정보 =====
    private final String eventType;
    private final String payload;

    // ===== 상태 정보 =====
    private OutboxStatus status;
    private int retryCount;
    private String errorMessage;

    // ===== 시간 정보 =====
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;

    private FileProcessingOutbox(
            FileProcessingOutboxId id,
            Long fileAssetId,
            String eventType,
            String payload,
            OutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        validateNotNull(id, "FileProcessingOutboxId");
        validateNotNull(fileAssetId, "FileAssetId");
        validateNotNull(eventType, "EventType");
        validateNotNull(payload, "Payload");
        validateNotNull(status, "Status");
        validateNotNull(createdAt, "CreatedAt");

        this.id = id;
        this.fileAssetId = fileAssetId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    // ===== 팩토리 메서드 =====

    /**
     * 가공 요청용 Outbox 생성.
     *
     * @param fileAssetId 파일 에셋 ID
     * @param eventType 이벤트 타입
     * @param payload JSON 페이로드
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox forProcessRequest(
            Long fileAssetId, String eventType, String payload) {
        return new FileProcessingOutbox(
                FileProcessingOutboxId.forNew(),
                fileAssetId,
                eventType,
                payload,
                OutboxStatus.PENDING,
                0,
                null,
                LocalDateTime.now(),
                null);
    }

    /**
     * 상태 변경 알림용 Outbox 생성.
     *
     * @param fileAssetId 파일 에셋 ID
     * @param fromStatus 이전 상태
     * @param toStatus 새 상태
     * @param payload JSON 페이로드
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox forStatusChange(
            Long fileAssetId, String fromStatus, String toStatus, String payload) {
        return new FileProcessingOutbox(
                FileProcessingOutboxId.forNew(),
                fileAssetId,
                "STATUS_CHANGE",
                payload,
                OutboxStatus.PENDING,
                0,
                null,
                LocalDateTime.now(),
                null);
    }

    /**
     * 재처리 요청용 Outbox 생성.
     *
     * @param fileAssetId 파일 에셋 ID
     * @param reason 재처리 사유
     * @param payload JSON 페이로드
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox forRetryRequest(Long fileAssetId, String reason, String payload) {
        return new FileProcessingOutbox(
                FileProcessingOutboxId.forNew(),
                fileAssetId,
                "RETRY_REQUEST",
                payload,
                OutboxStatus.PENDING,
                0,
                null,
                LocalDateTime.now(),
                null);
    }

    /**
     * DB에서 복원 (Persistence Layer용).
     *
     * @param id Outbox ID
     * @param fileAssetId 파일 에셋 ID
     * @param eventType 이벤트 타입
     * @param payload JSON 페이로드
     * @param status Outbox 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @param createdAt 생성 시각
     * @param processedAt 처리 완료 시각
     * @return FileProcessingOutbox
     */
    public static FileProcessingOutbox reconstitute(
            FileProcessingOutboxId id,
            Long fileAssetId,
            String eventType,
            String payload,
            OutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        return new FileProcessingOutbox(
                id, fileAssetId, eventType, payload, status, retryCount, errorMessage, createdAt, processedAt);
    }

    // ===== Private Helper =====

    private void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "는 null일 수 없습니다.");
        }
    }

    // ===== Getter =====

    public FileProcessingOutboxId getId() {
        return id;
    }

    public Long getFileAssetId() {
        return fileAssetId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
