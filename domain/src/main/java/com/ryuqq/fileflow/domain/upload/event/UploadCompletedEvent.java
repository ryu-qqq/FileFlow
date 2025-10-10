package com.ryuqq.fileflow.domain.upload.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 업로드 완료 이벤트
 * 업로드 세션이 성공적으로 완료되었음을 알립니다.
 */
public final class UploadCompletedEvent implements DomainEvent {

    private final String sessionId;
    private final String uploaderId;
    private final String fileId;
    private final String s3Uri;
    private final LocalDateTime occurredOn;

    private UploadCompletedEvent(
            String sessionId,
            String uploaderId,
            String fileId,
            String s3Uri,
            LocalDateTime occurredOn
    ) {
        this.sessionId = sessionId;
        this.uploaderId = uploaderId;
        this.fileId = fileId;
        this.s3Uri = s3Uri;
        this.occurredOn = occurredOn;
    }

    public static UploadCompletedEvent of(
            String sessionId,
            String uploaderId,
            String fileId,
            String s3Uri
    ) {
        return new UploadCompletedEvent(
                sessionId,
                uploaderId,
                fileId,
                s3Uri,
                LocalDateTime.now()
        );
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "UploadCompletedEvent";
    }

    // ========== Getters ==========

    public String getSessionId() {
        return sessionId;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getS3Uri() {
        return s3Uri;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadCompletedEvent that = (UploadCompletedEvent) o;
        return Objects.equals(sessionId, that.sessionId) &&
               Objects.equals(uploaderId, that.uploaderId) &&
               Objects.equals(fileId, that.fileId) &&
               Objects.equals(s3Uri, that.s3Uri) &&
               Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, uploaderId, fileId, s3Uri, occurredOn);
    }

    @Override
    public String toString() {
        return "UploadCompletedEvent{" +
                "sessionId='" + sessionId + '\'' +
                ", uploaderId='" + uploaderId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", s3Uri='" + s3Uri + '\'' +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
