package com.ryuqq.fileflow.domain.upload.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 업로드 실패 이벤트
 * 업로드 세션이 실패로 처리되었음을 알립니다.
 */
public final class UploadFailedEvent implements DomainEvent {

    private final String sessionId;
    private final String uploaderId;
    private final String reason;
    private final LocalDateTime occurredOn;

    private UploadFailedEvent(
            String sessionId,
            String uploaderId,
            String reason,
            LocalDateTime occurredOn
    ) {
        this.sessionId = sessionId;
        this.uploaderId = uploaderId;
        this.reason = reason;
        this.occurredOn = occurredOn;
    }

    public static UploadFailedEvent of(
            String sessionId,
            String uploaderId,
            String reason
    ) {
        return new UploadFailedEvent(
                sessionId,
                uploaderId,
                reason,
                LocalDateTime.now()
        );
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "UploadFailedEvent";
    }

    // ========== Getters ==========

    public String getSessionId() {
        return sessionId;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public String getReason() {
        return reason;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadFailedEvent that = (UploadFailedEvent) o;
        return Objects.equals(sessionId, that.sessionId) &&
               Objects.equals(uploaderId, that.uploaderId) &&
               Objects.equals(reason, that.reason) &&
               Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, uploaderId, reason, occurredOn);
    }

    @Override
    public String toString() {
        return "UploadFailedEvent{" +
                "sessionId='" + sessionId + '\'' +
                ", uploaderId='" + uploaderId + '\'' +
                ", reason='" + reason + '\'' +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
